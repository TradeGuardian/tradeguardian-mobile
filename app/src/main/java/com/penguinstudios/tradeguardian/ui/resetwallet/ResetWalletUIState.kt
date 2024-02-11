package com.penguinstudios.tradeguardian.ui.resetwallet

sealed class ResetWalletUIState {
    object SuccessResetWallet : ResetWalletUIState()
    object InvalidTextEntered : ResetWalletUIState()
    object ValidTextEntered : ResetWalletUIState()
}
