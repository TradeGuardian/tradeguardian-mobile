package com.penguinstudios.tradeguardian.ui.createtrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.penguinstudios.tradeguardian.R
import com.penguinstudios.tradeguardian.data.model.Network
import com.penguinstudios.tradeguardian.databinding.CreateTradeFragmentBinding
import com.penguinstudios.tradeguardian.databinding.LayoutSpinnerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateTradeFragment : Fragment() {

    private lateinit var binding: CreateTradeFragmentBinding
    private lateinit var spinnerBinding: LayoutSpinnerBinding

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
}
