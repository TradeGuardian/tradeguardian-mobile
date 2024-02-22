package com.penguinstudios.tradeguardian.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.databinding.LayoutSpinnerBinding
import com.penguinstudios.tradeguardian.databinding.WalletFragmentBinding
import com.penguinstudios.tradeguardian.ui.resetwallet.ResetWalletFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class WalletFragment : Fragment(), WalletPopupWindow.Callback {

    private lateinit var binding: WalletFragmentBinding
    private lateinit var spinnerBinding: LayoutSpinnerBinding
    private val viewModel: WalletViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = WalletFragmentBinding.inflate(inflater, container, false)
        spinnerBinding = LayoutSpinnerBinding.bind(binding.layoutSpinner.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNetworkSpinner()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getWalletBalance()
        }

        binding.layoutWalletBalance.btnWalletMoreOptions.setOnClickListener {
            val popupWindow = WalletPopupWindow(it, this)
            popupWindow.showAsDropDown(it, -100, 0)
        }

        binding.btnSend.setOnClickListener {

        }

        binding.btnReceive.setOnClickListener {

        }

        binding.btnExportTrades.setOnClickListener {

        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is WalletUIState.SuccessGetBalance -> {
                        binding.layoutWalletBalance.tvWalletAddress.text = uiState.walletAddress
                        binding.layoutWalletBalance.tvWalletBalance.text = uiState.walletBalance
                        binding.layoutWalletBalance.root.visibility = View.VISIBLE
                        binding.layoutProgressWalletBalance.root.visibility = View.INVISIBLE
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    is WalletUIState.Error -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initNetworkSpinner() {
        val spinnerItems = mutableListOf<String>()
        spinnerItems.add(Network.TEST_NET.networkName)

        val spinnerAdapter = ArrayAdapter(
            requireContext(), R.layout.spinner_network_drop_down, spinnerItems
        )

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_network_drop_down)
        spinnerBinding.spinnerNetwork.adapter = spinnerAdapter
    }

    override fun onClearWallet() {
        ResetWalletFragment().show(requireActivity().supportFragmentManager, null)
    }
}