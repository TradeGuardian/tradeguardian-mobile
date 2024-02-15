package com.penguinstudios.tradeguardian.ui.trades

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.penguinstudios.tradeguardian.databinding.TradesFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TradesFragment : Fragment(), TradesAdapter.Callback {

    private lateinit var binding: TradesFragmentBinding
    private val viewModel: TradesViewModel by viewModels()
    private lateinit var tradesAdapter: TradesAdapter

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

        tradesAdapter = TradesAdapter(viewModel.trades, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tradesAdapter
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is TradesUIState.SuccessGetTrades -> {
                        binding.progress.visibility = View.GONE
                        binding.tvNoTrades.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        tradesAdapter.notifyDataSetChanged()
                    }

                    is TradesUIState.NoTrades -> {
                        binding.recyclerView.visibility = View.GONE
                        binding.progress.visibility = View.GONE
                        binding.tvNoTrades.visibility = View.VISIBLE
                    }

                    is TradesUIState.Error -> {
                        binding.progress.visibility = View.GONE
                        Toast.makeText(requireContext(), uiState.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.getTrades()

    }

    override fun onTradeClick(adapterPosition: Int) {

    }
}