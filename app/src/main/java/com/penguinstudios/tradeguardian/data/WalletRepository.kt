package com.penguinstudios.tradeguardian.data

import com.penguinstudios.tradeguardian.data.CreatePasswordValidator.isSameAs
import com.penguinstudios.tradeguardian.data.CreatePasswordValidator.isValidPassword
import org.web3j.crypto.Bip39Wallet
import org.web3j.crypto.CipherException
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val web3j: Web3j,
    private val sharedPrefManager: SharedPrefManager
) {

    var password: String? = null
    var mnemonic: String? = null
    var mnemonicList : MutableList<String> = mutableListOf()
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

    fun isValidUserInput(password: String, confirmPassword: String){
        password.isValidPassword()
        confirmPassword.isValidPassword()
        password.isSameAs(confirmPassword)
        this.password = password
    }

    fun createWallet(password: String, filesDirectory: File) {
        val wallet = generateWallet(password, filesDirectory)
        this.mnemonic = wallet.mnemonic
        sharedPrefManager.walletName = wallet.filename

        Timber.d("Mnemonic: " + mnemonic)

        mnemonicList = wallet.mnemonic.split(" ").toMutableList()
        shuffledMnemonicList = mnemonicList.shuffled().toMutableList()
    }

}