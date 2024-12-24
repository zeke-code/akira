package com.zekecode.akira_financialtracker.utils

import java.text.NumberFormat
import java.util.Currency
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyUtils @Inject constructor() {

    /**
     * Returns the symbol for the given currency code, or a fallback symbol if
     * the provided code is invalid.
     */
    fun getCurrencySymbol(currencyCode: String): String {
        return try {
            Currency.getInstance(currencyCode).symbol
        } catch (e: IllegalArgumentException) {
            "$"  // Fallback symbol
        }
    }

    /**
     * Formats the given amount (Double) as a currency string for the provided currencyCode.
     */
    fun formatAmountWithCurrency(amount: Double, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
        return format.format(amount)
    }
}