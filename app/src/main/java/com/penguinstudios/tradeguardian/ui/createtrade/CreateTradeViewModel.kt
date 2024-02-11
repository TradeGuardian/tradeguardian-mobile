package com.penguinstudios.tradeguardian.ui.createtrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.ContractDeployment
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.data.model.ExchangeRateResponse
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.data.model.UserRole
import com.penguinstudios.tradeguardian.util.Constants
import com.penguinstudios.tradeguardian.util.WalletUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.web3j.protocol.core.methods.response.EthEstimateGas
import org.web3j.protocol.core.methods.response.EthGasPrice
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateTradeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val _uiState = MutableSharedFlow<CreateTradeUIState>()
    val uiState = _uiState.asSharedFlow()
    private lateinit var contractDeployment: ContractDeployment
    private var itemCostUsd: String? = null
    private var totalDeploymentGasCostEther: String? = null

    fun onCreateTradeClick(
        userRole: UserRole,
        counterPartyAddress: String,
        itemPrice: String,
        description: String
    ) {
        viewModelScope.launch {
            try {
                contractDeployment = ContractDeployment.builder()
                    .network(Network.TEST_NET)
                    .feeRecipientAddress(Constants.FEE_RECIPIENT)
                    .userRole(userRole)
                    .itemPrice(itemPrice)
                    .description(description)
                    .userWalletAddress(walletRepository.credentials.address)
                    .counterPartyAddress(counterPartyAddress)
                    .build()

                // Start all operations concurrently
                val gasPriceDeferred = async { estimateGasPrice() }
                val gasLimitDeferred = async { estimateDeployContractGasLimit(contractDeployment) }
                val exchangeRateDeferred = async { getBnbUsdtExchangeRate() }

                // Await all results
                val gasPriceResult = gasPriceDeferred.await()
                val gasLimitResult = gasLimitDeferred.await()
                val exchangeRateResult = exchangeRateDeferred.await()

                if (gasPriceResult == null || gasLimitResult == null) {
                    _uiState.emit(CreateTradeUIState.Error("Current node is having problems. Please try again later."))
                    return@launch
                } else {
                    val gasPrice = gasPriceResult.gasPrice
                    val gasLimit = gasLimitResult.amountUsed
                    val deployContractCostWei: BigInteger = gasPrice.multiply(gasLimit)
                    totalDeploymentGasCostEther = WalletUtil.weiToEther(deployContractCostWei)
                        .toString() + " " + contractDeployment.network.networkTokenName
                }

                if (exchangeRateResult != null) {
                    val unformattedUsdAmount =
                        BigDecimal(exchangeRateResult.price).multiply(contractDeployment.itemPriceDecimal)
                    itemCostUsd = NumberFormat.getCurrencyInstance(Locale.US)
                        .format(unformattedUsdAmount) + " USD"
                }

                _uiState.emit(
                    CreateTradeUIState.ConfirmContractDeployment(
                        contractDeployment,
                        itemCostUsd,
                        totalDeploymentGasCostEther
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(CreateTradeUIState.Error(e.message.toString()))
            }
        }
    }

    private suspend fun estimateGasPrice(): EthGasPrice? {
        return try {
            remoteRepository.estimateGasPrice()
        } catch (e: TimeoutCancellationException) {
            Timber.e("Estimate gas price timed out")
            null
        } catch (e: Exception) {
            Timber.e(e, "Failed to estimate gas price")
            null
        }
    }

    private suspend fun estimateDeployContractGasLimit(contractDeployment: ContractDeployment): EthEstimateGas? {
        return try {
            remoteRepository.estimateGasLimit(contractDeployment)
        } catch (e: TimeoutCancellationException) {
            Timber.e("Estimate gas limit timed out")
            null
        } catch (e: Exception) {
            Timber.e(e, "Failed to estimate deploy contract gas limit")
            null
        }
    }

    private suspend fun getBnbUsdtExchangeRate(): ExchangeRateResponse? {
        return try {
            remoteRepository.getBnbUsdtExchangeRate()
        } catch (e: TimeoutCancellationException) {
            Timber.e("Get BNB/USD exchange rate timed out")
            null
        } catch (e: Exception) {
            Timber.e(e, "Failed to get BNB/USD exchanged rate")
            null
        }
    }

    fun onConfirmBtnClick() {
        viewModelScope.launch {
            try {
                val txReceipt = remoteRepository.deployContract(contractDeployment)
                _uiState.emit(
                    CreateTradeUIState.SuccessDeployContract(
                        txReceipt.transactionHash,
                        txReceipt.contractAddress
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to deploy contract")
                _uiState.emit(CreateTradeUIState.Error(e.message.toString()))
            }
        }
    }
}
