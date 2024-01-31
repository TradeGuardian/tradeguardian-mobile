package com.penguinstudios.tradeguardian.util

import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.MnemonicUtils
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

object WalletUtil {

    private const val HARDENED_BIT = 0x80000000

    // Standard Ethereum derivation path -- metamask compliant
    fun deriveKeyPairFromMnemonic(mnemonic: String): Bip32ECKeyPair {
        val derivationPath = intArrayOf(
            44 or HARDENED_BIT.toInt(),
            60 or HARDENED_BIT.toInt(),
            0 or HARDENED_BIT.toInt(),
            0,
            0
        )
        val seed = MnemonicUtils.generateSeed(mnemonic, null)
        val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
        return Bip32ECKeyPair.deriveKeyPair(masterKeyPair, derivationPath)
    }

    fun formatBalance(balance: BigInteger): String {
        val divisor = BigDecimal.TEN.pow(18)
        val result = BigDecimal(balance).divide(divisor, 5, RoundingMode.HALF_UP)
        return "$result BNB"
    }
}
