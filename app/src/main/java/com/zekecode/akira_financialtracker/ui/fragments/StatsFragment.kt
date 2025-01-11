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

    private var isShowingExpenses = true
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
        binding.sumsChartView.modelProducer = viewModel.sumsChartModelProducer
        binding.categoryChartView.modelProducer = viewModel.categoryChartModelProducer

        // Initial state: show expenses, options to toggle
        binding.chartToggleButton.text = getString(R.string.stats_toggle_button_revenues)
        binding.categorySumsHeader.text = getString(R.string.stats_monthly_expenses_header)
        binding.chartToggleButton.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.accent_green)
        )

        // Add initial state for sums chart
        binding.sumsToggleButton.text = getString(R.string.stats_toggle_button_revenues)
        binding.sumsChartHeader.text = getString(R.string.stats_general_expenses_header)
        binding.sumsToggleButton.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.accent_green)
        )

        binding.chartToggleButton.setOnClickListener {
            toggleCategoryChart()
        }

        binding.sumsToggleButton.setOnClickListener {
            toggleSumsChart()
        }

        setupObservers()
        setupCharts()
    }

    private fun setupObservers() {
        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            viewModel.processExpenses(expenses)
            viewModel.processExpenseSumsByDay(expenses)
        }
        viewModel.monthlyEarnings.observe(viewLifecycleOwner) { earnings ->
            viewModel.processEarnings(earnings)
            viewModel.processEarningsSumsByDay(earnings)
        }

        viewModel.isDataAvailable.observe(viewLifecycleOwner) { isAvailable ->
            updateUI(isAvailable)
        }
    }

    /**
     * Setup chart formatters
     **/
    private fun setupCharts() {
        binding.categoryChartView.post {
            val chart = binding.categoryChartView.chart ?: return@post
            binding.categoryChartView.isHorizontalScrollBarEnabled = true
            binding.categoryChartView.chart = chart.copy(
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
     * Toggle between expense and revenue data in the same chart, with fade animations on the header.
     */
    private fun toggleCategoryChart() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        val isShowingExpenses =
            binding.categorySumsHeader.text == getString(R.string.stats_monthly_expenses_header)

        if (isShowingExpenses) {
            // Animate out the current text
            binding.categorySumsHeader.startAnimation(fadeOut)
            binding.categorySumsHeader.postOnAnimation {
                binding.categorySumsHeader.text = getString(R.string.stats_monthly_revenue_header)
                binding.categorySumsHeader.startAnimation(fadeIn)
            }

            // Update the chart data to show revenues
            viewModel.updateCategoryChart(showRevenues = true)

            binding.chartToggleButton.text = getString(R.string.stats_toggle_button_expenses)
            binding.chartToggleButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.accent_red)
            )
        } else {
            // Animate out the current text
            binding.categorySumsHeader.startAnimation(fadeOut)
            binding.categorySumsHeader.postOnAnimation {
                binding.categorySumsHeader.text = getString(R.string.stats_monthly_expenses_header)
                binding.categorySumsHeader.startAnimation(fadeIn)
            }

            // Update the chart data to show expenses
            viewModel.updateCategoryChart(showRevenues = false)

            binding.chartToggleButton.text = getString(R.string.stats_toggle_button_revenues)
            binding.chartToggleButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.accent_green)
            )
        }
    }

    private fun toggleSumsChart() {
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)

        isShowingExpenses = !isShowingExpenses  // Toggle the state

        if (!isShowingExpenses) {
            binding.sumsChartHeader.startAnimation(fadeOut)
            binding.sumsChartHeader.postOnAnimation {
                binding.sumsChartHeader.text = getString(R.string.stats_general_revenue_header)
                binding.sumsChartHeader.startAnimation(fadeIn)
            }

            viewModel.updateSumsChart(showRevenues = true)

            binding.sumsToggleButton.text = getString(R.string.stats_toggle_button_expenses)
            binding.sumsToggleButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.accent_red)
            )
        } else {  // Showing expenses
            binding.sumsChartHeader.startAnimation(fadeOut)
            binding.sumsChartHeader.postOnAnimation {
                binding.sumsChartHeader.text = getString(R.string.stats_general_expenses_header)
                binding.sumsChartHeader.startAnimation(fadeIn)
            }

            viewModel.updateSumsChart(showRevenues = false)

            binding.sumsToggleButton.text = getString(R.string.stats_toggle_button_revenues)
            binding.sumsToggleButton.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.accent_green)
            )
        }
    }

    /**
     * Show or hide the main UI based on availability of both expense and revenue data.
     */
    private fun updateUI(isDataAvailable: Boolean) {
        if (isDataAvailable) {
            binding.noDataTextView.visibility = View.GONE
            binding.statsCard.visibility = View.VISIBLE
            binding.chartToggleButton.visibility = View.VISIBLE
            binding.sumsToggleButton.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
            binding.sumsContainer.visibility = View.VISIBLE
        } else {
            binding.noDataTextView.visibility = View.VISIBLE
            binding.statsCard.visibility = View.GONE
            binding.chartToggleButton.visibility = View.GONE
            binding.sumsToggleButton.visibility = View.GONE
            binding.divider.visibility = View.GONE
            binding.sumsContainer.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}