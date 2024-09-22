package com.zekecode.akira_financialtracker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel
import com.zekecode.akira_financialtracker.databinding.ItemEarningBinding
import com.zekecode.akira_financialtracker.databinding.ItemExpenseBinding

class TransactionsAdapter(private var transactions: List<TransactionModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return when (transactions[position]) {
            is TransactionModel.Expense -> VIEW_TYPE_EXPENSE
            is TransactionModel.Earning -> VIEW_TYPE_EARNING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_EXPENSE -> {
                val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ExpenseViewHolder(binding)
            }
            VIEW_TYPE_EARNING -> {
                val binding = ItemEarningBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EarningViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = transactions[position]) {
            is TransactionModel.Expense -> (holder as ExpenseViewHolder).bind(item.expense)
            is TransactionModel.Earning -> (holder as EarningViewHolder).bind(item.revenue)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<TransactionModel>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: ExpenseModel) {
            binding.tvExpenseCategory.text = expense.category
            binding.tvExpenseAmount.text = itemView.context.getString(R.string.expense_amount, expense.amount)
            // TODO: Set the appropriate icon based on the category or expense type
            // binding.ivExpenseIcon.setImageResource(...) // Set your icon here
        }
    }

    // ViewHolder for Earnings using ViewBinding
    class EarningViewHolder(private val binding: ItemEarningBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(earning: EarningModel) {
            binding.tvEarningCategory.text = earning.category
            binding.tvEarningAmount.text = itemView.context.getString(R.string.earning_amount, earning.amount)
            // TODO: Set the appropriate icon based on the category or earning type
            // binding.ivEarningIcon.setImageResource(...) // Set your icon here
        }
    }

    companion object {
        private const val VIEW_TYPE_EXPENSE = 1
        private const val VIEW_TYPE_EARNING = 2
    }
}
