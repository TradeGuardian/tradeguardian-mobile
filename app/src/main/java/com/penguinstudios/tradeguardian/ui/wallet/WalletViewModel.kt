package com.penguinstudios.tradeguardian.ui.wallet

import android.app.Application
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.penguinstudios.tradeguardian.data.LocalRepository
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.util.WalletUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository,
    private val localRepository: LocalRepository
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

    fun onExportTrades() {
        viewModelScope.launch {
            try {
                localRepository.exportTrades()
                _uiState.emit(WalletUIState.SuccessExportTrade)
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(WalletUIState.Error("Failed to export trades"))
            }
        }
    }
}