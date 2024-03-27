package com.penguinstudios.tradeguardian.ui.tradeinfo

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.DialogFragment
import com.penguinstudios.tradeguardian.data.LocalRepository
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.databinding.SuccessDeleteTradeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SuccessDeleteFragment(
    private val txHash: String,
    private val contractAddress: String,
    private val formattedAmountReturned: String,
    private val formattedGasUsed: String
) : DialogFragment() {

    @Inject
    lateinit var localRepository: LocalRepository

    private lateinit var binding: SuccessDeleteTradeFragmentBinding

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
        binding = SuccessDeleteTradeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTxHash.text = txHash
        binding.tvContractAddress.text = contractAddress
        binding.tvAmountReturned.text = formattedAmountReturned
        binding.tvGasUsed.text = formattedGasUsed
        val selectedNetwork = localRepository.getSelectedNetwork()
        binding.tvExplorerName.text = "View on ${selectedNetwork.explorerName}"

        binding.btnViewExplorer.setOnClickListener {
            val url = selectedNetwork.explorerUrl + contractAddress
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
            dismiss()
        }
    }
}