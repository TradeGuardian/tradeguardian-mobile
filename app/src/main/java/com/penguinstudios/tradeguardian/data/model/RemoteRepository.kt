package com.penguinstudios.tradeguardian.data.model

import com.penguinstudios.tradeguardian.data.WalletRepository
import kotlinx.coroutines.future.await
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetBalance
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteRepository @Inject constructor(
    private val web3j: Web3j,
    private val walletRepository: WalletRepository
) {

    suspend fun getWalletBalance(): EthGetBalance {
        //val address = "0xEAaA2542CdB884fb279507Bb73b52095Aa4685A9"
        //Timber.d("Mock address balance: " + address)
        Timber.d("Credentials: " + walletRepository.credentials.address)
        return web3j.ethGetBalance(
            walletRepository.credentials.address,
            DefaultBlockParameterName.LATEST
        ).sendAsync().await()
    }
}