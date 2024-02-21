package com.penguinstudios.tradeguardian.util

import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

class CustomGasProvider : StaticGasProvider(GAS_PRICE, GAS_LIMIT) {
    companion object {
        val GAS_LIMIT: BigInteger = BigInteger.valueOf(3_000_000) //minimum amount required
        val GAS_PRICE: BigInteger = BigInteger.valueOf(10_000_000_000L)
    }
}