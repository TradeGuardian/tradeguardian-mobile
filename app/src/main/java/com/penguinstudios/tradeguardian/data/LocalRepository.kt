package com.penguinstudios.tradeguardian.data

import com.penguinstudios.tradeguardian.data.model.Trade
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalRepository @Inject constructor(
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
}