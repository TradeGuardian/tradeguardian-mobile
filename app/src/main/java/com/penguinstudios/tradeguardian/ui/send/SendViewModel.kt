package com.penguinstudios.tradeguardian.ui.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableSharedFlow<SendUIState>()
    val uiState = _uiState.asSharedFlow()

    fun onSend(sendToAddress: String, itemPrice: String) {
        viewModelScope.launch {
            try {
                remoteRepository.send(sendToAddress, itemPrice)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}