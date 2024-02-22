package com.penguinstudios.tradeguardian.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.util.WalletUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableSharedFlow<WalletUIState>()
    val uiState = _uiState.asSharedFlow()

    fun getWalletBalance() {
        viewModelScope.launch {
            try {
                val walletBalance = remoteRepository.getWalletBalance().balance
                val formattedWalletBalance =
                    WalletUtil.weiToEther(walletBalance).toString() + " BNB"

                _uiState.emit(
                    WalletUIState.SuccessGetBalance(
                        walletRepository.credentials.address, formattedWalletBalance
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Timber.e(e)
                _uiState.emit(WalletUIState.Error("Request timed out: Failed to get wallet balance"))
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(WalletUIState.Error("Failed to get wallet balance: " + e.message))
            }
        }
    }

    fun getWalletAddress(): String {
        return walletRepository.credentials.address
    }
}