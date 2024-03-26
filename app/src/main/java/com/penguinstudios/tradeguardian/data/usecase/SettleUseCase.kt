package com.penguinstudios.tradeguardian.data.usecase

import com.penguinstudios.tradeguardian.contract.Escrow
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.util.CustomGasProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.gas.DefaultGasProvider
import javax.inject.Inject

class SettleUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository
) {

    suspend fun settle(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                CustomGasProvider()
            )
                .settle()
                .sendAsync()
                .await()
        }
    }

    suspend fun hasBuyerSettled(contractAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                DefaultGasProvider()
            )
                .hasBuyerSettled()
                .sendAsync()
                .await()
        }
    }

    suspend fun hasSellerSettled(contractAddress: String): Boolean {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                DefaultGasProvider()
            )
                .hasSellerSettled()
                .sendAsync()
                .await()
        }
    }
}