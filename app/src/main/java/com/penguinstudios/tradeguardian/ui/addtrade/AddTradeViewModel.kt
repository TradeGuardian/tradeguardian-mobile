package com.penguinstudios.tradeguardian.ui.addtrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.LocalRepository
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.data.model.Trade
import com.penguinstudios.tradeguardian.data.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class AddTradeViewModel @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _uiState = MutableSharedFlow<AddTradeUIState>()
    val uiState = _uiState.asSharedFlow()

    fun onConfirm(contractAddress: String) {
        viewModelScope.launch {
            try {
                if (localRepository.tradeExists(contractAddress)) {
                    _uiState.emit(AddTradeUIState.Error("Trade already exists"))
                    return@launch
                }

                _uiState.emit(AddTradeUIState.ShowProgressAddTrade)

                val buyerAddress = remoteRepository.getBuyerAddress(contractAddress)
                val sellerAddress = remoteRepository.getSellerAddress(contractAddress)

                if (!remoteRepository.isUserInvolvedInTrade(contractAddress)) {
                    _uiState.emit(AddTradeUIState.Error("This trade does not belong to you"))
                    return@launch
                }

                val network = Network.TEST_NET
                val contractStatusId = remoteRepository.getContractStatus(contractAddress)
                val dateCreated = remoteRepository.getDateCreatedSeconds(contractAddress)
                val itemPriceWei = remoteRepository.getItemPriceWei(contractAddress)
                val description = remoteRepository.getDescription(contractAddress)

                val userRole: UserRole
                val counterPartyRole: UserRole
                val counterPartyAddress: String

                val userWalletAddress = walletRepository.credentials.address

                if (userWalletAddress == buyerAddress) {
                    userRole = UserRole.BUYER
                    counterPartyRole = UserRole.SELLER
                    counterPartyAddress = sellerAddress
                } else {
                    userRole = UserRole.SELLER
                    counterPartyRole = UserRole.BUYER
                    counterPartyAddress = buyerAddress
                }

                val trade = Trade.builder()
                    .network(network)
                    .contractAddress(contractAddress)
                    .contractStatus(contractStatusId)
                    .dateCreatedSeconds(dateCreated)
                    .itemPriceWei(itemPriceWei)
                    .gasCostWei(BigInteger.valueOf(-1))
                    .userRole(userRole)
                    .userWalletAddress(userWalletAddress)
                    .counterPartyRole(counterPartyRole)
                    .counterPartyWalletAddress(counterPartyAddress)
                    .description(description)
                    .build()

                localRepository.insertTrade(trade)

                _uiState.emit(AddTradeUIState.SuccessAddTrade)
            } catch (e: NoSuchMethodError) {
                Timber.e(e)
                _uiState.emit(AddTradeUIState.Error("Invalid contract address"))
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(AddTradeUIState.Error(e.message.toString()))
            } finally {
                _uiState.emit(AddTradeUIState.HideProgressAddTrade)
            }
        }
    }
}