package com.zekecode.akira_financialtracker.utils

import android.content.SharedPreferences
import java.text.NumberFormat
import java.util.Currency

object CurrencyUtils {

    // Function to get the currency symbol
    fun getCurrencySymbol(sharedPreferences: SharedPreferences): String {
        val currencyCode = sharedPreferences.getString("Currency", "USD") ?: "USD"
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: IllegalArgumentException) {
            "$"  // Fallback to default symbol
        }
    }

    // Function to format the amount with the currency symbol
    fun formatAmountWithCurrency(amount: Double, sharedPreferences: SharedPreferences): String {
        val currencyCode = sharedPreferences.getString("Currency", "USD") ?: "USD"
        val currencyFormat = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
        return currencyFormat.format(amount)
    }
}
