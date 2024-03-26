package com.penguinstudios.tradeguardian.data.usecase

import com.penguinstudios.tradeguardian.contract.Escrow
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.util.CustomGasProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.web3j.protocol.core.methods.response.TransactionReceipt
import javax.inject.Inject

class UpdateItemStateUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository
) {

    suspend fun correctItemReceived(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                CustomGasProvider()
            )
                .setBuyerHasReceivedCorrectItem()
                .sendAsync()
                .await()
        }
    }

    suspend fun incorrectItemReceived(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                CustomGasProvider()
            )
                .setBuyerHasReceivedIncorrectItem()
                .sendAsync()
                .await()
        }
    }

    suspend fun itemDelivered(contractAddress: String): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                CustomGasProvider()
            )
                .setSellerHasGivenItem()
                .sendAsync()
                .await()
        }
    }
}