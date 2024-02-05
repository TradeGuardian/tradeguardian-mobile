package com.penguinstudios.tradeguardian.ui.createtrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.data.model.RemoteRepository
import com.penguinstudios.tradeguardian.data.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class CreateTradeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    fun onCreateTradeClick(
        userRole: UserRole,
        counterPartyAddress: String,
        itemPrice: String,
        description: String
    ) {

    }
}