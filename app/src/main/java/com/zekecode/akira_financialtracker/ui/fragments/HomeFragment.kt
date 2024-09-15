package com.zekecode.akira_financialtracker.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import com.zekecode.akira_financialtracker.Application
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.FragmentHomeBinding
import com.zekecode.akira_financialtracker.ui.adapters.TransactionsAdapter
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModel
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cartesianChartView: CartesianChartView

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((requireActivity().application as Application).repository, sharedPreferences)
    }

    private lateinit var transactionsAdapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("AkiraPrefs", Context.MODE_PRIVATE)

        transactionsAdapter = TransactionsAdapter(emptyList())
        binding.homeExpenseRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.homeExpenseRecyclerView.adapter = transactionsAdapter

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionsAdapter.updateTransactions(transactions)
        }

        // Observe the real monthly budget
        viewModel.remainingMonthlyBudget.observe(viewLifecycleOwner) { realBudget ->

            binding.budgetTextView.text = getString(R.string.home_budget_text, realBudget)

            // Calculate the percentage of the budget used and update the progress bar
            val totalBudget = sharedPreferences.getFloat("MonthlyBudget", 0F)
            if (totalBudget > 0) {
                val progress = ((totalBudget - realBudget) / totalBudget * 100)
                binding.budgetProgressBar.progress = progress.toInt()
                binding.homeUsedBudgetText.text = getString(R.string.home_used_budget_text, progress)
            } else {
                binding.homeUsedBudgetText.text = getString(R.string.home_no_budget_set_text)
                binding.budgetProgressBar.progress = 0
            }
        }

        cartesianChartView = binding.homeChart
        cartesianChartView.modelProducer = viewModel.chartModelProducer

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
