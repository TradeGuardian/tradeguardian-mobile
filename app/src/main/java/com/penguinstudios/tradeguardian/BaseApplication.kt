package com.penguinstudios.tradeguardian

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.security.Security

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        setupBouncyCastle()
    }

    companion object {
        fun setupBouncyCastle() {
            val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            when {
                // Web3j will set up the provider lazily when it's first used.
                provider == null -> return

                // BC with same package name, shouldn't happen in real life.
                provider.javaClass == BouncyCastleProvider::class.java -> return
                else -> {
                    // Android registers its own BC provider. As it might be outdated and might not include
                    // all needed ciphers, we substitute it with a known BC bundled in the app.
                    // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
                    // of that it's possible to have another BC implementation loaded in VM.
                    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
                    Security.insertProviderAt(BouncyCastleProvider(), 1)
                }
            }
        }
    }
}