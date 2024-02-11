package com.penguinstudios.tradeguardian.ui.confirmtrade

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.databinding.ConfirmCreateTradeFragmentBinding
import com.penguinstudios.tradeguardian.ui.createtrade.CreateTradeUIState
import com.penguinstudios.tradeguardian.ui.createtrade.CreateTradeViewModel
import com.penguinstudios.tradeguardian.ui.createtrade.SuccessCreateTradeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfirmTradeFragment(
    private val uiState: CreateTradeUIState.ConfirmContractDeployment
) : DialogFragment() {

    private lateinit var binding: ConfirmCreateTradeFragmentBinding
    private val viewModel: CreateTradeViewModel by viewModels({ requireActivity() })

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
        binding = ConfirmCreateTradeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            viewModel.onConfirmBtnClick()
        }

        binding.tvDeployingOn.text = uiState.contractDeployment.network.networkName
        binding.tvItemPrice.text = uiState.contractDeployment.itemPriceFormatted
        binding.tvItemPriceUsd.text = uiState.itemCostUsd
        binding.tvEstimatedGas.text = uiState.totalDeploymentGasCostEther
        binding.tvMyRole.text = uiState.contractDeployment.userRole.roleName
        binding.tvMyAddress.text = uiState.contractDeployment.userWalletAddress
        binding.tvCounterPartyRole.text = uiState.contractDeployment.counterPartyRole.roleName
        binding.tvCounterPartyAddress.text = uiState.contractDeployment.counterPartyAddress
        binding.tvDescription.text = uiState.contractDeployment.description

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is CreateTradeUIState.SuccessDeployContract -> {
                        SuccessCreateTradeFragment(uiState.txHash, uiState.contractAddress).show(
                            requireActivity().supportFragmentManager,
                            null
                        )
                        dismiss()
                    }

                    else -> {}
                }
            }
        }
    }
}