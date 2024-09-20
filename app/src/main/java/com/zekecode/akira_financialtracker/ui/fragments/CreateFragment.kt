package com.zekecode.akira_financialtracker.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.database.AkiraDatabase
import com.zekecode.akira_financialtracker.databinding.FragmentCreateBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.CreateViewModel
import com.zekecode.akira_financialtracker.ui.viewmodels.CreateViewModelFactory
import com.zekecode.akira_financialtracker.data.local.repository.FinancialRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy {
        val database = AkiraDatabase.getDatabase(requireContext(), lifecycleScope)
        FinancialRepository(database.expenseDao(), database.earningDao(), database.categoryDao())
    }

    private val viewModel: CreateViewModel by activityViewModels {
        CreateViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)

        setupInputListeners()

        // Set up TextWatcher to update hint dynamically
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amountText = s.toString()
                val amountDouble = amountText.toDoubleOrNull() ?: 0.0
                viewModel.setAmount(amountDouble)
                binding.tilAmount.hint = amountText.ifEmpty { getString(R.string.create_amount_hint) }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed on text changes
            }
        })

        // Set up TextWatcher for transaction name input
        binding.etCreateName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setTransactionName(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed on text changes
            }
        })

        // Observe the ViewModel's LiveData to navigate
        viewModel.navigateToHome.observe(viewLifecycleOwner, Observer { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_createFragment_to_homeFragment)
                viewModel.doneNavigating() // Reset the navigation state
            }
        })

        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            binding.tvCreateCategory.text = category?.name
        }

        viewModel.formattedSelectedDate.observe(viewLifecycleOwner) { formattedDate ->
            binding.tvCreateDate.text = formattedDate
        }

        // Handle Cancel Button Click
        binding.ivCancel.setOnClickListener {
            viewModel.onCancelClicked()
        }

        // Handle the Confirm button click to save data
        binding.ivConfirm.setOnClickListener {
            val amount = viewModel.amount.value ?: 0.0
            val name = viewModel.name.value ?: ""
            val category = viewModel.selectedCategory.value
            val date = viewModel.selectedDate.value
            val isExpense = viewModel.isExpense.value ?: true

            // Check if all required fields are filled
            if (amount > 0 && name.isNotEmpty() && category != null && date != null) {
                if (isExpense) {
                    viewModel.insertExpense()
                } else {
                    viewModel.insertEarning()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle category click button
        binding.tvCreateCategory.setOnClickListener {
            val dialog = SelectCategoryDialogFragment()
            dialog.show(childFragmentManager, "SelectCategoryDialog")
        }

        // Setup date button
        binding.tvCreateDate.setOnClickListener {
            showDatePickerDialog()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupInputListeners() {
        // Toggle group listener for expense or revenue
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_expense -> viewModel.setIsExpense(true)
                    R.id.btn_revenue -> viewModel.setIsExpense(false)
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        // Get the current date from ViewModel
        val calendar = Calendar.getInstance().apply {
            timeInMillis = viewModel.selectedDate.value ?: System.currentTimeMillis()
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog and show it
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                // Save the selected date in ViewModel
                viewModel.setSelectedDate(selectedDate.timeInMillis)
            },
            year, month, day
        )

        datePickerDialog.show()
    }
}
