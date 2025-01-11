package com.zekecode.akira_financialtracker.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.databinding.FragmentCreateBinding
import com.zekecode.akira_financialtracker.ui.viewmodels.CreateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)

        setupObservers()
        setupListeners()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.navigateToHome.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_createFragment_to_homeFragment)
                viewModel.doneNavigating() // Reset the navigation state
            }
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            binding.tvCreateCategory.text = category?.name
        }

        viewModel.formattedSelectedDate.observe(viewLifecycleOwner) { formattedDate ->
            binding.tvCreateDate.text = formattedDate
        }
    }

    private fun setupListeners() {
        // TextWatcher to update amount hint dynamically
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val amountText = s.toString()
                val amountDouble = amountText.toDoubleOrNull() ?: 0.0
                viewModel.setAmount(amountDouble)
                binding.tilAmount.hint = amountText.ifEmpty { getString(R.string.create_amount_hint) }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // TextWatcher for transaction name input
        binding.etCreateName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setTransactionDescription(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Confirm button click listener
        binding.ivConfirm.setOnClickListener {
            if (viewModel.canInsertTransaction()) {
                binding.ivConfirm.isEnabled = false
                viewModel.insertTransaction(viewModel.isExpense.value ?: true)
            } else {
                Toast.makeText(requireContext(), "Enter at least the amount, category, and date.", Toast.LENGTH_SHORT).show()
            }
        }

        // Category selection click listener
        binding.tvCreateCategory.setOnClickListener {
            val dialog = SelectCategoryDialogFragment()
            dialog.show(childFragmentManager, "SelectCategoryDialog")
        }

        // Date picker click listener
        binding.tvCreateDate.setOnClickListener {
            showDatePicker()
        }

        // Toggle group listener for expense or revenue
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_expense -> viewModel.setIsExpense(true)
                    R.id.btn_revenue -> viewModel.setIsExpense(false)
                }
            }
        }

        // Listen for category selection result
        childFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val selectedCategory = bundle.getParcelable<CategoryModel>("selectedCategory")
            if (selectedCategory != null) {
                viewModel.setSelectedCategory(selectedCategory)
            }
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText(R.string.select_date)
        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            binding.tvCreateDate.text = datePicker.headerText
            viewModel.setSelectedDate(selection)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }
}