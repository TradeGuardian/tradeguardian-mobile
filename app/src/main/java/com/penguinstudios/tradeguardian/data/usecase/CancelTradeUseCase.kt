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

class CancelTradeUseCase @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val walletRepository: WalletRepository,
    private val contractInfoUseCase: ContractInfoUseCase
) {

    suspend fun cancelTrade(contractAddress: String): TransactionReceipt? {
        return if (
            contractInfoUseCase.getContractUserBalance(
                contractAddress,
                walletRepository.credentials.address
            )
            != BigInteger.ZERO
        ) {
            withContext(Dispatchers.IO) {
                Escrow.load(
                    contractAddress,
                    remoteRepository.web3j,
                    walletRepository.credentials,
                    CustomGasProvider()
                )
                    .withdraw()
                    .sendAsync()
                    .await()
            }
        } else {
            null
        }
    }
}