package com.penguinstudios.tradeguardian.data.usecase

import com.penguinstudios.tradeguardian.contract.Escrow
import com.penguinstudios.tradeguardian.data.RemoteRepository
import com.penguinstudios.tradeguardian.data.WalletRepository
import com.penguinstudios.tradeguardian.util.CustomGasProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger
import javax.inject.Inject

class DepositUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository
) {

    suspend fun buyerDeposit(
        contractAddress: String,
        depositAmountWei: BigInteger
    ): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                CustomGasProvider()
            )
                .buyerDeposit(depositAmountWei)
                .sendAsync()
                .await()
        }
    }

    suspend fun sellerDeposit(
        contractAddress: String,
        depositAmountWei: BigInteger
    ): TransactionReceipt {
        return withContext(Dispatchers.IO) {
            Escrow.load(
                contractAddress,
                remoteRepository.web3j,
                walletRepository.credentials,
                CustomGasProvider()
            )
                .sellerDeposit(depositAmountWei)
                .sendAsync()
                .await()
        }
    }
}