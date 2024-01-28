package com.penguinstudios.tradeguardian.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.penguinstudios.tradeguardian.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }
}