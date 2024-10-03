package com.zekecode.akira_financialtracker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.entities.EarningWithCategory
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseWithCategory
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
            is TransactionModel.Expense -> (holder as ExpenseViewHolder).bind(item.expenseWithCategory)
            is TransactionModel.Earning -> (holder as EarningViewHolder).bind(item.earningWithCategory)
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<TransactionModel>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expenseWithCategory: ExpenseWithCategory) {
            val expense = expenseWithCategory.expense
            val category = expenseWithCategory.category

            binding.tvExpenseCategory.text = category.name
            binding.tvExpenseAmount.text = itemView.context.getString(R.string.expense_amount, expense.amount)
            binding.ivExpenseIcon.setImageResource(category.icon)
        }
    }

    class EarningViewHolder(private val binding: ItemEarningBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(earningWithCategory: EarningWithCategory) {
            val earning = earningWithCategory.earning
            val category = earningWithCategory.category

            binding.tvEarningCategory.text = category.name
            binding.tvEarningAmount.text = itemView.context.getString(R.string.earning_amount, earning.amount)
            binding.ivEarningIcon.setImageResource(category.icon)
        }
    }

    companion object {
        private const val VIEW_TYPE_EXPENSE = 1
        private const val VIEW_TYPE_EARNING = 2
    }
}
