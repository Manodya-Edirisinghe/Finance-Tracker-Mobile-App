package com.example.finance_trackerlabexam03

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.finance_trackerlabexam03.databinding.ActivityCategoriesBinding
import com.example.finance_trackerlabexam03.models.Transaction
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.text.NumberFormat
import java.util.Locale

class CategoriesActivity : BaseActivity() {
    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson
    private val categories = mutableListOf<Category>()
    private val transactions = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("FinanceTracker", Context.MODE_PRIVATE)
        gson = Gson()

        setupUI()
        loadCategories()
        loadTransactions()
        loadBalances()
        super.setupBottomNavigation()
        super.updateBottomNavigationState()
    }

    override fun onResume() {
        super.onResume()
        super.updateBottomNavigationState()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.notificationBell.setOnClickListener {
            // TODO: Implement notification functionality
        }

        setupCategoryClickListeners()
    }

    private fun setupCategoryClickListeners() {
        // Food category
        binding.foodCategory.setOnClickListener {
            showDeleteDialog("Food")
        }

        // Transport category
        binding.transportCategory.setOnClickListener {
            showDeleteDialog("Transport")
        }

        // Medicine category
        binding.medicineCategory.setOnClickListener {
            showDeleteDialog("Medicine")
        }

        // Groceries category
        binding.groceriesCategory.setOnClickListener {
            showDeleteDialog("Groceries")
        }

        // Rent category
        binding.rentCategory.setOnClickListener {
            showDeleteDialog("Rent")
        }

        // Gifts category
        binding.giftsCategory.setOnClickListener {
            showDeleteDialog("Gifts")
        }

        // Savings category
        binding.savingsCategory.setOnClickListener {
            showDeleteDialog("Savings")
        }

        // Entertainment category
        binding.entertainmentCategory.setOnClickListener {
            showDeleteDialog("Entertainment")
        }

        // Add New category
        binding.addNewCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showDeleteDialog(categoryName: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_delete_category)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            deleteCategory(categoryName)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteCategory(categoryName: String) {
        categories.removeAll { it.name == categoryName }
        saveCategories()
        updateCategoriesUI()
    }

    private fun updateCategoriesUI() {
        // Clear existing views except the "Add New" button
        val addNewIndex = binding.categoriesGrid.childCount - 1
        binding.categoriesGrid.removeViews(0, addNewIndex)

        // Add all categories back to the grid
        categories.forEach { category ->
            addCategoryToGrid(category)
        }
    }

    private fun navigateToCategoryDetails(categoryName: String) {
        val intent = Intent(this, CategoryDetailsActivity::class.java)
        intent.putExtra("category_name", categoryName)
        startActivity(intent)
    }

    private fun showAddCategoryDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_category)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val categoryNameInput = dialog.findViewById<TextInputEditText>(R.id.categoryNameInput)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val categoryName = categoryNameInput.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                addNewCategory(categoryName)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun addNewCategory(name: String) {
        val category = Category(
            id = UUID.randomUUID().toString(),
            name = name,
            icon = R.drawable.ic_category_default,
            total = 0.0
        )
        
        categories.add(category)
        saveCategories()
        addCategoryToGrid(category)
    }

    private fun addCategoryToGrid(category: Category) {
        val categoryView = layoutInflater.inflate(R.layout.item_category, null)
        val categoryIcon = categoryView.findViewById<ImageView>(R.id.categoryIcon)
        val categoryName = categoryView.findViewById<TextView>(R.id.categoryName)
        val categoryAmount = categoryView.findViewById<TextView>(R.id.categoryAmount)

        // Set category data
        categoryName.text = category.name
        categoryAmount.text = formatCurrency(category.total)
        
        // Set icon based on category name
        when (category.name.lowercase()) {
            "food" -> categoryIcon.setImageResource(R.drawable.food)
            "transport" -> categoryIcon.setImageResource(R.drawable.transport)
            "shopping" -> categoryIcon.setImageResource(R.drawable.shopping)
            "entertainment" -> categoryIcon.setImageResource(R.drawable.entertainment)
            "bills" -> categoryIcon.setImageResource(R.drawable.bills)
            "health" -> categoryIcon.setImageResource(R.drawable.health)
            "education" -> categoryIcon.setImageResource(R.drawable.education)
            "other" -> categoryIcon.setImageResource(R.drawable.other)
            else -> categoryIcon.setImageResource(R.drawable.ic_category_default)
        }

        // Set click listeners
        categoryView.setOnClickListener {
            showDeleteCategoryDialog(category)
        }

        // Add to grid
        binding.categoriesGrid.addView(categoryView)
    }

    private fun showDeleteCategoryDialog(category: Category) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_category, null)
        
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Delete") { _, _ ->
                // Remove category from the list
                categories.remove(category)
                
                // Save updated categories
                saveCategories()
                
                // Update UI
                updateCategoriesUI()
                
                Toast.makeText(this, "Category deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "LK"))
        return format.format(amount)
    }

    private fun saveCategories() {
        val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val editor = sharedPref.edit()
        
        // Convert categories to JSON array
        val jsonArray = JSONArray()
        categories.forEach { category ->
            val jsonObject = JSONObject().apply {
                put("id", category.id)
                put("name", category.name)
                put("icon", category.icon)
                put("total", category.total)
            }
            jsonArray.put(jsonObject)
        }
        
        editor.putString("categories", jsonArray.toString())
        editor.apply()
    }

    private fun loadCategories() {
        val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val categoriesJson = sharedPref.getString("categories", null)
        
        if (categoriesJson != null) {
            val jsonArray = JSONArray(categoriesJson)
            categories.clear()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                categories.add(
                    Category(
                        id = jsonObject.getString("id"),
                        name = jsonObject.getString("name"),
                        icon = jsonObject.getInt("icon"),
                        total = jsonObject.getDouble("total")
                    )
                )
            }
        }
    }

    private fun loadTransactions() {
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        val savedTransactions = gson.fromJson<List<Transaction>>(transactionsJson, type)
        transactions.clear()
        transactions.addAll(savedTransactions)
    }

    private fun loadBalances() {
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            when (transaction.type) {
                "Income" -> totalIncome += transaction.amount
                "Expense" -> totalExpense += transaction.amount
            }
        }

        val totalBalance = totalIncome - totalExpense

        // Update UI with formatted currency
        binding.totalBalanceText.text = String.format("$%.2f", totalBalance)
        binding.totalExpenseText.text = String.format("$%.2f", totalExpense)
    }

    data class Category(
        val id: String,
        val name: String,
        val icon: Int,
        val total: Double
    )
} 