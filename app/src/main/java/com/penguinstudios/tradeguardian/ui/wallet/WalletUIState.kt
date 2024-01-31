package com.penguinstudios.tradeguardian.ui.wallet

sealed interface WalletUIState {
    data class SuccessGetBalance(val walletAddress: String, val walletBalance: String) : WalletUIState
    data class Error(val message: String) : WalletUIState
}