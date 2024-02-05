package com.penguinstudios.tradeguardian.ui.resetwallet

sealed interface ResetWalletUIState {
    object SuccessResetWallet : ResetWalletUIState
    object InvalidTextEntered : ResetWalletUIState
    object ValidTextEntered : ResetWalletUIState
}
