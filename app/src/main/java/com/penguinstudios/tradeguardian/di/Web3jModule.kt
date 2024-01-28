package com.penguinstudios.tradeguardian.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.penguinstudios.tradeguardian.data.model.Network
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Web3jModule {

    @Provides
    @Singleton
    fun provideWeb3j(): Web3j {
        return Web3j.build(HttpService(Network.TEST_NET.baseUrl))
    }
}