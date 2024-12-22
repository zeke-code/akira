package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.FragmentHomeBinding
import com.zekecode.akira_financialtracker.ui.adapters.TransactionsAdapter
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

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

        setupRecyclerView()
        setupObservers()
        Log.d("HomeFragment", "Remaining budget: ${viewModel.remainingMonthlyBudget.value}")
    }

    private fun setupRecyclerView() {
        transactionsAdapter = TransactionsAdapter(emptyList())
        binding.homeExpenseRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.homeExpenseRecyclerView.adapter = transactionsAdapter
    }

    private fun setupObservers() {
        viewModel.currentMonthTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionsAdapter.updateTransactions(transactions)
        }

        viewModel.usedBudgetPercentage.observe(viewLifecycleOwner) { usedBudget ->
            val usedBudgetText = when {
                usedBudget < 0 -> getString(R.string.home_no_budget_used_text)
                usedBudget < 100 -> getString(R.string.home_used_budget_text, usedBudget)
                else -> getString(R.string.home_all_budged_used_text)
            }
            binding.homeUsedBudgetText.text = usedBudgetText

            val trackColor = if (usedBudget > 75) {
                requireContext().getColor(R.color.accent_yellow)
            } else {
                requireContext().getColor(R.color.accent_green)
            }

            binding.circularProgress.trackColor = trackColor
            binding.circularProgress.setProgress(usedBudget.toInt(), true)
        }

        viewModel.remainingMonthlyBudget.observe(viewLifecycleOwner) { remainingBudget ->
            viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
                val formattedText = getString(
                    R.string.home_remaining_budget_frame_text,
                    remainingBudget ?: 0F,
                    symbol
                )
                binding.budgetText.text = formattedText
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
