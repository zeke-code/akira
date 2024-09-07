package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.database.AkiraDatabase
import com.zekecode.akira_financialtracker.databinding.FragmentCreateBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.CreateViewModel
import com.zekecode.akira_financialtracker.ui.viewmodels.CreateViewModelFactory
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository

class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    // Initialize repository and ViewModel factory correctly
    private val repository by lazy {
        val database = AkiraDatabase.getDatabase(requireContext(), lifecycleScope)
        FinancialRepository(database.expenseDao(), database.earningDao(), database.categoryDao())
    }

    private val viewModel: CreateViewModel by viewModels {
        CreateViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)

        // Set up TextWatcher to update hint dynamically
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tilAmount.hint = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed on text changes
            }
        })

        binding.ivCancel.setOnClickListener {
            viewModel.onCancelClicked()
        }

        // Observe the ViewModel's LiveData to navigate
        viewModel.navigateToHome.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_createFragment_to_homeFragment)
                viewModel.doneNavigating() // Reset the navigation state
            }
        })

        // Handle the Confirm button click to save data
        binding.ivConfirm.setOnClickListener {
            val name = "Sample Earning" // Replace with actual input
            val category = "Salary" // Replace with actual input
            val date = System.currentTimeMillis()

            if (!viewModel.amount.value.isNullOrEmpty()) {
                viewModel.insertEarning(name, category, date)
            } else {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
