package com.penguinstudios.tradeguardian.data.model

import com.penguinstudios.tradeguardian.data.WalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetBalance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteRepository @Inject constructor(
    private val web3j: Web3j,
    private val walletRepository: WalletRepository
) {

    suspend fun getWalletBalance(): EthGetBalance {
        return web3j.ethGetBalance(
            //walletRepository.credentials.address,
            walletRepository.mockWalletAddress(),
            DefaultBlockParameterName.LATEST
        ).sendAsync().await()
    }

    suspend fun mockGetWalletBalanceError(): EthGetBalance {
        throw Exception("Mock error")
    }

    suspend fun mockTimeOut(): EthGetBalance {
        delay(20000)
        throw Exception("Mock error")
    }
}