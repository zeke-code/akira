package com.zekecode.akira_financialtracker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zekecode.akira_financialtracker.R
import com.zekecode.akira_financialtracker.data.local.entities.EarningModel
import com.zekecode.akira_financialtracker.data.local.entities.ExpenseModel
import com.zekecode.akira_financialtracker.data.local.entities.TransactionModel

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
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
                ExpenseViewHolder(view)
            }
            VIEW_TYPE_EARNING -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_earning, parent, false)
                EarningViewHolder(view)
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
        // TODO: Target more specific events
        notifyDataSetChanged()
    }

    // ViewHolder for Expenses
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.ivExpenseIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.tvExpenseCategory)
        private val expenseAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)

        fun bind(expense: ExpenseModel) {
            categoryName.text = expense.category
            expenseAmount.text = itemView.context.getString(R.string.expense_amount, expense.amount)
            // TODO: Set the appropriate icon based on the category or expense type
        }
    }

    // ViewHolder for Earnings
    class EarningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.ivEarningIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.tvEarningCategory)
        private val earningAmount: TextView = itemView.findViewById(R.id.tvEarningAmount)

        fun bind(earning: EarningModel) {
            categoryName.text = earning.category
            earningAmount.text = itemView.context.getString(R.string.earning_amount, earning.amount)
            // TODO: Set the appropriate icon based on the category or earning type
            // categoryIcon.setImageResource(...) // Set your icon here
        }
    }

    companion object {
        private const val VIEW_TYPE_EXPENSE = 1
        private const val VIEW_TYPE_EARNING = 2
    }
}

