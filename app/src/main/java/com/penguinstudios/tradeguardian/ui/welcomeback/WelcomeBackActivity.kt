package com.penguinstudios.tradeguardian.ui.welcomeback

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.penguinstudios.tradeguardian.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class WelcomeBackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_back_activity)

    }
}