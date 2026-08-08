package com.credenceai.app.core.utils

import java.util.Locale

object CurrencyUtils {
    fun formatAmount(amount: Double, currencyString: String): String {
        val symbol = extractSymbol(currencyString)
        return "$symbol${"%.2f".format(Locale.getDefault(), amount)}"
    }

    fun extractSymbol(currencyString: String): String {
        // Extracts symbol from "INR (₹)" or "USD ($)"
        val startIndex = currencyString.indexOf('(')
        val endIndex = currencyString.indexOf(')')
        return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex + 1) {
            currencyString.substring(startIndex + 1, endIndex)
        } else {
            "₹" // Default fallback
        }
    }
}
