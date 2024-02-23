package com.penguinstudios.tradeguardian.ui.tradeinfo

sealed class TradeInfoUIState {
    data class SuccessDeposit(
        val contractAddress: String,
        val txHash: String,
        val formattedDepositAmount: String,
        val formattedGasUsed: String
    ) : TradeInfoUIState()

    object ShowProgressDeposit : TradeInfoUIState()
    object HideProgressDeposit : TradeInfoUIState()

    data class SuccessChangeDeliveryState(
        val contractAddress: String,
        val txHash: String,
        val formattedDeliveryState: String,
        val formattedGasUsed: String
    ) : TradeInfoUIState()

    object ShowItemDeliveryProgress : TradeInfoUIState()
    object HideItemDeliveryProgress : TradeInfoUIState()

    data class UpdateSellerDepositStatus(val status: String, val hasDeposited: Boolean) :
        TradeInfoUIState()

    data class UpdateBuyerDepositStatus(val status: String, val hasDeposited: Boolean) :
        TradeInfoUIState()

    object ShowDepositBtn : TradeInfoUIState()

    data class UpdateSellerDeliveryStatus(val status: String, val isDelivered: Boolean) :
        TradeInfoUIState()

    data class UpdateBuyerReceivedStatus(val status: String, val isDelivered: Boolean) :
        TradeInfoUIState()

    data class ShowTradeStatus(val isTradeSuccessful: Boolean) : TradeInfoUIState()

    object ShowBuyerReceivedBtns : TradeInfoUIState()
    object ShowSellerDeliveryBtn : TradeInfoUIState()

    data class UpdateSellerPayout(val status: String) : TradeInfoUIState()
    data class UpdateSellerReturnDepositStatus(val status: String) : TradeInfoUIState()
    data class UpdateBuyerReturnDepositStatus(val status: String) : TradeInfoUIState()
    data class UpdateFeePerParty(val status: String) : TradeInfoUIState()

    object SetCurrentStepIndicatorStepOne : TradeInfoUIState()
    object SetCurrentStepIndicatorStepTwo : TradeInfoUIState()
    object SetCurrentStepIndicatorStepThree : TradeInfoUIState()

    data class IncorrectItem(val status: String) : TradeInfoUIState()

    data class SuccessDeleteTradeNoReceipt(val contractAddress: String) : TradeInfoUIState()

    data class SuccessDeleteWithReceipt(
        val txHash: String,
        val contractAddress: String,
        val formattedAmountReturned: String,
        val formattedGasUsed: String
    ) : TradeInfoUIState()

    data class Error(val message: String) : TradeInfoUIState()
}