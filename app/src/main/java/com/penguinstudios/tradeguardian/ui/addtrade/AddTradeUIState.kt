package com.penguinstudios.tradeguardian.ui.addtrade

sealed class AddTradeUIState {
    object SuccessAddTrade : AddTradeUIState()
    data class Error(val message: String) : AddTradeUIState()
}