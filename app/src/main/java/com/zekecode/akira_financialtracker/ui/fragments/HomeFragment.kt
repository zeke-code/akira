package com.zekecode.akira_financialtracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.entities.CategoryModel
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.databinding.DialogEditTransactionBinding
import com.zekecode.akira_financialtracker.databinding.FragmentHomeBinding
import com.zekecode.akira_financialtracker.ui.adapters.TransactionsAdapter
import com.zekecode.akira_financialtracker.ui.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var dialogBinding: DialogEditTransactionBinding? = null
    private var currentEditDialog: Dialog? = null
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var transactionsAdapter: TransactionsAdapter
    private var selectedCategory: CategoryModel? = null

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
        setUpObservers()
        setupButtons()

        // Set up the fragment result listener once
        parentFragmentManager.setFragmentResultListener("requestKey", viewLifecycleOwner) { _, bundle ->
            selectedCategory = bundle.getParcelable("selectedCategory")
            // Update dialog if it's showing
            dialogBinding?.let { binding ->
                selectedCategory?.let { category ->
                    binding.tvCategory.setText(category.name)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        transactionsAdapter = TransactionsAdapter(emptyList()) { transaction ->
            handleTransactionEdit(transaction)
        }
        binding.homeExpenseRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionsAdapter
        }

        setupSwipeToDelete(binding.homeExpenseRecyclerView, transactionsAdapter)
    }

    private fun setUpObservers() {
        viewModel.filteredTransactions.observe(viewLifecycleOwner) { transactions ->
            transactionsAdapter.updateTransactions(transactions)
        }

        viewModel.usedBudgetPercentage.observe(viewLifecycleOwner) { usedBudget ->
            val usedBudgetText = getBudgetUsageText(usedBudget)
            binding.homeUsedBudgetText.text = usedBudgetText
            updateCircularProgress(usedBudget)
        }

        viewModel.remainingMonthlyBudget.observe(viewLifecycleOwner) { remainingBudget ->
            viewModel.currencySymbol.observe(viewLifecycleOwner) { symbol ->
                binding.budgetText.text = getString(
                    R.string.home_remaining_budget_frame_text,
                    remainingBudget ?: 0F,
                    symbol
                )
            }
        }
    }

    private fun setupButtons() {
        binding.buttonDaily.setOnClickListener {
            viewModel.setTransactionFilter(HomeViewModel.Filter.DAILY)
        }

        binding.buttonWeekly.setOnClickListener {
            viewModel.setTransactionFilter(HomeViewModel.Filter.WEEKLY)
        }

        binding.buttonMonthly.setOnClickListener {
            viewModel.setTransactionFilter(HomeViewModel.Filter.MONTHLY)
        }
    }

    private fun getBudgetUsageText(usedBudget: Float): String {
        return when {
            usedBudget <= 0 -> getString(R.string.home_no_budget_used_text)
            usedBudget < 100 -> getString(R.string.home_used_budget_text, usedBudget)
            else -> getString(R.string.home_all_budged_used_text)
        }
    }

    private fun updateCircularProgress(usedBudget: Float) {
        val trackColor = if (usedBudget > 75) {
            requireContext().getColor(R.color.warning_yellow)
        } else {
            requireContext().getColor(R.color.accent_green)
        }
        binding.circularProgress.trackColor = trackColor
        binding.circularProgress.setProgress(usedBudget.toInt(), true)
    }

    private fun handleTransactionEdit(transaction: TransactionModel) {
        showEditTransactionDialog(transaction)
    }

    private fun showEditTransactionDialog(transaction: TransactionModel) {
        val dialog = Dialog(requireContext())
        dialogBinding = DialogEditTransactionBinding.inflate(layoutInflater)
        currentEditDialog = dialog

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(dialogBinding!!.root)

        // Populate existing fields
        when (transaction) {
            is TransactionModel.Expense -> {
                val expense = transaction.expenseWithCategory.expense
                dialogBinding!!.etTransactionDescription.setText(expense.description)
                dialogBinding!!.etTransactionAmount.setText(expense.amount.toString())
                dialogBinding!!.etTransactionDate.setText(
                    android.text.format.DateFormat.format("MMM dd, yyyy", expense.date)
                )
                dialogBinding!!.tvCategory.setText(transaction.expenseWithCategory.category.name)
            }
            is TransactionModel.Earning -> {
                val earning = transaction.earningWithCategory.earning
                dialogBinding!!.etTransactionDescription.setText(earning.description)
                dialogBinding!!.etTransactionAmount.setText(earning.amount.toString())
                dialogBinding!!.etTransactionDate.setText(
                    android.text.format.DateFormat.format("MMM dd, yyyy", earning.date)
                )
                dialogBinding!!.tvCategory.setText(transaction.earningWithCategory.category.name)
            }
        }

        dialogBinding!!.tvCategory.setOnClickListener {
            val dialogFragment = SelectCategoryDialogFragment()
            dialogFragment.show(parentFragmentManager, "SelectCategoryDialogFragment")
        }

        // Listen for category selection result
        parentFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            selectedCategory = bundle.getParcelable("selectedCategory")
            if (selectedCategory != null) {
                dialogBinding!!.tvCategory.setText(selectedCategory!!.name)
            }
        }

        dialogBinding!!.etTransactionDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_date)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val selectedDate = Calendar.getInstance().apply {
                    timeInMillis = selection
                }
                dialogBinding!!.etTransactionDate.setText(
                    android.text.format.DateFormat.format("MMM dd, yyyy", selectedDate.time)
                )
            }
        }

        dialogBinding!!.btnSave.setOnClickListener {
            val updatedDescription = dialogBinding!!.etTransactionDescription.text.toString()
            if (updatedDescription.isEmpty()) {
                dialogBinding!!.etTransactionDescription.error = "Description required"
                return@setOnClickListener
            }

            val updatedAmount = dialogBinding!!.etTransactionAmount.text.toString().toDoubleOrNull()
            if (updatedAmount == null) {
                dialogBinding!!.etTransactionAmount.error = "Valid amount required"
                return@setOnClickListener
            }

            val updatedDateString = dialogBinding!!.etTransactionDate.text.toString()
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val updatedDate = try {
                dateFormat.parse(updatedDateString)?.time
            } catch (e: Exception) {
                dialogBinding!!.etTransactionDate.error = "Invalid date format"
                return@setOnClickListener
            }

            if (updatedDate == null) {
                dialogBinding!!.etTransactionDate.error = "Valid date required"
                return@setOnClickListener
            }

            if (selectedCategory == null) {
                Snackbar.make(dialogBinding!!.root, "Please select a category", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateTransaction(
                transaction,
                updatedDescription,
                updatedAmount,
                updatedDate,
                selectedCategory!!
            )

            Snackbar.make(binding.root, "Transaction updated successfully", Snackbar.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogBinding!!.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            dialogBinding = null
            currentEditDialog = null
        }

        dialog.show()
    }

    private fun setupSwipeToDelete(recyclerView: RecyclerView, adapter: TransactionsAdapter) {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // No move operation required
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                // Remove the transaction from the adapter and notify
                val removedTransaction = adapter.removeTransactionAt(position)
                viewModel.deleteTransaction(removedTransaction)

                // Show a Snackbar for undo functionality
                Snackbar.make(recyclerView, "Transaction deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        adapter.addTransactionAt(position, removedTransaction)
                        viewModel.addTransactionBack(removedTransaction)
                    }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}