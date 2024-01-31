package com.penguinstudios.tradeguardian.ui.trades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.penguinstudios.tradeguardian.databinding.TradesFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TradesFragment : Fragment() {

    private lateinit var binding: TradesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TradesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}