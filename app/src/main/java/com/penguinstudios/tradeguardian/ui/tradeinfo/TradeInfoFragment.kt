package com.penguinstudios.tradeguardian.ui.tradeinfo

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.data.counterPartyRole
import com.penguinstudios.tradeguardian.data.getFormattedItemPrice
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.data.model.Trade
import com.penguinstudios.tradeguardian.data.networkName
import com.penguinstudios.tradeguardian.data.userRole
import com.penguinstudios.tradeguardian.databinding.TradeInfoFragmentBinding
import com.penguinstudios.tradeguardian.ui.trades.TradesViewModel
import com.penguinstudios.tradeguardian.util.ClipboardUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TradeInfoFragment(
    private val trade: Trade
) : DialogFragment() {

    private lateinit var binding: TradeInfoFragmentBinding
    private val tradeInfoViewModel: TradeInfoViewModel by activityViewModels()
    private val tradesViewModel: TradesViewModel by activityViewModels()
    private var progressDeposit: AlertDialog? = null
    private var progressDelivery: AlertDialog? = null
    private var progressDeleteTrade: AlertDialog? = null

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
        binding = TradeInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tradeInfoViewModel.initTrade(trade)

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.tvContractAddress.text = trade.contractAddress
        binding.tvDeployedOn.text = tradeInfoViewModel.trade.networkName
        binding.tvItemPrice.text = tradeInfoViewModel.trade.getFormattedItemPrice()
        binding.tvUserRole.text = tradeInfoViewModel.trade.userRole.roleName
        binding.tvUserWalletAddress.text = trade.userWalletAddress
        binding.tvCounterPartyRole.text = tradeInfoViewModel.trade.counterPartyRole.roleName
        binding.tvCounterpartyWalletAddress.text = trade.counterPartyWalletAddress
        binding.tvDescription.text = trade.description

        binding.btnDeposit.setOnClickListener {
            ConfirmDepositFragment().show(requireActivity().supportFragmentManager, null)
        }

        binding.btnItemReceived.setOnClickListener {
            ConfirmItemReceivedFragment(true).show(requireActivity().supportFragmentManager, null)
        }

        binding.btnIncorrectItem.setOnClickListener {
            ConfirmItemReceivedFragment(false).show(requireActivity().supportFragmentManager, null)
        }

        binding.btnItemDelivered.setOnClickListener {
            ConfirmItemDeliveredFragment().show(requireActivity().supportFragmentManager, null)
        }

        binding.btnRequestSettle.setOnClickListener {
            ConfirmRequestSettleFragment().show(requireActivity().supportFragmentManager, null)
        }

        binding.btnViewOnExplorer.setOnClickListener {
            openBrowser()
        }

        binding.btnCancelTrade.setOnClickListener {
            ConfirmCancelTradeFragment().show(requireActivity().supportFragmentManager, null)
        }

        binding.btnCopyContractAddress.setOnClickListener {
            Toast.makeText(requireContext(), "Copied contract address", Toast.LENGTH_SHORT).show()
            ClipboardUtil.copyText(requireContext(), trade.contractAddress)
        }

        lifecycleScope.launch {
            tradeInfoViewModel.uiState.collect { uiState ->
                when (uiState) {
                    is TradeInfoUIState.SuccessDeposit -> {
                        SuccessDepositFragment(
                            uiState.contractAddress,
                            uiState.txHash,
                            uiState.formattedDepositAmount,
                            uiState.formattedGasUsed
                        ).show(requireActivity().supportFragmentManager, null)
                    }

                    is TradeInfoUIState.SuccessChangeDeliveryState -> {
                        SuccessChangeDeliveryStateFragment(
                            uiState.contractAddress,
                            uiState.txHash,
                            uiState.formattedDeliveryState,
                            uiState.formattedGasUsed
                        ).show(requireActivity().supportFragmentManager, null)
                    }

                    is TradeInfoUIState.ShowProgressDeposit -> {
                        showProgressDeposit()
                    }

                    is TradeInfoUIState.HideProgressDeposit -> {
                        hideProgressDeposit()
                    }

                    is TradeInfoUIState.ShowItemDeliveryProgress -> {
                        showProgressDelivery()
                    }

                    is TradeInfoUIState.HideItemDeliveryProgress -> {
                        hideProgressDelivery()
                    }

                    is TradeInfoUIState.ShowCancelingTradeProgress -> {
                        showProgressCancelTrade()
                    }

                    is TradeInfoUIState.HideCancelingTradeProgress -> {
                        hideProgressCancelTrade()
                    }

                    is TradeInfoUIState.UpdateBuyerDepositStatus -> {
                        binding.tvBuyerDepositStatus.text = uiState.status
                        if (uiState.hasDeposited) {
                            binding.tvBuyerDepositStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green_400
                                )
                            )
                        }
                    }

                    is TradeInfoUIState.UpdateSellerDepositStatus -> {
                        binding.tvSellerDepositStatus.text = uiState.status
                        if (uiState.hasDeposited) {
                            binding.tvSellerDepositStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green_400
                                )
                            )
                        }
                    }

                    is TradeInfoUIState.ShowDepositBtn -> {
                        binding.btnDeposit.visibility = View.VISIBLE
                    }

                    is TradeInfoUIState.Error -> {
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                    }

                    is TradeInfoUIState.SetCurrentStepIndicatorStepOne -> {
                        setCurrentStepIndicator(1)
                    }

                    is TradeInfoUIState.SetCurrentStepIndicatorStepTwo -> {
                        setCurrentStepIndicator(2)
                    }

                    is TradeInfoUIState.SetCurrentStepIndicatorStepThree -> {
                        setCurrentStepIndicator(3)
                    }

                    is TradeInfoUIState.SuccessDeleteTradeNoReceipt -> {
                        tradesViewModel.removeTradeFromList(uiState.contractAddress)
                        Toast.makeText(
                            requireContext(),
                            "Successfully canceled trade",
                            Toast.LENGTH_SHORT
                        ).show()
                        dismiss()
                    }

                    is TradeInfoUIState.SuccessDeleteWithReceipt -> {
                        tradesViewModel.removeTradeFromList(uiState.contractAddress)
                        SuccessDeleteFragment(
                            uiState.txHash,
                            uiState.contractAddress,
                            uiState.formattedAmountReturned,
                            uiState.formattedGasUsed
                        ).show(requireActivity().supportFragmentManager, null)
                        dismiss()
                    }

                    is TradeInfoUIState.ShowBuyerReceivedBtns -> {
                        binding.btnItemReceived.visibility = View.VISIBLE
                        binding.btnIncorrectItem.visibility = View.VISIBLE
                    }

                    is TradeInfoUIState.ShowSellerDeliveryBtn -> {
                        binding.btnItemDelivered.visibility = View.VISIBLE
                    }

                    is TradeInfoUIState.IncorrectItem -> {
                        binding.tvBuyerReceivedStatus.text = uiState.status
                        binding.tvBuyerReceivedStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.red_400
                            )
                        )
                    }

                    is TradeInfoUIState.UpdateBuyerReceivedStatus -> {
                        binding.tvBuyerReceivedStatus.text = uiState.status
                        if (uiState.isDelivered) {
                            binding.tvBuyerReceivedStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green_400
                                )
                            )
                        }
                    }

                    is TradeInfoUIState.UpdateSellerDeliveryStatus -> {
                        binding.tvSellerDeliveryStatus.text = uiState.status
                        if (uiState.isDelivered) {
                            binding.tvSellerDeliveryStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green_400
                                )
                            )
                        }
                    }

                    is TradeInfoUIState.UpdateSellerReturnDepositStatus -> {
                        binding.tvReturnSellerStatus.text = uiState.status
                        binding.tvReturnSellerStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.green_400
                            )
                        )
                    }

                    is TradeInfoUIState.UpdateBuyerReturnDepositStatus -> {
                        binding.tvReturnBuyerStatus.text = uiState.status
                        binding.tvReturnBuyerStatus.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.green_400
                            )
                        )
                    }

                    is TradeInfoUIState.UpdateSellerPayout -> {
                        binding.tvAmountPayedToSeller.text = uiState.status
                        binding.tvAmountPayedToSeller.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.green_400
                            )
                        )
                    }

                    is TradeInfoUIState.UpdateFeePerParty -> {
                        binding.tvFeesPerParty.text = uiState.status
                        binding.tvFeesPerParty.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.default_text_color
                            )
                        )
                    }

                    is TradeInfoUIState.ShowTradeStatus -> {
                        Timber.d("Show trade status fired")
                        binding.tvTradeStatus.visibility = View.VISIBLE
                        if (uiState.isTradeSuccessful) {
                            binding.tvTradeStatus.text = "Trade Successful"
                            binding.tvTradeStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green_400
                                )
                            )
                        } else {
                            binding.tvTradeStatus.text = "Deposits Locked"
                            binding.tvTradeStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red_400
                                )
                            )
                        }
                    }

                    else -> {}
                }
            }
        }

        tradeInfoViewModel.setTradeInfo()
    }

    // @formatter:off
    private fun setCurrentStepIndicator(step: Int) {
        // Common actions for all steps
        binding.tvStaticAwaitingDeposit.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.layoutAwaitingDeposit.visibility = View.VISIBLE

        when (step) {
            1 -> {
                binding.circle1.setBackgroundResource(R.drawable.circle_holo_purple)
                binding.circle1Text.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            }
            2 -> {
                fillIndicatorOne()
                setupStepTwo()
            }
            3 -> {
                fillIndicatorOne()
                setupStepTwo()
                fillIndicatorTwo()
                binding.line2.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
                binding.circle3.setBackgroundResource(R.drawable.circle_filled_purple)
                binding.circle3Text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.tvStaticReturnDeposits.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.layoutReturnDeposits.visibility = View.VISIBLE
            }
        }
    }

    private fun setupStepTwo() {
        binding.circle1.setBackgroundResource(R.drawable.circle_filled_purple)
        binding.circle1Text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.line1.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        binding.tvStaticAwaitingItemDelivery.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.circle2.setBackgroundResource(R.drawable.circle_holo_purple)
        binding.circle2Text.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        binding.layoutAwaitingItem.visibility = View.VISIBLE
    }
    // @formatter:on

    private fun fillIndicatorOne() {
        binding.circle1.setBackgroundResource(R.drawable.circle_filled_purple)
        binding.circle1Text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun fillIndicatorTwo() {
        binding.circle2.setBackgroundResource(R.drawable.circle_filled_purple)
        binding.circle2Text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    private fun showProgressDeposit() {
        if (progressDeposit == null) {
            val builder = AlertDialog.Builder(requireContext(), R.style.alertDialogTheme)
            builder.setView(R.layout.progress_deposit)
            progressDeposit = builder.create().apply {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
        }
        progressDeposit?.show()
    }

    private fun hideProgressDeposit() {
        progressDeposit?.hide()
    }

    private fun showProgressDelivery() {
        if (progressDelivery == null) {
            val builder = AlertDialog.Builder(requireContext(), R.style.alertDialogTheme)
            builder.setView(R.layout.progress_delivery)
            progressDelivery = builder.create().apply {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
        }
        progressDelivery?.show()
    }

    private fun hideProgressDelivery() {
        progressDelivery?.hide()
    }

    private fun showProgressCancelTrade() {
        if (progressDeleteTrade == null) {
            val builder = AlertDialog.Builder(requireContext(), R.style.alertDialogTheme)
            builder.setView(R.layout.progress_delete_trade)
            progressDeleteTrade = builder.create().apply {
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
        }
        progressDeleteTrade?.show()
    }

    private fun hideProgressCancelTrade() {
        progressDeleteTrade?.hide()
    }

    private fun openBrowser() {
        val url = Network.getNetworkById(trade.networkId).explorerUrl + trade.contractAddress
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }
}