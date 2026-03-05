package com.example.finance_trackerlabexam03.models

data class User(
    val id: String,
    val username: String,
    val email: String,
    val password: String,
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 