package com.penguinstudios.tradeguardian.ui.tradeinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.LocalRepository
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.data.counterPartyRole
import com.penguinstudios.tradeguardian.data.getBuyerDepositAmount
import com.penguinstudios.tradeguardian.data.getFormattedAmountReturnedToBuyer
import com.penguinstudios.tradeguardian.data.getFormattedAmountReturnedToSeller
import com.penguinstudios.tradeguardian.data.getFormattedBuyerDepositAmount
import com.penguinstudios.tradeguardian.data.getFormattedItemPrice
import com.penguinstudios.tradeguardian.data.getFormattedPercentFeePerParty
import com.penguinstudios.tradeguardian.data.getFormattedSellerDepositAmount
import com.penguinstudios.tradeguardian.data.getSellerDepositAmount
import com.penguinstudios.tradeguardian.data.model.ContractStatus
import com.penguinstudios.tradeguardian.data.model.Trade
import com.penguinstudios.tradeguardian.data.model.UserRole
import com.penguinstudios.tradeguardian.data.network
import com.penguinstudios.tradeguardian.data.networkTokenName
import com.penguinstudios.tradeguardian.data.userRole
import com.penguinstudios.tradeguardian.util.CustomGasProvider
import com.penguinstudios.tradeguardian.util.WalletUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TradeInfoViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val remoteRepository: RemoteRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _uiState = MutableSharedFlow<TradeInfoUIState>()
    val uiState = _uiState.asSharedFlow()
    lateinit var trade: Trade

    fun initTrade(trade: Trade) {
        this.trade = trade
    }

    fun deposit() {
        viewModelScope.launch {
            try {
                _uiState.emit(TradeInfoUIState.ShowProgressDeposit)
                //Check if user already deposited
                if (remoteRepository.hasDeposited(
                        trade.contractAddress,
                        walletRepository.credentials.address
                    )
                ) {
                    _uiState.emit(TradeInfoUIState.HideProgressDeposit)
                    _uiState.emit(TradeInfoUIState.Error("You have already deposited into the trade"))
                    return@launch
                }

                val txReceipt = if (trade.userRole == UserRole.SELLER) {
                    remoteRepository.sellerDeposit(
                        trade.contractAddress,
                        trade.getSellerDepositAmount()
                    )
                } else {
                    remoteRepository.buyerDeposit(
                        trade.contractAddress,
                        trade.getBuyerDepositAmount()
                    )
                }

                val formattedDepositAmount = if (trade.userRole == UserRole.SELLER) {
                    trade.getFormattedSellerDepositAmount()
                } else {
                    trade.getFormattedBuyerDepositAmount()
                }

                _uiState.emit(TradeInfoUIState.HideProgressDeposit)

                val gasCostEther =
                    WalletUtil.weiToEther(txReceipt.gasUsed.multiply(CustomGasProvider.GAS_PRICE))
                val formattedGasCost = "$gasCostEther ${trade.networkTokenName}"

                _uiState.emit(
                    TradeInfoUIState.SuccessDeposit(
                        //Deploying contract will return the TxReceipt with contract address
                        //However subsequent calls will return a null contract address in the TxReceipt
                        trade.contractAddress,
                        txReceipt.transactionHash,
                        formattedDepositAmount,
                        formattedGasCost
                    )
                )

            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(TradeInfoUIState.HideProgressDeposit)
                _uiState.emit(TradeInfoUIState.Error(e.message.toString()))
            }
        }
    }

    fun onCancelTrade() {
        viewModelScope.launch {
            try {
                if (System.currentTimeMillis() < trade.withdrawEligibilityDate) {
                    _uiState.emit(TradeInfoUIState.Error("Contract cancellation requires a minimum two-hour wait after creation"))
                    return@launch
                }

                if (remoteRepository.getContractStatus(trade.contractAddress) != ContractStatus.AWAITING_DEPOSIT) {
                    _uiState.emit(TradeInfoUIState.Error("Trade cannot be canceled"))
                    return@launch
                }

                val txReceipt = remoteRepository.cancelTrade(trade.contractAddress)
                localRepository.deleteTrade(trade.contractAddress)

                if (txReceipt == null) {
                    _uiState.emit(TradeInfoUIState.SuccessDeleteTradeNoReceipt(trade.contractAddress))
                } else {
                    val gasCostEther =
                        WalletUtil.weiToEther(txReceipt.gasUsed.multiply(CustomGasProvider.GAS_PRICE))
                    val formattedGasCost = "$gasCostEther ${trade.networkTokenName}"

                    val formattedAmountReturned = if (trade.userRole == UserRole.SELLER) {
                        trade.getFormattedSellerDepositAmount()
                    } else {
                        trade.getFormattedBuyerDepositAmount()
                    }

                    _uiState.emit(
                        TradeInfoUIState.SuccessDeleteWithReceipt(
                            txReceipt.transactionHash,
                            trade.contractAddress,
                            formattedAmountReturned,
                            formattedGasCost
                        )
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(TradeInfoUIState.Error(e.message.toString()))
            }
        }
    }

    fun onSettle() {

    }

    fun setTradeInfo() {
        viewModelScope.launch {
            try {
                val contractStatus = remoteRepository.getContractStatus(trade.contractAddress)
                Timber.d("Contract address: " + contractStatus.statusName)

                val hasUserDeposited = remoteRepository.hasDeposited(
                    trade.contractAddress, trade.userWalletAddress
                )

                val hasCounterPartyDeposited = remoteRepository.hasDeposited(
                    trade.contractAddress, trade.counterPartyWalletAddress
                )

                setDepositStatus(
                    contractStatus,
                    hasUserDeposited,
                    trade.userRole
                )

                setDepositStatus(
                    contractStatus,
                    hasCounterPartyDeposited,
                    trade.counterPartyRole
                )

                setDepositBtnState(contractStatus, hasUserDeposited)
                setCurrentStepIndicator(contractStatus)
                setAwaitingDeliveryBtnState(contractStatus, trade.userRole)
                setItemDeliveryStatus(contractStatus)
                setReturnDepositStatus(contractStatus)
            } catch (e: Exception) {
                Timber.e(e)
            }

        }
    }

    private suspend fun setReturnDepositStatus(contractStatus: ContractStatus) {
        if (contractStatus == ContractStatus.ITEM_RECEIVED) {
            val formattedSellerReturnAmount = trade.getFormattedAmountReturnedToSeller()
            val formattedBuyerReturnAmount = trade.getFormattedAmountReturnedToBuyer()
            val payoutStatus = "Buyer paid ${trade.getFormattedItemPrice()} to seller"
            val sellerStatus = "$formattedSellerReturnAmount returned to seller"
            val buyerStatus = "$formattedBuyerReturnAmount returned to buyer"
            val feeStatus = "-${trade.getFormattedPercentFeePerParty()} fee per party"

            _uiState.emit(TradeInfoUIState.UpdateSellerPayout(payoutStatus))
            _uiState.emit(TradeInfoUIState.UpdateSellerReturnDepositStatus(sellerStatus))
            _uiState.emit(TradeInfoUIState.UpdateBuyerReturnDepositStatus(buyerStatus))
            _uiState.emit(TradeInfoUIState.UpdateFeePerParty(feeStatus))
            _uiState.emit(TradeInfoUIState.ShowTradeStatus(true))
        }
    }

    // @formatter:off
    private suspend fun setItemDeliveryStatus(contractStatus: ContractStatus) {
        when (contractStatus) {
            ContractStatus.AWAITING_DELIVERY -> {
                _uiState.emit(TradeInfoUIState.UpdateSellerDeliveryStatus("Seller has not marked item as delivered", false))
                _uiState.emit(TradeInfoUIState.UpdateBuyerReceivedStatus("Buyer has not updated received status", false))
            }

            ContractStatus.ITEM_SENT -> {
                _uiState.emit(TradeInfoUIState.UpdateSellerDeliveryStatus("Seller has marked item as delivered", true))
                _uiState.emit(TradeInfoUIState.UpdateBuyerReceivedStatus("Buyer has not updated received status", false))
            }

            ContractStatus.ITEM_RECEIVED -> {
                _uiState.emit(TradeInfoUIState.UpdateSellerDeliveryStatus("Seller has marked item as delivered", true))
                _uiState.emit(TradeInfoUIState.UpdateBuyerReceivedStatus("Buyer has received correct item", true))
            }

            ContractStatus.ITEM_INCORRECT -> {
                _uiState.emit(TradeInfoUIState.UpdateSellerDeliveryStatus("Seller has marked item as delivered", true))
                _uiState.emit(TradeInfoUIState.IncorrectItem("Buyer has incorrect item or no item"))
                _uiState.emit(TradeInfoUIState.ShowTradeStatus(false))
            }

            else -> {}
        }
    }
    // @formatter:on

    private suspend fun setAwaitingDeliveryBtnState(
        contractStatus: ContractStatus,
        userRole: UserRole
    ) {
        when (contractStatus) {
            ContractStatus.AWAITING_DELIVERY -> {
                if (userRole == UserRole.SELLER) {
                    _uiState.emit(TradeInfoUIState.ShowSellerDeliveryBtn)
                }
            }

            ContractStatus.ITEM_SENT -> {
                if (userRole == UserRole.BUYER) {
                    _uiState.emit(TradeInfoUIState.ShowBuyerReceivedBtns)
                }
            }

            else -> {}
        }
    }

    // @formatter:off
    private suspend fun setDepositStatus(
        contractStatus: ContractStatus,
        hasDeposited: Boolean,
        userRole: UserRole
    ) {
        if (contractStatus == ContractStatus.AWAITING_DEPOSIT) {
            if (hasDeposited) {
                when (userRole) {
                    UserRole.SELLER -> {
                        val formattedSellerDepositAmount = trade.getFormattedSellerDepositAmount()
                        val depositStatus = "Seller has deposited $formattedSellerDepositAmount"
                        _uiState.emit(TradeInfoUIState.UpdateSellerDepositStatus(depositStatus, true))
                    }

                    UserRole.BUYER -> {
                        val formattedBuyerDepositAmount = trade.getFormattedBuyerDepositAmount()
                        val depositStatus = "Buyer has deposited $formattedBuyerDepositAmount"
                        _uiState.emit(TradeInfoUIState.UpdateBuyerDepositStatus(depositStatus, true))
                    }
                }
            } else {
                when (userRole) {
                    UserRole.SELLER -> {
                        _uiState.emit(TradeInfoUIState.UpdateSellerDepositStatus("Seller has not deposited funds", false))
                    }

                    UserRole.BUYER -> {
                        _uiState.emit(TradeInfoUIState.UpdateBuyerDepositStatus("Buyer has not deposited funds", false))
                    }
                }
            }

        } else {
            //Has already deposited
            when (userRole) {
                UserRole.SELLER -> {
                    val formattedSellerDepositAmount = trade.getFormattedSellerDepositAmount()
                    val depositStatus = "Seller has deposited $formattedSellerDepositAmount"
                    _uiState.emit(TradeInfoUIState.UpdateSellerDepositStatus(depositStatus, true))
                }

                UserRole.BUYER -> {
                    val formattedBuyerDepositAmount = trade.getFormattedBuyerDepositAmount()
                    val depositStatus = "Buyer has deposited $formattedBuyerDepositAmount"
                    _uiState.emit(TradeInfoUIState.UpdateBuyerDepositStatus(depositStatus, true))
                }
            }
        }
    }
    // @formatter:on

    private suspend fun setDepositBtnState(contractStatus: ContractStatus, hasDeposited: Boolean) {
        //Any other state means both users have already deposited
        if (contractStatus == ContractStatus.AWAITING_DEPOSIT) {
            if (!hasDeposited) {
                _uiState.emit(TradeInfoUIState.ShowDepositBtn)
            }
        }
    }

    private suspend fun setCurrentStepIndicator(contractStatus: ContractStatus) {
        when (contractStatus) {
            ContractStatus.AWAITING_DEPOSIT -> {
                _uiState.emit(TradeInfoUIState.SetCurrentStepIndicatorStepOne)
            }

            ContractStatus.AWAITING_DELIVERY, ContractStatus.ITEM_SENT, ContractStatus.ITEM_INCORRECT -> {
                _uiState.emit(TradeInfoUIState.SetCurrentStepIndicatorStepTwo)
            }

            ContractStatus.ITEM_RECEIVED -> {
                _uiState.emit(TradeInfoUIState.SetCurrentStepIndicatorStepThree)
            }

            ContractStatus.SETTLED -> {
                _uiState.emit(TradeInfoUIState.SetCurrentStepIndicatorStepThree)
            }

            else -> {}
        }
    }

    fun correctItemReceived() {
        viewModelScope.launch {
            try {
                _uiState.emit(TradeInfoUIState.ShowItemDeliveryProgress)
                val txReceipt = remoteRepository.correctItemReceived(trade.contractAddress)
                _uiState.emit(TradeInfoUIState.HideItemDeliveryProgress)

                val gasCostWei = txReceipt.gasUsed.multiply(CustomGasProvider.GAS_PRICE)

                _uiState.emit(
                    TradeInfoUIState.SuccessChangeDeliveryState(
                        trade.contractAddress,
                        txReceipt.transactionHash,
                        ContractStatus.ITEM_RECEIVED.statusName,
                        WalletUtil.weiToNetworkToken(gasCostWei, trade.network)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(TradeInfoUIState.HideItemDeliveryProgress)
                _uiState.emit(TradeInfoUIState.Error(e.message.toString()))
            }
        }

    }

    fun incorrectItemReceived() {
        viewModelScope.launch {
            try {
                _uiState.emit(TradeInfoUIState.ShowItemDeliveryProgress)
                val txReceipt = remoteRepository.incorrectItemReceived(trade.contractAddress)
                _uiState.emit(TradeInfoUIState.HideItemDeliveryProgress)

                val gasCostWei = txReceipt.gasUsed.multiply(CustomGasProvider.GAS_PRICE)

                _uiState.emit(
                    TradeInfoUIState.SuccessChangeDeliveryState(
                        trade.contractAddress,
                        txReceipt.transactionHash,
                        ContractStatus.ITEM_INCORRECT.statusName,
                        WalletUtil.weiToNetworkToken(gasCostWei, trade.network)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(TradeInfoUIState.HideItemDeliveryProgress)
                _uiState.emit(TradeInfoUIState.Error(e.message.toString()))
            }
        }
    }

    fun itemDelivered() {
        viewModelScope.launch {
            try {
                _uiState.emit(TradeInfoUIState.ShowItemDeliveryProgress)
                val txReceipt = remoteRepository.itemDelivered(trade.contractAddress)
                _uiState.emit(TradeInfoUIState.HideItemDeliveryProgress)

                val gasCostWei = txReceipt.gasUsed.multiply(CustomGasProvider.GAS_PRICE)

                _uiState.emit(
                    TradeInfoUIState.SuccessChangeDeliveryState(
                        trade.contractAddress,
                        txReceipt.transactionHash,
                        ContractStatus.ITEM_SENT.statusName,
                        WalletUtil.weiToNetworkToken(gasCostWei, trade.network)
                    )
                )
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(TradeInfoUIState.HideItemDeliveryProgress)
                _uiState.emit(TradeInfoUIState.Error(e.message.toString()))
            }
        }
    }
}