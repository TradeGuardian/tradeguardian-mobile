package com.penguinstudios.tradeguardian.ui.createwallet

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.databinding.CreateWalletFragmentBinding
import com.penguinstudios.tradeguardian.ui.MainActivity
import com.penguinstudios.tradeguardian.ui.createwallet.adapters.CreateWalletPagerAdapter
import com.penguinstudios.tradeguardian.ui.createwallet.viewmodel.CreateWalletUIState
import com.penguinstudios.tradeguardian.ui.createwallet.viewmodel.CreateWalletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateWalletFragment : DialogFragment() {

    private lateinit var binding: CreateWalletFragmentBinding
    private lateinit var viewModel: CreateWalletViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.Theme_TradeGuardian)
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.attributes?.windowAnimations = R.style.FragmentSlideUpAnim
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateWalletFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.stepIndicatorView.setActiveStep(0)

        binding.viewpager2.adapter = CreateWalletPagerAdapter(this)

        viewModel = ViewModelProvider(this).get(CreateWalletViewModel::class.java)
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is CreateWalletUIState.SuccessCreateWallet -> {
                        binding.stepIndicatorView.setActiveStep(1)
                        binding.viewpager2.currentItem = 1
                    }

                    is CreateWalletUIState.SecureWalletComplete -> {
                        binding.stepIndicatorView.setActiveStep(2)
                        binding.viewpager2.currentItem = 2
                    }

                    is CreateWalletUIState.Error -> {
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                    }

                    is CreateWalletUIState.CompleteBackup -> {
                        dismiss()
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                    }

                    else -> {}
                }
            }
        }
    }
}