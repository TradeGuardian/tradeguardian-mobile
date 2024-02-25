package com.penguinstudios.tradeguardian.data

import com.penguinstudios.tradeguardian.data.model.Trade
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
    private val walletRepository: WalletRepository,
    private val appDatabase: AppDatabase
) {

    suspend fun getTrades(userWalletAddress: String): List<Trade> {
        return appDatabase.tradesDao().getTrades(userWalletAddress)
    }

    suspend fun insertTrade(trade: Trade) {
        appDatabase.tradesDao().insertTrade(trade)
    }

    suspend fun deleteTrade(contractAddress: String) {
        appDatabase.tradesDao().deleteTrade(contractAddress)
    }

    suspend fun tradeExists(contractAddress: String): Boolean {
        val userWalletAddress = walletRepository.credentials.address
        return getTrades(userWalletAddress).any { it.contractAddress == contractAddress.lowercase() }
    }
}