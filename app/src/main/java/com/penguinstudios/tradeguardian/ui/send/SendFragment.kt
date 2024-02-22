package com.penguinstudios.tradeguardian.ui.send

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.databinding.AddTradeFragmentBinding
import com.penguinstudios.tradeguardian.databinding.LayoutSpinnerBinding
import com.penguinstudios.tradeguardian.databinding.SendFragmentBinding
import com.penguinstudios.tradeguardian.ui.trades.TradesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SendFragment : DialogFragment() {

    private lateinit var binding: SendFragmentBinding
    private lateinit var spinnerBinding: LayoutSpinnerBinding

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
        binding = SendFragmentBinding.inflate(inflater, container, false)
        spinnerBinding = LayoutSpinnerBinding.bind(binding.layoutSpinner.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.tilSendToAddress.setEndIconOnClickListener {
            initScanner()
        }

        initSpinner()
    }

    private fun initSpinner() {
        val spinnerItems = mutableListOf<String>()
        spinnerItems.add(Network.TEST_NET.networkName)

        val spinnerAdapter = ArrayAdapter(
            requireContext(), R.layout.spinner_network_drop_down, spinnerItems
        )

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_network_drop_down)
        spinnerBinding.spinnerNetwork.adapter = spinnerAdapter
    }

    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val s = result.contents
                binding.etSendToAddress.setText(s)
            }
        }

    private fun initScanner() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan wallet address")
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        barcodeLauncher.launch(options)
    }
}