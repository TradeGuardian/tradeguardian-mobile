package com.penguinstudios.tradeguardian.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.SharedPrefManager
import com.penguinstudios.tradeguardian.data.model.RemoteRepository
import com.penguinstudios.tradeguardian.util.WalletUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Timber.d("Fired")
                    val balanceResponse = withTimeout(15000L) {
                        remoteRepository.getWalletBalance()
                    }
                    val formattedWalletBalance = WalletUtil.formatBalance(balanceResponse.balance)
                    Timber.d("Formatted Wallet Balance: %s", formattedWalletBalance)
                }
            } catch (e: TimeoutCancellationException) {
                Timber.e("Timeout occurred: No response received within the specified time")
                //Display error message in wallet
            } catch (e: Exception) {
                Timber.e(e)

            }
        }
    }
}