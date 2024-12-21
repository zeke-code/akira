package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
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

        // fix the expense chart’s formatter:
        binding.expenseChartView.post {
            val chart = binding.expenseChartView.chart ?: return@post
            binding.expenseChartView.chart = chart.copy(
                bottomAxis = (chart.bottomAxis as? HorizontalAxis ?: return@post).copy(
                    valueFormatter = { context, x, _ ->
                        // get the list of labels from the extraStore:
                        val labels = context.model.extraStore[viewModel.labelListKey]
                        // safety check the index, fallback to x.toString() if not in range:
                        labels.getOrNull(x.toInt()) ?: x.toString()
                    }
                )
            )
        }

        // fix the earning chart’s formatter:
        binding.earningChartView.post {
            val chart = binding.earningChartView.chart ?: return@post
            binding.earningChartView.chart = chart.copy(
                bottomAxis = (chart.bottomAxis as? HorizontalAxis ?: return@post).copy(
                    valueFormatter = { context, x, _ ->
                        val labels = context.model.extraStore[viewModel.labelListKey]
                        labels.getOrNull(x.toInt()) ?: x.toString()
                    }
                )
            )
        }

        // 2) Observe expenseData to hide/show “no data” text.
        viewModel.expenseData.observe(viewLifecycleOwner) { (categoryNames, _) ->
            val isDataEmpty = categoryNames.isEmpty()
            handleChartVisibility(
                isDataEmpty = isDataEmpty,
                chartView = binding.expenseChartView,
                noDataTextView = binding.noExpensesDataTextView
            )
        }

        // 3) Observe earningData to hide/show “no data” text.
        viewModel.earningData.observe(viewLifecycleOwner) { (categoryNames, _) ->
            val isDataEmpty = categoryNames.isEmpty()
            handleChartVisibility(
                isDataEmpty = isDataEmpty,
                chartView = binding.earningChartView,
                noDataTextView = binding.noEarningsDataTextView
            )
        }
    }

    /**
     * A helper to toggle between chart vs. "no data" text.
     */
    private fun handleChartVisibility(
        isDataEmpty: Boolean,
        chartView: View,
        noDataTextView: View
    ) {
        chartView.visibility = if (isDataEmpty) View.GONE else View.VISIBLE
        noDataTextView.visibility = if (isDataEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
