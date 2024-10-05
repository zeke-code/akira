package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
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

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy {
        val database = AkiraDatabase.getDatabase(requireContext(), lifecycleScope)
        FinancialRepository(database.expenseDao(), database.earningDao(), database.categoryDao())
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

        binding.chartView.modelProducer = viewModel.chartModelProducer
        binding.revenueChartView.modelProducer = viewModel.revenueChartModelProducer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
