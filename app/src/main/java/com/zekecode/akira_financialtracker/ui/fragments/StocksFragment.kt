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
        // Sample data for suggestions
        val suggestions = listOf("AAPL - Apple", "GOOGL - Alphabet", "TSLA - Tesla", "EUR - Euro", "USD - US Dollar")

        // Initialize the adapter with suggestions
        suggestionsAdapter = SuggestionsAdapter(requireContext(), suggestions)

        // Set the adapter to the AutoCompleteTextView
        binding.stockSearch.setAdapter(suggestionsAdapter)

        lifecycleScope.launch {
            viewModel.stockName.collect { name ->
                binding.stockHeader.text = name
            }
        }

        lifecycleScope.launch {
            viewModel.stockPrice.collect { price ->
                binding.stockPriceChange.text = price
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
