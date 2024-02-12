package com.penguinstudios.tradeguardian.data

import com.penguinstudios.tradeguardian.contract.Escrow
import com.penguinstudios.tradeguardian.data.model.ContractDeployment
import com.penguinstudios.tradeguardian.data.model.ExchangeRateResponse
import com.penguinstudios.tradeguardian.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.protocol.core.methods.response.EthGasPrice
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteRepository @Inject constructor(
    private val web3j: Web3j,
    private val walletRepository: WalletRepository,
    private val binanceService: BinanceService
) {

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

    suspend fun estimateGasLimit(contractDeployment: ContractDeployment): EthEstimateGas {
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

    suspend fun getBnbUsdtExchangeRate(): ExchangeRateResponse {
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
}