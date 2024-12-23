package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.FragmentStatsBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.expenseChartView.modelProducer = viewModel.expenseChartModelProducer
        binding.earningChartView.modelProducer = viewModel.earningChartModelProducer
        binding.chartToggleButton.text = getString(R.string.stats_toggle_button_revenues)
        binding.chartToggleButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_blue))

        setupCharts()

        viewModel.isDataAvailable.observe(viewLifecycleOwner) { isDataAvailable ->
            updateUI(isDataAvailable)
        }

        binding.chartToggleButton.setOnClickListener {
            toggleCharts()
        }
    }

    private fun setupCharts() {
        binding.expenseChartView.post {
            val chart = binding.expenseChartView.chart ?: return@post
            binding.expenseChartView.isHorizontalScrollBarEnabled = true
            binding.expenseChartView.chart = chart.copy(
                bottomAxis = (chart.bottomAxis as? HorizontalAxis ?: return@post).copy(
                    valueFormatter = { context, x, _ ->
                        val labels = context.model.extraStore[viewModel.categoriesLabelList]
                        labels.getOrNull(x.toInt()) ?: x.toString()
                    },
                    itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                )
            )
        }

        binding.earningChartView.post {
            val chart = binding.earningChartView.chart ?: return@post
            binding.earningChartView.isHorizontalScrollBarEnabled = true
            binding.earningChartView.chart = chart.copy(
                bottomAxis = (chart.bottomAxis as? HorizontalAxis ?: return@post).copy(
                    valueFormatter = { context, x, _ ->
                        val labels = context.model.extraStore[viewModel.categoriesLabelList]
                        labels.getOrNull(x.toInt()) ?: x.toString()
                    },
                    itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                )
            )
        }
    }

    private fun toggleCharts() {
        if (binding.expensesLayout.visibility == View.VISIBLE) {
            binding.chartToggleButton.text = getString(R.string.stats_toggle_button_expenses)
            binding.chartToggleButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_red))
            binding.expensesLayout.visibility = View.GONE
            binding.revenuesLayout.visibility = View.VISIBLE
        } else {
            binding.chartToggleButton.text = getString(R.string.stats_toggle_button_revenues)
            binding.chartToggleButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_blue))
            binding.revenuesLayout.visibility = View.GONE
            binding.expensesLayout.visibility = View.VISIBLE
        }
    }


    private fun updateUI(isDataAvailable: Boolean) {
        binding.noDataTextView.visibility = if (isDataAvailable) View.GONE else View.VISIBLE
        val visibility = if (isDataAvailable) View.VISIBLE else View.GONE
        binding.expenseChartView.visibility = visibility
        binding.earningChartView.visibility = visibility
        binding.expensesHeader.visibility = visibility
        binding.revenueHeader.visibility = visibility
        binding.chartToggleButton.visibility = visibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
