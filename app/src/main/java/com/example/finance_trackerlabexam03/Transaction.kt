package com.example.finance_trackerlabexam03

data class Transaction(
    val id: String,
    val type: String,
    val category: String,
    val amount: Double,
    val date: String,
    val description: String
) 