package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.zekecode.akira_financialtracker.databinding.FragmentStocksBinding
import com.zekecode.akira_financialtracker.ui.adapters.SuggestionsAdapter
import com.zekecode.akira_financialtracker.ui.viewmodels.StocksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class StocksFragment : Fragment() {

    private var _binding: FragmentStocksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StocksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStocksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupSuggestionsAdapter()
        setupSearchListener()
    }

    private fun setupObservers() {
        viewModel.isApiKeyValid.observe(viewLifecycleOwner) { isPresent ->
            if (isPresent) {
                showView()
                viewModel.stockName.observe(viewLifecycleOwner) { stockName ->
                    binding.stockHeader.text = stockName
                }
                viewModel.stockPrice.observe(viewLifecycleOwner) { stockPrice ->
                    binding.stockPrice.text = stockPrice
                }
                viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
                    errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                hideView()
            }
        }

        binding.stocksChartView.modelProducer = viewModel.stocksChartModelProducer
        setupChartFormatter()
    }

    private fun setupSuggestionsAdapter() {
        val suggestions = listOf("AAPL", "GOOGL", "TSLA", "EUR", "USD")
        val suggestionsAdapter = SuggestionsAdapter(requireContext(), suggestions)
        binding.stockSearch.setAdapter(suggestionsAdapter)
    }

    private fun setupSearchListener() {
        val searchQueryFlow = callbackFlow<String> {
            val textWatcher = binding.stockSearch.doAfterTextChanged { text ->
                trySend(text.toString())
            }
            awaitClose { binding.stockSearch.removeTextChangedListener(textWatcher) }
        }

        searchQueryFlow
            .debounce(600)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotEmpty()) {
                    viewModel.fetchStockData(query)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupChartFormatter() {
        binding.stocksChartView.post {
            val chart = binding.stocksChartView.chart ?: return@post
            binding.stocksChartView.chart = chart.copy(
                bottomAxis = (chart.bottomAxis as? HorizontalAxis)?.copy(
                    valueFormatter = { context, x, _ ->
                        val labels = context.model.extraStore[viewModel.dateLabels]
                        labels.getOrNull(x.toInt()) ?: x.toString()
                    },
                    itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                )
            )
        }
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
