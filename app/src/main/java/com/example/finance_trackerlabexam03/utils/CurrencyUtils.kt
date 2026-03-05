package com.example.finance_trackerlabexam03.utils

import android.content.Context
import android.content.SharedPreferences

object CurrencyUtils {
    private const val DEFAULT_CURRENCY = "Rs"
    
    fun getCurrencySymbol(context: Context): String {
        val sharedPref = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPref.getString("currency", DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }
    
    fun formatAmount(context: Context, amount: Double): String {
        val currencySymbol = getCurrencySymbol(context)
        return String.format("%s%.2f", currencySymbol, amount)
    }
    
    fun formatAmountWithSign(context: Context, amount: Double, isIncome: Boolean): String {
        val currencySymbol = getCurrencySymbol(context)
        val sign = if (isIncome) "+" else "-"
        return String.format("%s%s%.2f", sign, currencySymbol, amount)
    }
} 