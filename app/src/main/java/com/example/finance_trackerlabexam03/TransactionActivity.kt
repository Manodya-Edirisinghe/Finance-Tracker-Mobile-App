package com.example.finance_trackerlabexam03

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finance_trackerlabexam03.databinding.ActivityTransactionBinding
import com.example.finance_trackerlabexam03.models.Transaction
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : BaseActivity() {
    private lateinit var binding: ActivityTransactionBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var adapter: TransactionAdapter
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val transactions = mutableListOf<Transaction>()
    private var currentFilter = "All" // Default filter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("FinanceTracker", Context.MODE_PRIVATE)
        setupRecyclerView()
        setupClickListeners()
        loadTransactions()
        super.setupBottomNavigation()
        super.updateBottomNavigationState()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(this, { transaction, position ->
            // Handle transaction click
        }) {
            // Handle transaction update
            loadTransactions()
        }

        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionActivity)
            adapter = this@TransactionActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.notificationBell.setOnClickListener {
            finish()
        }

        binding.addTransactionButton.setOnClickListener {
            startActivity(Intent(this, AddTransactionFormActivity::class.java))
        }

        binding.allFilterChip.setOnClickListener {
            currentFilter = "All"
            loadTransactions()
        }

        binding.incomeFilterChip.setOnClickListener {
            currentFilter = "Income"
            loadTransactions()
        }

        binding.expenseFilterChip.setOnClickListener {
            currentFilter = "Expense"
            loadTransactions()
        }
    }

    private fun loadTransactions() {
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        val typeToken = object : TypeToken<List<Transaction>>() {}.type
        val loadedTransactions = gson.fromJson<List<Transaction>>(transactionsJson, typeToken)
        
        // Sort transactions by date in descending order (most recent first)
        transactions.clear()
        transactions.addAll(loadedTransactions.sortedWith(compareByDescending { 
            try {
                dateFormat.parse(it.date)
            } catch (e: Exception) {
                Date(0) // If date parsing fails, put at the end
            }
        }))

        // Filter transactions based on selected category
        val filteredTransactions = when (currentFilter) {
            "Income" -> transactions.filter { it.type == "Income" }
            "Expense" -> transactions.filter { it.type == "Expense" }
            else -> transactions
        }

        // Update adapter with filtered transactions
        adapter.updateTransactions(filteredTransactions)
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }
} 