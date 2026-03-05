package com.example.finance_trackerlabexam03.models

import java.io.Serializable

data class Transaction(
    val id: String,
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val category: String,
    val date: String,
    val note: String = ""
) : Serializable 