package com.example.finance_trackerlabexam03

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.finance_trackerlabexam03.databinding.ActivityAddtransactionformBinding
import com.example.finance_trackerlabexam03.models.Transaction
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AddTransactionFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddtransactionformBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var amountEditText: TextInputEditText
    private lateinit var categorySpinner: AutoCompleteTextView
    private lateinit var datePickerCard: CardView
    private lateinit var dateTextView: TextView
    private lateinit var transactionTypeGroup: RadioGroup
    private lateinit var incomeRadioButton: RadioButton
    private lateinit var expenseRadioButton: RadioButton
    private lateinit var saveButton: Button
    private var selectedDate = Calendar.getInstance()
    private var isIncome = true
    private var isUpdating = false
    private var updatePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddtransactionformBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("FinanceTracker", Context.MODE_PRIVATE)

        // Initialize views
        amountEditText = binding.amountEditText
        categorySpinner = binding.categorySpinner
        datePickerCard = binding.datePickerCard
        dateTextView = binding.dateTextView
        transactionTypeGroup = binding.transactionTypeGroup
        incomeRadioButton = binding.incomeRadioButton
        expenseRadioButton = binding.expenseRadioButton
        saveButton = binding.saveButton

        // Check if we're updating an existing transaction
        val transaction = intent.getSerializableExtra("transaction") as? Transaction
        if (transaction != null) {
            isUpdating = true
            updatePosition = intent.getIntExtra("position", -1)
            
            // Pre-fill the form with transaction details
            amountEditText.setText(transaction.amount.toString())
            categorySpinner.setText(transaction.category)
            
            // Try to parse the date in both formats
            val dateFormat1 = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val dateFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = try {
                dateFormat1.parse(transaction.date)
            } catch (e: Exception) {
                try {
                    dateFormat2.parse(transaction.date)
                } catch (e: Exception) {
                    Date() // Fallback to current date if parsing fails
                }
            }
            selectedDate.time = date ?: Date()
            updateDateText()
            
            if (transaction.type == "Income") {
                setIncomeSelected()
            } else {
                setExpenseSelected()
            }
        } else {
            updateDateText()
            setIncomeSelected()
        }

        setupSpinners()
        setupClickListeners()
    }

    private fun setupSpinners() {
        // Setup category dropdown
        val categories = resources.getStringArray(R.array.transaction_categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categorySpinner.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveTransaction()
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        datePickerCard.setOnClickListener {
            showDatePicker()
        }

        transactionTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.incomeRadioButton -> setIncomeSelected()
                R.id.expenseRadioButton -> setExpenseSelected()
            }
        }
    }

    private fun setIncomeSelected() {
        isIncome = true
        incomeRadioButton.isChecked = true
    }

    private fun setExpenseSelected() {
        isIncome = false
        expenseRadioButton.isChecked = true
    }

    private fun showDatePicker() {
        val datePicker = android.app.DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                updateDateText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateDateText() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        dateTextView.text = dateFormat.format(selectedDate.time)
    }

    private fun saveTransaction() {
        val amount = binding.amountEditText.text.toString().toDoubleOrNull() ?: 0.0
        val type = if (binding.incomeRadioButton.isChecked) "Income" else "Expense"
        val category = binding.categorySpinner.text.toString()
        val note = binding.descriptionEditText.text.toString()
        val date = binding.dateTextView.text.toString()

        if (amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            type = type,
            category = category,
            date = date,
            note = note
        )

        // Save transaction to SharedPreferences
        val transactionsJson = sharedPreferences.getString("transactions", "[]")
        val typeToken = object : TypeToken<List<Transaction>>() {}.type
        val transactions = gson.fromJson<List<Transaction>>(transactionsJson, typeToken).toMutableList()
        
        if (isUpdating && updatePosition != -1) {
            // Update existing transaction
            transactions[updatePosition] = transaction
        } else {
            // Add new transaction
            transactions.add(transaction)
        }
        
        sharedPreferences.edit().putString("transactions", gson.toJson(transactions)).apply()

        Toast.makeText(this, "Transaction saved successfully", Toast.LENGTH_SHORT).show()
        
        // Navigate to HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
} 