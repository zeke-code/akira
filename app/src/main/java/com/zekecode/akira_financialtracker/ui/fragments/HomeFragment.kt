package com.zekecode.akira_financialtracker.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zekecode.akira_financialtracker.Application
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.databinding.FragmentHomeBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModel
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((requireActivity().application as Application).repository, sharedPreferences)
    }

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

        viewModel.userName.observe(viewLifecycleOwner) { userName ->
            binding.welcomeTextView.text = getString(R.string.home_welcome_text, userName)
        }

        viewModel.monthlyBudget.observe(viewLifecycleOwner) { monthlyBudget ->
            binding.budgetTextView.text = getString(R.string.home_budget_text, monthlyBudget)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
