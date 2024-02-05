package com.penguinstudios.tradeguardian.ui.welcomeback

sealed interface WelcomeBackUIState {
    object SuccessUnlockWallet : WelcomeBackUIState
    data class Error(val message: String) : WelcomeBackUIState
}