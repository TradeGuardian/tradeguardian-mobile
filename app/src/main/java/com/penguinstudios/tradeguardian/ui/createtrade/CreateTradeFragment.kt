package com.penguinstudios.tradeguardian.ui.createtrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.data.model.UserRole
import com.penguinstudios.tradeguardian.databinding.CreateTradeFragmentBinding
import com.penguinstudios.tradeguardian.databinding.LayoutSpinnerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateTradeFragment : Fragment() {

    private lateinit var binding: CreateTradeFragmentBinding
    private lateinit var spinnerBinding: LayoutSpinnerBinding
    private val viewModel: CreateTradeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateTradeFragmentBinding.inflate(inflater, container, false)
        spinnerBinding = LayoutSpinnerBinding.bind(binding.layoutSpinner.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNetworkSpinner()

        binding.tilCountPartyAddress.setEndIconOnClickListener {
            initScanner()
        }

        binding.btnCreateTrade.setOnClickListener {
            val userRole = when (binding.radioGroup.checkedRadioButtonId) {
                R.id.radio_btn_seller -> UserRole.SELLER
                R.id.radio_btn_buyer -> UserRole.BUYER
                else -> throw IllegalStateException("A user role must be selected.")
            }

            viewModel.onCreateTradeClick(
                userRole,
                binding.etCounterPartyAddress.text.toString(),
                binding.etItemPrice.text.toString(),
                binding.etDescription.text.toString()
            )
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

    private val barcodeLauncher =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val s = result.contents
                binding.etCounterPartyAddress.setText(s)
            }
        }

    private fun initScanner() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan seed phrase")
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        barcodeLauncher.launch(options)
    }
}
