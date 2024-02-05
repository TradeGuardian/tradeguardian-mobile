package com.penguinstudios.tradeguardian.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.SharedPrefManager
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.data.model.RemoteRepository
import com.penguinstudios.tradeguardian.util.WalletUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _uiState = MutableSharedFlow<WalletUIState>()
    val uiState = _uiState.asSharedFlow()

    init {
        getWalletBalance()
    }

    fun getWalletBalance(){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val balanceResponse = withTimeout(15000L) {
                        remoteRepository.getWalletBalance()
                    }
                    val formattedWalletBalance = WalletUtil.formatBalance(balanceResponse.balance)
                    Timber.d("Formatted wallet balance: " + formattedWalletBalance)
                    _uiState.emit(
                        WalletUIState.SuccessGetBalance(
                            walletRepository.credentials.address, formattedWalletBalance
                        )
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Timber.e(e)
                _uiState.emit(WalletUIState.Error("Request timed out: Failed to get wallet balance"))
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(WalletUIState.Error("Failed to get wallet balance: " + e.message))
            }
        }
    }
}