package com.penguinstudios.tradeguardian.util;

import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

import java.math.BigInteger;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;
import org.web3j.tx.gas.StaticGasProvider;

public class CustomGasProvider extends StaticGasProvider {
    // Adjust the gas price to meet or exceed the network's required minimum
    public static final BigInteger GAS_LIMIT = BigInteger.valueOf(3_000_000); // Gas limit as previously adjusted
    public static final BigInteger GAS_PRICE = BigInteger.valueOf(10_000_000_000L); // Updated to 10 Gwei

    public CustomGasProvider() {
        super(GAS_PRICE, GAS_LIMIT);
    }
}







