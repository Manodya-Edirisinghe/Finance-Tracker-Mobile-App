package com.example.finance_trackerlabexam03

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finance_trackerlabexam03.databinding.ActivityCategoryDetailsBinding
import com.example.finance_trackerlabexam03.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

class CategoryDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityCategoryDetailsBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var adapter: TransactionListAdapter
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val transactions = mutableListOf<Transaction>()
    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra("category_name") ?: ""
        binding.categoryTitle.text = categoryName

        sharedPreferences = getSharedPreferences("FinanceTracker", Context.MODE_PRIVATE)
        setupRecyclerView()
        setupClickListeners()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionListAdapter(transactions) { transaction ->
            // Handle transaction click if needed
        }

        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryDetailsActivity)
            adapter = this@CategoryDetailsActivity.adapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadTransactions() {
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        val typeToken = object : TypeToken<List<Transaction>>() {}.type
        val loadedTransactions = gson.fromJson<List<Transaction>>(transactionsJson, typeToken)
        
        // Filter transactions for this category
        transactions.clear()
        transactions.addAll(loadedTransactions.filter { it.category == categoryName }
            .sortedWith(compareByDescending { 
                try {
                    dateFormat.parse(it.date)
                } catch (e: Exception) {
                    try {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                    } catch (e: Exception) {
                        null
                    }
                }
            }))

        // Calculate category total
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                "Income" -> totalIncome += transaction.amount
                "Expense" -> totalExpense += transaction.amount
            }
        }

        val categoryTotal = totalIncome - totalExpense

        // Update UI
        binding.categoryTotalText.text = String.format("$%.2f", categoryTotal)
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }
} 