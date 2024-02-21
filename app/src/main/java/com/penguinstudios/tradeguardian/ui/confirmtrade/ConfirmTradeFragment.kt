package com.penguinstudios.tradeguardian.ui.confirmtrade

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.data.model.ContractDeployment
import com.penguinstudios.tradeguardian.databinding.ConfirmCreateTradeFragmentBinding
import com.penguinstudios.tradeguardian.ui.createtrade.CreateTradeUIState
import com.penguinstudios.tradeguardian.ui.createtrade.CreateTradeViewModel
import com.penguinstudios.tradeguardian.ui.createtrade.SuccessCreateTradeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfirmTradeFragment(
    private val contractDeployment: ContractDeployment
) : DialogFragment() {

    private lateinit var binding: ConfirmCreateTradeFragmentBinding
    private val viewModel: CreateTradeViewModel by viewModels({ requireActivity() })
    private lateinit var progressCreateContract: AlertDialog

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

        binding.tvDeployingOn.text = contractDeployment.network.networkName
        binding.tvItemPrice.text = contractDeployment.itemPriceFormatted

        binding.tvUserRole.text = contractDeployment.userRole.roleName
        binding.tvUserWalletAddress.text = contractDeployment.userWalletAddress
        binding.tvCounterPartyRole.text = contractDeployment.counterPartyRole.roleName
        binding.tvCounterpartyWalletAddress.text = contractDeployment.counterPartyAddress
        binding.tvDescription.text = contractDeployment.description

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is CreateTradeUIState.SuccessDeployContract -> {
                        SuccessCreateTradeFragment(
                            uiState.txHash,
                            uiState.contractAddress,
                            uiState.formattedGasUsed
                        ).show(
                            requireActivity().supportFragmentManager,
                            null
                        )
                        dismiss()
                    }

                    is CreateTradeUIState.SuccessGetDeploymentCosts -> {
                        binding.tvItemPriceUsd.text = uiState.itemCostUsd
                        binding.tvEstimatedGasEther.text = uiState.totalDeploymentGasCostEther
                        binding.tvEstimatedGasUsd.text = uiState.totalDeploymentGasCostUsd
                        binding.tvItemPriceUsd.visibility = View.VISIBLE
                        binding.tvEstimatedGasEther.visibility = View.VISIBLE
                        binding.tvEstimatedGasUsd.visibility = View.VISIBLE
                        binding.progressEstimatedGas.visibility = View.GONE
                        binding.progressItemPriceUsd.visibility = View.GONE
                    }

                    is CreateTradeUIState.FailedToGetExchangeRate -> {
                        binding.progressEstimatedGas.visibility = View.GONE
                        binding.progressItemPriceUsd.visibility = View.GONE

                        binding.tvEstimatedGasEther.text = uiState.totalDeploymentGasCostEther
                        binding.tvEstimatedGasEther.visibility = View.VISIBLE

                        binding.tvItemPriceUsd.apply {
                            text = "Failed to get item price USD"
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.red_400))
                            visibility = View.VISIBLE
                        }

                        binding.tvEstimatedGasUsd.apply {
                            text = "Failed to get estimated gas USD"
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.red_400))
                            visibility = View.VISIBLE
                        }
                    }

                    is CreateTradeUIState.FailedToGetGasData -> {
                        binding.progressEstimatedGas.visibility = View.GONE
                        binding.progressItemPriceUsd.visibility = View.GONE

                        binding.tvItemPriceUsd.apply {
                            text = "Failed to get item price USD"
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.red_400))
                            visibility = View.VISIBLE
                        }

                        binding.tvEstimatedGasEther.apply {
                            text = "Failed to get gas estimate"
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.red_400))
                            visibility = View.VISIBLE
                        }

                        binding.tvEstimatedGasUsd.apply {
                            visibility = View.INVISIBLE
                        }
                    }

                    is CreateTradeUIState.ShowDeployContractProgress -> {
                        showProgressDialog()
                    }

                    is CreateTradeUIState.HideDeployContractProgress -> {
                        hideProgressDialog()
                    }

                    else -> {}
                }
            }
        }

        viewModel.getDeploymentCosts()
    }

    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.alertDialogTheme)
        builder.setView(R.layout.progress_create_contract)
        progressCreateContract = builder.create().apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            show()
        }
    }

    private fun hideProgressDialog() {
        progressCreateContract.hide()
    }
}