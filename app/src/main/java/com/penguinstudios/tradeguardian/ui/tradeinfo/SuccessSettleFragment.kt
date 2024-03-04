package com.penguinstudios.tradeguardian.ui.tradeinfo

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.DialogFragment
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.databinding.SuccessSettleFragmentBinding

class SuccessSettleFragment(
    private val contractAddress: String,
    private val title: String,
    private val settlementStatus: String,
    private val txHash: String,
    private val formattedGasCost: String
) : DialogFragment() {

    private lateinit var binding: SuccessSettleFragmentBinding

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SuccessSettleFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTitle.text = title
        binding.tvSettlementStatus.text = settlementStatus
        binding.tvTxHash.text = txHash
        binding.tvGasUsed.text = formattedGasCost

        binding.btnViewExplorer.setOnClickListener {
            val url = Network.TEST_NET.explorerUrl + contractAddress
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
            dismiss()
        }
    }
}