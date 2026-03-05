package com.example.finance_trackerlabexam03

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finance_trackerlabexam03.auth.AuthManager
import com.example.finance_trackerlabexam03.databinding.ActivityHomeBinding
import com.example.finance_trackerlabexam03.models.Transaction
import com.example.finance_trackerlabexam03.utils.CurrencyUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var transactionAdapter: TransactionAdapter
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("FinanceTracker", Context.MODE_PRIVATE)
        setupRecyclerView()
        loadTransactions()
        updateUI()
        setupClickListeners()

        // Check if user is logged in
        val currentUser = AuthManager.getCurrentUser()
        if (currentUser == null) {
            Log.e("HomeActivity", "No user found, redirecting to login")
            // If not logged in, redirect to login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        Log.d("HomeActivity", "User logged in: ${currentUser.username}")
        // Display welcome message
        binding.welcomeText.text = "Welcome, ${currentUser.name}!"

        // Set up notification bell click listener
        binding.notificationBell.setOnClickListener {
            // TODO: Handle notification bell click
        }

        // Set up bottom navigation
        setupBottomNavigation()
        updateBottomNavigationState()
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            context = this,
            onTransactionClick = { transaction, position ->
                // Handle transaction click if needed
            },
            onTransactionUpdated = {
                loadTransactions()
            }
        )

        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = transactionAdapter
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
        
        // Calculate totals
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            if (transaction.type == "Income") {
                totalIncome += transaction.amount
            } else {
                totalExpense += transaction.amount
            }
        }

        val balance = totalIncome - totalExpense

        // Update UI
        binding.totalBalanceText.text = CurrencyUtils.formatAmount(this, balance)
        binding.totalExpenseText.text = CurrencyUtils.formatAmountWithSign(this, totalExpense, false)

        // Update adapter
        transactionAdapter.updateTransactions()
    }

    private fun updateUI() {
        val totalIncome = transactions
            .filter { it.type == "Income" }
            .sumOf { it.amount }
        
        val totalExpense = transactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }
        
        val balance = totalIncome - totalExpense

        binding.totalBalanceText.text = CurrencyUtils.formatAmount(this, balance)
        binding.totalExpenseText.text = CurrencyUtils.formatAmountWithSign(this, totalExpense, false)
    }

    private fun setupClickListeners() {
        // Set up bottom navigation click listeners
        binding.bottomNavigationInclude.transferNavItem.setOnClickListener {
            startActivity(Intent(this, AddTransactionFormActivity::class.java))
        }

        binding.bottomNavigationInclude.walletNavItem.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // Set up budget edit button
        binding.editBudgetButton.setOnClickListener {
            showBudgetEditDialog()
        }
    }

    private fun showBudgetEditDialog() {
        val budgetValue = sharedPreferences.all["monthly_budget"]
        val currentBudget = when (budgetValue) {
            is Float -> budgetValue
            is Int -> budgetValue.toFloat()
            else -> 0f
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_budget, null)
        val budgetInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.budgetInput)
        budgetInput.setText(currentBudget.toString())

        MaterialAlertDialogBuilder(this)
            .setTitle("Set Monthly Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                try {
                    val newBudget = budgetInput.text.toString().toFloat()
                    if (newBudget < 0) {
                        Toast.makeText(this, "Budget cannot be negative", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    sharedPreferences.edit().putFloat("monthly_budget", newBudget).apply()
                    updateBudgetUI(newBudget)
                    dialog.dismiss()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateBudgetUI(budget: Float) {
        val totalExpense = transactions
            .filter { it.type == "Expense" }
            .sumOf { it.amount }
        
        val progress = if (budget > 0) {
            (totalExpense / budget * 100).toInt()
        } else {
            0
        }
        
        binding.budgetProgressBar.progress = progress
        binding.budgetSpentText.text = CurrencyUtils.formatAmount(this, totalExpense)
        binding.budgetLimitText.text = String.format("/ %s", CurrencyUtils.formatAmount(this, budget.toDouble()))
        binding.budgetProgressText.text = String.format("%d%%", progress)

        // Update progress bar color and warning message based on usage
        when {
            progress >= 90 -> {
                // Near budget limit (red)
                binding.budgetProgressBar.setIndicatorColor(android.graphics.Color.parseColor("#FF5722"))
                binding.budgetWarningText.apply {
                    text = "Warning: You are very close to your monthly budget limit!"
                    visibility = View.VISIBLE
                }
            }
            progress >= 50 -> {
                // Halfway through budget (orange)
                binding.budgetProgressBar.setIndicatorColor(android.graphics.Color.parseColor("#FF9800"))
                binding.budgetWarningText.apply {
                    text = "You have used more than half of your monthly budget"
                    visibility = View.VISIBLE
                }
            }
            else -> {
                // Under budget (green)
                binding.budgetProgressBar.setIndicatorColor(android.graphics.Color.parseColor("#4CAF50"))
                binding.budgetWarningText.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
        updateUI()
        val budgetValue = sharedPreferences.all["monthly_budget"]
        val budget = when (budgetValue) {
            is Float -> budgetValue
            is Int -> budgetValue.toFloat()
            else -> 0f
        }
        updateBudgetUI(budget)
    }
} 