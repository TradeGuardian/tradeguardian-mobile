package com.penguinstudios.tradeguardian.ui.createtrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.model.ContractDeployment
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
import javax.inject.Inject

@HiltViewModel
class CreateTradeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    private val _uiState = MutableSharedFlow<CreateTradeUIState>()
    val uiState = _uiState.asSharedFlow()
    private lateinit var contractDeployment: ContractDeployment
    private lateinit var gasPrice: BigInteger
    private lateinit var gasLimit: BigInteger

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

                _uiState.emit(CreateTradeUIState.ConfirmContractDeployment(contractDeployment))
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(CreateTradeUIState.Error(e.message.toString()))
            }
        }
    }

    fun getDeploymentCosts() {
        viewModelScope.launch {
            try {
                val itemCostEther: BigDecimal
                val formattedGasCostEther: String
                var formattedItemCostUsd: String? = null
                var formattedGasCostUsd: String? = null

                // Start all operations concurrently
                val gasPriceDeferred = async { estimateGasPrice() }
                val gasLimitDeferred = async { estimateDeployContractGasLimit(contractDeployment) }
                val exchangeRateDeferred = async { getBnbUsdtExchangeRate() }

                // Await all results
                val gasPriceResult = gasPriceDeferred.await()
                val gasLimitResult = gasLimitDeferred.await()
                val exchangeRateResult = exchangeRateDeferred.await()

                if (gasPriceResult == null || gasLimitResult == null) {
                    _uiState.emit(CreateTradeUIState.FailedToGetGasData)
                    return@launch
                } else {
                    gasPrice = gasPriceResult.gasPrice
                    gasLimit = gasLimitResult.amountUsed
                    val deployContractCostWei: BigInteger = gasPrice.multiply(gasLimit)
                    itemCostEther = WalletUtil.weiToEther(deployContractCostWei)
                    formattedGasCostEther = itemCostEther.toString() + " " +
                            contractDeployment.network.networkTokenName
                }

                if (exchangeRateResult == null) {
                    _uiState.emit(CreateTradeUIState.FailedToGetExchangeRate(formattedGasCostEther))
                    return@launch
                } else {
                    val unformattedItemCostUsd =
                        BigDecimal(exchangeRateResult.price).multiply(contractDeployment.itemPriceDecimal)
                    val unformattedGasCostUsd =
                        BigDecimal(exchangeRateResult.price).multiply(itemCostEther)
                    formattedItemCostUsd = WalletUtil.formatToUSD(unformattedItemCostUsd)
                    formattedGasCostUsd = WalletUtil.formatToUSD(unformattedGasCostUsd)
                }

                _uiState.emit(
                    CreateTradeUIState.SuccessGetDeploymentCosts(
                        formattedItemCostUsd,
                        formattedGasCostEther,
                        formattedGasCostUsd
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
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
                _uiState.emit(CreateTradeUIState.ShowDeployContractProgress)

                val txReceipt = remoteRepository.deployContract(
                    contractDeployment, gasPrice, gasLimit
                )

                //Calculate how much ether the gas is worth
                val amountEtherUsed = txReceipt.gasUsed.multiply(gasPrice)

                _uiState.emit(
                    CreateTradeUIState.SuccessDeployContract(
                        txReceipt.transactionHash,
                        txReceipt.contractAddress,
                        WalletUtil.weiToEther(amountEtherUsed).toString() + " BNB"
                    )
                )

                _uiState.emit(CreateTradeUIState.HideDeployContractProgress)
            } catch (e: Exception) {
                Timber.e(e, "Failed to deploy contract")
                _uiState.emit(CreateTradeUIState.HideDeployContractProgress)
                _uiState.emit(CreateTradeUIState.Error(e.message.toString()))
            }
        }
    }
}
