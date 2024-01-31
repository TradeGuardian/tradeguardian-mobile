package com.penguinstudios.tradeguardian.data

import com.penguinstudios.tradeguardian.data.CreatePasswordValidator.isSameAs
import com.penguinstudios.tradeguardian.data.CreatePasswordValidator.isValidPassword
import com.penguinstudios.tradeguardian.data.MnemonicValidator.isValidMnemonic
import com.penguinstudios.tradeguardian.util.WalletUtil
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.CipherException
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val sharedPrefManager: SharedPrefManager
) {

    lateinit var credentials: Credentials
    lateinit var password: String
    lateinit var mnemonic: String
    var mnemonicList: MutableList<String> = mutableListOf()
    var shuffledMnemonicList: MutableList<String> = mutableListOf()

    //Generates a wallet with a 12 word mnemonic that has no duplicate words
    @Throws(CipherException::class, IOException::class)
    fun generateWallet(password: String, filesDirectory: File): Bip39Wallet {
        while (true) {
            val wallet = WalletUtils.generateBip39Wallet(password, filesDirectory)
            val mnemonic = wallet.mnemonic
            val words = mnemonic.split(" ")
            val uniqueWords = words.toSet()

            if (uniqueWords.size == words.size) {
                return wallet
            }
        }
    }

    fun validateUserPasswordInput(password: String, confirmPassword: String) {
        password.isValidPassword()
        confirmPassword.isValidPassword()
        password.isSameAs(confirmPassword)
    }

    fun validateMnemonicInput(mnemonic: String){
        mnemonic.isValidMnemonic()
    }

    fun importWallet(mnemonic: String, password: String, filesDirectory: File) {
        val derivedKeypair = WalletUtil.deriveKeyPairFromMnemonic(mnemonic)
        val credentials = Credentials.create(derivedKeypair)
        val walletFileName = WalletUtils.generateWalletFile(
            password, credentials.ecKeyPair, filesDirectory, false
        )

        this.credentials = credentials
        sharedPrefManager.walletName = walletFileName
    }

    fun createWallet(password: String, filesDirectory: File) {
        val wallet = generateWallet(password, filesDirectory)
        this.mnemonic = wallet.mnemonic

        val derivedKeyPair: Bip32ECKeyPair = WalletUtil.deriveKeyPairFromMnemonic(mnemonic)
        this.credentials = Credentials.create(derivedKeyPair)

        sharedPrefManager.walletName = wallet.filename

        Timber.d("Mnemonic: " + mnemonic)
        Timber.d("Public key: " + credentials.address)

        mnemonicList = wallet.mnemonic.split(" ").toMutableList()
        shuffledMnemonicList = mnemonicList.shuffled().toMutableList()
    }
}