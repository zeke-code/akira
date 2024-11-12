package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.zekecode.akira_financialtracker.databinding.FragmentStocksBinding
import com.zekecode.akira_financialtracker.ui.adapters.SuggestionsAdapter
import com.zekecode.akira_financialtracker.ui.viewmodels.StocksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StocksFragment : Fragment() {

    private var _binding: FragmentStocksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StocksViewModel by viewModels()

    private lateinit var suggestionsAdapter: SuggestionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStocksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeApiKey()
        setupSuggestionsAdapter()
    }

    private fun observeApiKey() {
        viewModel.isApiKeyPresent.observe(viewLifecycleOwner) { isPresent ->
            if (isPresent) {
                showView()
                observeStockData()
            } else {
                hideView()
            }
        }
    }

    private fun observeStockData() {
        lifecycleScope.launch {
            combine(viewModel.stockName, viewModel.stockPrice) { name, price ->
                name to price
            }.collect { (name, price) ->
                binding.stockHeader.text = name
                binding.stockPriceChange.text = price
            }
        }
    }

    private fun setupSuggestionsAdapter() {
        val suggestions = listOf("AAPL - Apple", "GOOGL - Alphabet", "TSLA - Tesla", "EUR - Euro", "USD - US Dollar")
        suggestionsAdapter = SuggestionsAdapter(requireContext(), suggestions)
        binding.stockSearch.setAdapter(suggestionsAdapter)
    }

    private fun showView() {
        binding.contentLayout.visibility = View.VISIBLE
        binding.disabledMessage.visibility = View.GONE
    }

    private fun hideView() {
        binding.contentLayout.visibility = View.GONE
        binding.disabledMessage.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
