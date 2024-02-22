package com.penguinstudios.tradeguardian.ui.send

sealed class SendUIState {
    data class SuccessfulSend(
        private val txHash: String,
        private val sentToAddress: String,
        private val formattedAmountSent: String,
        private val formattedGasUsed: String
    ) : SendUIState()

    data class Error(val message: String) : SendUIState()
}