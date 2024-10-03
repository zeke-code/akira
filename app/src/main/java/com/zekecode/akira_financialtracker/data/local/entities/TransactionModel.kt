package com.zekecode.akira_financialtracker.data.local.entities

/**
 * This class acts as a unified model for both transactions and expenses
 * to simplify the display of lists of ALL transactions throughout the app
 */
sealed class TransactionModel {
    data class Expense(val expenseWithCategory: ExpenseWithCategory) : TransactionModel()
    data class Earning(val earningWithCategory: EarningWithCategory) : TransactionModel()
}

