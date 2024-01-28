package com.penguinstudios.tradeguardian.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.penguinstudios.tradeguardian.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeBackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_back_activity)

    }
}