package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            viewModel.processExpenses(expenses)
            viewModel.processExpenseSumsByDay(expenses)
        }
        viewModel.monthlyEarnings.observe(viewLifecycleOwner) { earnings ->
            viewModel.processEarnings(earnings)
        }

        viewModel.isDataAvailable.observe(viewLifecycleOwner) { isAvailable ->
            updateUI(isAvailable)
        }

        binding.expenseChartView.modelProducer = viewModel.expenseChartModelProducer
        binding.earningChartView.modelProducer = viewModel.earningChartModelProducer
        binding.sumsChartView.modelProducer = viewModel.expenseSumsChartModelProducer

        binding.chartToggleButton.text = getString(R.string.stats_toggle_button_revenues)
        binding.chartToggleButton.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.accent_green)
        )

        setupCharts()

        binding.chartToggleButton.setOnClickListener { toggleCharts() }
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

        binding.sumsChartView.post {
            val chart = binding.sumsChartView.chart ?: return@post
            binding.sumsChartView.isHorizontalScrollBarEnabled = true
            binding.sumsChartView.chart = chart.copy(
                bottomAxis = (chart.bottomAxis as? HorizontalAxis ?: return@post).copy(
                    valueFormatter = { context, x, _ ->
                        val labels = context.model.extraStore[viewModel.dateLabelList]
                        labels.getOrNull(x.toInt()) ?: x.toString()
                    },
                    itemPlacer = HorizontalAxis.ItemPlacer.segmented()
                )
            )
        }
    }

    /**
     * Toggle between the expense and revenue charts with animations.
     */
    private fun toggleCharts() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        if (binding.expensesContainer.visibility == View.VISIBLE) {
            binding.expensesContainer.startAnimation(fadeOut)
            binding.expensesContainer.visibility = View.GONE

            binding.revenuesContainer.startAnimation(fadeIn)
            binding.revenuesContainer.visibility = View.VISIBLE
            binding.expensesHeader.text = getString(R.string.stats_monthly_expenses_header)

            binding.chartToggleButton.text = getString(R.string.stats_toggle_button_expenses)
            binding.chartToggleButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.accent_red)
            )
        } else {
            binding.revenuesContainer.startAnimation(fadeOut)
            binding.revenuesContainer.visibility = View.GONE

            binding.expensesContainer.startAnimation(fadeIn)
            binding.revenueHeader.text = getString(R.string.stats_monthly_revenue_header)
            binding.expensesContainer.visibility = View.VISIBLE

            binding.chartToggleButton.text = getString(R.string.stats_toggle_button_revenues)
            binding.chartToggleButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.accent_green)
            )
        }
    }

    /**
     * Hide the view if data is missing.
     */
    private fun updateUI(isDataAvailable: Boolean) {
        if (isDataAvailable) {
            binding.noDataTextView.visibility = View.GONE
            binding.statsCard.visibility = View.VISIBLE
            binding.chartToggleButton.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
            binding.sumsContainer.visibility = View.VISIBLE
        } else {
            binding.noDataTextView.visibility = View.VISIBLE
            binding.statsCard.visibility = View.GONE
            binding.chartToggleButton.visibility = View.GONE
            binding.divider.visibility = View.GONE
            binding.sumsContainer.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
