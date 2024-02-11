package com.penguinstudios.tradeguardian.ui.confirmtrade

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.databinding.ConfirmCreateTradeFragmentBinding
import com.penguinstudios.tradeguardian.ui.createtrade.CreateTradeUIState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmTradeFragment(
    private val uiState: CreateTradeUIState.ConfirmContractDeployment
) : DialogFragment() {

    private lateinit var binding: ConfirmCreateTradeFragmentBinding

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

        binding.tvDeployingOn.text = uiState.contractDeployment.network.networkName
        binding.tvItemPrice.text = uiState.contractDeployment.itemPriceFormatted
        binding.tvItemPriceUsd.text = uiState.itemCostUsd
        binding.tvEstimatedGas.text = uiState.totalDeploymentGasCostEther
        binding.tvMyRole.text = uiState.contractDeployment.userRole.roleName
        binding.tvMyAddress.text = uiState.contractDeployment.userWalletAddress
        binding.tvCounterPartyRole.text = uiState.contractDeployment.counterPartyRole.roleName
        binding.tvCounterPartyAddress.text = uiState.contractDeployment.counterPartyAddress
        binding.tvDescription.text = uiState.contractDeployment.description
    }
}