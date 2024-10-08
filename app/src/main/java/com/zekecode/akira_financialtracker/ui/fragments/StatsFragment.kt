package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.zekecode.akira_financialtracker.data.local.database.AkiraDatabase
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import com.zekecode.akira_financialtracker.databinding.FragmentStatsBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.StatsViewModel
import com.zekecode.akira_financialtracker.ui.viewmodels.StatsViewModelFactory

/**
 * This class needs refactoring, as its state right now is disgusting.
 * The vico library does not help at all with dependency injections.
 */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy {
        val database = AkiraDatabase.getDatabase(requireContext(), lifecycleScope)
        FinancialRepository(database.expenseDao(), database.earningDao(), database.categoryDao(), database.budgetDao())
    }

    private val viewModel: StatsViewModel by viewModels{
        StatsViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chartView.modelProducer = viewModel.expenseChartModelProducer
        binding.revenueChartView.modelProducer = viewModel.revenueChartModelProducer

        viewModel.expenseCategoryNames.observe(viewLifecycleOwner) { categoryNames ->
            setExpenseChartFormatter(categoryNames)
        }

        viewModel.earningCategoryNames.observe(viewLifecycleOwner) { categoryNames ->
            setEarningsChartFormatter(categoryNames)
        }
    }

    private fun setExpenseChartFormatter(categoryNames: List<String>) {
        Log.d("StatsFragment", "Category names : $categoryNames")
        val formatter = CartesianValueFormatter { _, x, _ ->
            categoryNames.getOrNull(x.toInt()) ?: x.toString()
        }
        binding.chartView.chart?.bottomAxis = (binding.chartView.chart?.bottomAxis as HorizontalAxis).copy(valueFormatter = formatter)
    }

    private fun setEarningsChartFormatter(categoryNames: List<String>) {
        val formatter = CartesianValueFormatter { _, x, _ ->
            categoryNames.getOrNull(x.toInt()) ?: x.toString()
        }
        binding.revenueChartView.chart?.bottomAxis = (binding.revenueChartView.chart?.bottomAxis as HorizontalAxis).copy(valueFormatter = formatter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
