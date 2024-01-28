package com.penguinstudios.tradeguardian.ui.createwallet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.penguinstudios.tradeguardian.databinding.WalletSetupActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletSetupActivity : AppCompatActivity() {

    private lateinit var binding: WalletSetupActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WalletSetupActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnCreateWallet.setOnClickListener {
            CreateWalletFragment().show(supportFragmentManager, null)
        }
    }
}