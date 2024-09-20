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

        viewModel.usedBudgetPercentage.observe(viewLifecycleOwner) { usedBudget ->
            if (usedBudget > 0) {
                binding.homeUsedBudgetText.text = getString(R.string.home_used_budget_text, usedBudget)
            } else {
                binding.homeUsedBudgetText.text = getString(R.string.home_no_budget_set_text)
            }
            usedBudget?.toInt()?.let { binding.circularProgress.setProgress(it, true) }
        }

        viewModel.remainingMonthlyBudget.observe(viewLifecycleOwner) { remainingBudget ->
            viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
                symbol?.let {
                    val formattedText = getString(R.string.home_remaining_budget_frame_text, remainingBudget ?: 0F, it)
                    binding.budgetText.text = formattedText
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
