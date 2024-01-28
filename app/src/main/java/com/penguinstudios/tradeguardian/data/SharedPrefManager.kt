package com.penguinstudios.tradeguardian.data

import android.content.SharedPreferences
import javax.inject.Inject

class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences){
    companion object {
        private const val WALLET_NAME = "wallet_name"
    }

    var walletName: String?
        get() = sharedPreferences.getString(WALLET_NAME, null)
        set(value) = sharedPreferences.edit().putString(WALLET_NAME, value).apply()
}