package com.penguinstudios.tradeguardian.ui.importwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinstudios.tradeguardian.data.PasswordStrengthEvaluator
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.ui.createwallet.viewmodel.CreateWalletUIState
import com.penguinstudios.tradeguardian.util.WalletUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImportWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val filesDir: File
) : ViewModel() {

    private val _uiState = MutableSharedFlow<ImportWalletUIState>()
    val uiState = _uiState.asSharedFlow()

    fun onImportBtnClick(mnemonic: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                walletRepository.validateMnemonicInput(mnemonic)
                walletRepository.validateUserPasswordInput(newPassword, confirmPassword)
                walletRepository.password = newPassword
                walletRepository.importWallet(mnemonic, newPassword, filesDir)
                _uiState.emit(ImportWalletUIState.SuccessImportWallet)
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.emit(ImportWalletUIState.Error(e.message.toString()))
            }
        }
    }

    fun onNewPasswordTextChange(s: String) {
        viewModelScope.launch {
            val strength = PasswordStrengthEvaluator.evaluatePasswordStrength(s)
            _uiState.emit(ImportWalletUIState.UpdatePasswordStrength(strength))
        }
    }


}