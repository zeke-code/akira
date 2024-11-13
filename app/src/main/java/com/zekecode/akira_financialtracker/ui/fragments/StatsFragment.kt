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

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.isEarningDataEmpty.observe(viewLifecycleOwner) { isDataEmpty ->
            if (isDataEmpty) {
                binding.earningChartView.visibility = View.GONE
                binding.noEarningsDataTextView.visibility = View.VISIBLE
            } else {
                binding.earningChartView.visibility = View.VISIBLE
                binding.noEarningsDataTextView.visibility = View.GONE
            }
        }

        viewModel.isExpenseDataEmpty.observe(viewLifecycleOwner) { isDataEmpty ->
            if (isDataEmpty) {
                binding.expenseChartView.visibility = View.GONE
                binding.noExpensesDataTextView.visibility = View.VISIBLE
            } else {
                binding.expenseChartView.visibility = View.VISIBLE
                binding.noExpensesDataTextView.visibility = View.GONE
            }
        }

        viewModel.expenseCategoryNames.observe(viewLifecycleOwner) { categoryNames ->
            setExpenseChartFormatter(categoryNames)
        }

        viewModel.earningCategoryNames.observe(viewLifecycleOwner) { categoryNames ->
            setEarningsChartFormatter(categoryNames)
        }
    }

    private fun setExpenseChartFormatter(categoryNames: List<String>) {
        val formatter = CartesianValueFormatter { _, x, _ ->
            categoryNames.getOrNull(x.toInt()) ?: x.toString()
        }
        binding.expenseChartView.chart?.bottomAxis = (binding.expenseChartView.chart?.bottomAxis as HorizontalAxis).copy(valueFormatter = formatter)
    }

    private fun setEarningsChartFormatter(categoryNames: List<String>) {
        val formatter = CartesianValueFormatter { _, x, _ ->
            categoryNames.getOrNull(x.toInt()) ?: x.toString()
        }
        binding.earningChartView.chart?.bottomAxis = (binding.earningChartView.chart?.bottomAxis as HorizontalAxis).copy(valueFormatter = formatter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
