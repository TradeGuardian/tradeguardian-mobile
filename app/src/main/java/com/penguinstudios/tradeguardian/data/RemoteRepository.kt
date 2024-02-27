package com.penguinstudios.tradeguardian.data

import com.penguinstudios.tradeguardian.contract.Escrow
import com.penguinstudios.tradeguardian.data.model.ContractDeployment
import com.penguinstudios.tradeguardian.data.model.ContractStatus
import com.penguinstudios.tradeguardian.data.model.ExchangeRateResponse
import com.penguinstudios.tradeguardian.data.validator.EtherAmountValidator
import com.penguinstudios.tradeguardian.util.Constants
import com.penguinstudios.tradeguardian.util.CustomGasProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.protocol.core.methods.response.EthGasPrice
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Transfer
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Convert
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RemoteRepository @Inject constructor(
    private val web3j: Web3j,
    private val walletRepository: WalletRepository,
    private val binanceService: BinanceService
) {

    suspend fun send(sendToAddress: String, amount: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            require(WalletUtils.isValidAddress(sendToAddress)) { "Not a valid user wallet address" }

            val itemPriceDecimal = EtherAmountValidator.validateAndConvert(amount)

            Transfer.sendFunds(
                web3j,
                walletRepository.credentials,
                sendToAddress,
                itemPriceDecimal,
                Convert.Unit.ETHER
            ).sendAsync().await()
        }
    }

    suspend fun getWalletBalance(): EthGetBalance {
        return withContext(Dispatchers.IO) {
            withTimeout(15000) {
                web3j.ethGetBalance(
                    walletRepository.credentials.address,
                    DefaultBlockParameterName.LATEST
                ).sendAsync().await()
            }
        }
    }

    suspend fun estimateGasPrice(): EthGasPrice {
        return withContext(Dispatchers.IO) {
            withTimeout(15000) {
                web3j.ethGasPrice().sendAsync().await()
            }
        }
    }

    suspend fun buyerDeposit(
        contractAddress: String,
        depositAmountWei: BigInteger
    ): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(contractAddress, web3j, walletRepository.credentials, CustomGasProvider())
                .buyerDeposit(depositAmountWei)
                .sendAsync()
                .await()
        }
    }

    suspend fun sellerDeposit(
        contractAddress: String,
        depositAmountWei: BigInteger
    ): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(contractAddress, web3j, walletRepository.credentials, CustomGasProvider())
                .sellerDeposit(depositAmountWei)
                .sendAsync()
                .await()
        }
    }

    private suspend fun getUserBalance(
        contractAddress: String,
        walletAddress: String
    ): BigInteger {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress)
                .userBalances(walletAddress)
                .sendAsync()
                .await()
        }
    }

    suspend fun hasDeposited(
        contractAddress: String,
        walletAddress: String
    ): Boolean {
        return getUserBalance(contractAddress, walletAddress) > BigInteger.ZERO
    }

    suspend fun correctItemReceived(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(contractAddress, web3j, walletRepository.credentials, CustomGasProvider())
                .setBuyerHasReceivedCorrectItem()
                .sendAsync()
                .await()
        }
    }

    suspend fun incorrectItemReceived(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(contractAddress, web3j, walletRepository.credentials, CustomGasProvider())
                .setBuyerHasReceivedIncorrectItem()
                .sendAsync()
                .await()
        }
    }

    suspend fun itemDelivered(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(contractAddress, web3j, walletRepository.credentials, CustomGasProvider())
                .setSellerHasGivenItem()
                .sendAsync()
                .await()
        }
    }

    suspend fun estimateDeployContractGasLimit(contractDeployment: ContractDeployment): EthEstimateGas {
        return withContext(Dispatchers.IO) {
            withTimeout(15000) {
                val feeRecipientAddress = Address(contractDeployment.feeRecipientAddress)
                val sellerAddress = Address(contractDeployment.sellerAddress)
                val buyerAddress = Address(contractDeployment.buyerAddress)
                val itemPriceWei = Uint256(contractDeployment.itemPriceWei)
                val description = Utf8String(contractDeployment.description)

                val encodedConstructorParameters = FunctionEncoder.encodeConstructor(
                    listOf(
                        feeRecipientAddress,
                        sellerAddress,
                        buyerAddress,
                        itemPriceWei,
                        description
                    )
                )

                val data = Escrow.BINARY + encodedConstructorParameters

                web3j.ethEstimateGas(
                    Transaction.createEthCallTransaction(
                        walletRepository.credentials.address,
                        null, // To address is null for contract deployment
                        data,
                    )
                ).sendAsync().await()
            }
        }
    }

    suspend fun getBnbUsdExchangeRate(): ExchangeRateResponse {
        return withContext(Dispatchers.IO) {
            binanceService.getPrice(Constants.BNB_USDT_EXCHANGE_RATE_SYMBOL)
        }
    }

    suspend fun deployContract(
        contractDeployment: ContractDeployment,
        gasPrice: BigInteger,
        gasLimit: BigInteger
    ): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.deploy(
                web3j,
                walletRepository.credentials,
                StaticGasProvider(gasPrice, gasLimit),
                contractDeployment.feeRecipientAddress,
                contractDeployment.sellerAddress,
                contractDeployment.buyerAddress,
                contractDeployment.itemPriceWei,
                contractDeployment.description
            )
                .sendAsync()
                .await()
                .transactionReceipt
                .orElseThrow {
                    IllegalStateException("Transaction receipt not present")
                }
        }
    }


    suspend fun cancelTrade(contractAddress: String): TransactionReceipt? {
        return if (getUserBalance(
                contractAddress,
                walletRepository.credentials.address
            ) != BigInteger.ZERO
        ) {
            withContext(Dispatchers.IO) {
                Escrow.load(
                    contractAddress,
                    web3j,
                    walletRepository.credentials,
                    CustomGasProvider()
                )
                    .withdraw()
                    .sendAsync()
                    .await()
            }
        } else {
            null
        }
    }

    fun isUserInvolvedInTrade(buyerAddress: String, sellerAddress: String): Boolean {
        val userWalletAddress = walletRepository.credentials.address
        return userWalletAddress == buyerAddress || userWalletAddress == sellerAddress
    }

    suspend fun settle(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(contractAddress, web3j, walletRepository.credentials, CustomGasProvider())
                .settle()
                .sendAsync()
                .await()
        }
    }


    //Used for read only methods
    private fun loadContract(contractAddress: String): Escrow {
        return Escrow.load(
            contractAddress, web3j, walletRepository.credentials, DefaultGasProvider()
        )
    }

    suspend fun getContractStatus(contractAddress: String): ContractStatus {
        return withContext(Dispatchers.IO) {
            val contractStatusId = loadContract(contractAddress).currentState().sendAsync().await()
            ContractStatus.getStatusById(contractStatusId.toInt())
        }
    }

    suspend fun getDateCreatedSeconds(contractAddress: String): BigInteger {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress).contractCreationDate().sendAsync().await()
        }
    }

    suspend fun getItemPriceWei(contractAddress: String): BigInteger {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress).itemPrice().sendAsync().await()
        }
    }

    suspend fun getSellerAddress(contractAddress: String): String {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress).seller().sendAsync().await()
        }
    }

    suspend fun getBuyerAddress(contractAddress: String): String {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress).buyer().sendAsync().await()
        }
    }

    suspend fun getDescription(contractAddress: String): String {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress).description().sendAsync().await()
        }
    }

    suspend fun hasBuyerSettled(contractAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress).hasBuyerSettled().sendAsync().await()
        }
    }

    suspend fun hasSellerSettled(contractAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            loadContract(contractAddress).hasSellerSettled().sendAsync().await()
        }
    }
}