package com.example.finance_trackerlabexam03

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.finance_trackerlabexam03.auth.AuthManager
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : BaseActivity() {
    private lateinit var currencyRadioGroup: RadioGroup
    private lateinit var backupButton: MaterialButton
    private lateinit var restoreButton: MaterialButton
    private lateinit var resetButton: MaterialButton
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize views
        currencyRadioGroup = findViewById(R.id.currencyRadioGroup)
        backupButton = findViewById(R.id.backupButton)
        restoreButton = findViewById(R.id.restoreButton)
        resetButton = findViewById(R.id.resetButton)
        backButton = findViewById(R.id.backButton)

        // Set up bottom navigation
        setupBottomNavigation()
        updateBottomNavigationState()

        // Load saved currency preference
        loadCurrencyPreference()

        // Set up click listeners
        setupClickListeners()
    }

    private fun loadCurrencyPreference() {
        val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val savedCurrency = sharedPref.getString("currency", "Rs")
        
        when (savedCurrency) {
            "$" -> currencyRadioGroup.check(R.id.dollarRadio)
            "Rs" -> currencyRadioGroup.check(R.id.rupeeRadio)
            "£" -> currencyRadioGroup.check(R.id.gbpRadio)
            "€" -> currencyRadioGroup.check(R.id.euroRadio)
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            onBackPressed()
        }

        currencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val currency = when (checkedId) {
                R.id.dollarRadio -> "$"
                R.id.rupeeRadio -> "Rs"
                R.id.gbpRadio -> "£"
                R.id.euroRadio -> "€"
                else -> "Rs"
            }
            
            // Save currency preference
            val sharedPref = getSharedPreferences("app_preferences", MODE_PRIVATE)
            sharedPref.edit().putString("currency", currency).apply()
            
            // Show toast and refresh the app
            Toast.makeText(this, "Currency set to $currency", Toast.LENGTH_SHORT).show()
            recreate()
        }

        backupButton.setOnClickListener {
            backupData()
        }

        restoreButton.setOnClickListener {
            showRestoreDialog()
        }

        resetButton.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun backupData() {
        try {
            // Create backup directory in app's external files directory
            val backupDir = File(getExternalFilesDir(null), "FinanceTrackerBackups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Create backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFile = File(backupDir, "backup_$timestamp.json")

            // Get all data from both SharedPreferences
            val appPrefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
            val financePrefs = getSharedPreferences("FinanceTracker", MODE_PRIVATE)
            
            // Combine all data into a single JSON object
            val jsonData = JSONObject()
            
            // Add app preferences data
            appPrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> jsonData.put("app_$key", value)
                    is Int -> jsonData.put("app_$key", value)
                    is Boolean -> jsonData.put("app_$key", value)
                    is Float -> jsonData.put("app_$key", value)
                    is Long -> jsonData.put("app_$key", value)
                }
            }
            
            // Add finance tracker data
            financePrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> jsonData.put("finance_$key", value)
                    is Int -> jsonData.put("finance_$key", value)
                    is Boolean -> jsonData.put("finance_$key", value)
                    is Float -> jsonData.put("finance_$key", value)
                    is Long -> jsonData.put("finance_$key", value)
                }
            }

            // Write to file
            FileWriter(backupFile).use { writer ->
                writer.write(jsonData.toString())
            }

            Toast.makeText(this, "Backup created successfully", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRestoreDialog() {
        val backupDir = File(getExternalFilesDir(null), "FinanceTrackerBackups")
        if (!backupDir.exists() || backupDir.listFiles()?.isEmpty() == true) {
            Toast.makeText(this, "No backup files found", Toast.LENGTH_SHORT).show()
            return
        }

        val backupFiles = backupDir.listFiles()?.filter { it.name.endsWith(".json") }?.map { it.name }?.toTypedArray()
            ?: emptyArray()

        AlertDialog.Builder(this)
            .setTitle("Select Backup to Restore")
            .setItems(backupFiles) { _, which ->
                val selectedFile = File(backupDir, backupFiles[which])
                showRestoreConfirmationDialog(selectedFile)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRestoreConfirmationDialog(backupFile: File) {
        AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage("Are you sure you want to restore this backup? Current data will be overwritten.")
            .setPositiveButton("Restore") { _, _ ->
                try {
                    val jsonString = backupFile.readText()
                    val jsonData = JSONObject(jsonString)

                    // Clear existing data
                    val appPrefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
                    val financePrefs = getSharedPreferences("FinanceTracker", MODE_PRIVATE)
                    val authPrefs = getSharedPreferences("FinanceTrackerPrefs", MODE_PRIVATE)
                    
                    appPrefs.edit().clear().apply()
                    financePrefs.edit().clear().apply()
                    authPrefs.edit().clear().apply()

                    // Process all keys and restore to appropriate SharedPreferences
                    jsonData.keys().forEach { key ->
                        when {
                            key.startsWith("app_") -> {
                                val actualKey = key.removePrefix("app_")
                                when (val value = jsonData.get(key)) {
                                    is String -> appPrefs.edit().putString(actualKey, value).apply()
                                    is Int -> appPrefs.edit().putInt(actualKey, value).apply()
                                    is Boolean -> appPrefs.edit().putBoolean(actualKey, value).apply()
                                    is Float -> appPrefs.edit().putFloat(actualKey, value).apply()
                                    is Long -> appPrefs.edit().putLong(actualKey, value).apply()
                                }
                            }
                            key.startsWith("finance_") -> {
                                val actualKey = key.removePrefix("finance_")
                                when (val value = jsonData.get(key)) {
                                    is String -> financePrefs.edit().putString(actualKey, value).apply()
                                    is Int -> financePrefs.edit().putInt(actualKey, value).apply()
                                    is Boolean -> financePrefs.edit().putBoolean(actualKey, value).apply()
                                    is Float -> financePrefs.edit().putFloat(actualKey, value).apply()
                                    is Long -> financePrefs.edit().putLong(actualKey, value).apply()
                                }
                            }
                            key.startsWith("auth_") -> {
                                val actualKey = key.removePrefix("auth_")
                                when (val value = jsonData.get(key)) {
                                    is String -> authPrefs.edit().putString(actualKey, value).apply()
                                    is Int -> authPrefs.edit().putInt(actualKey, value).apply()
                                    is Boolean -> authPrefs.edit().putBoolean(actualKey, value).apply()
                                    is Float -> authPrefs.edit().putFloat(actualKey, value).apply()
                                    is Long -> authPrefs.edit().putLong(actualKey, value).apply()
                                }
                            }
                        }
                    }

                    Toast.makeText(this, "Backup restored successfully", Toast.LENGTH_SHORT).show()
                    recreate()
                } catch (e: Exception) {
                    Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset Data")
            .setMessage("This will reset all your data. A backup will be created automatically before resetting. Are you sure you want to continue?")
            .setPositiveButton("Reset") { _, _ ->
                // Create backup before resetting
                backupData()
                
                // Clear all SharedPreferences
                val appPrefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
                val financePrefs = getSharedPreferences("FinanceTracker", MODE_PRIVATE)
                val authPrefs = getSharedPreferences("FinanceTrackerPrefs", MODE_PRIVATE)
                
                // Clear all preferences
                appPrefs.edit().clear().apply()
                financePrefs.edit().clear().apply()
                authPrefs.edit().clear().apply()
                
                // Reset currency to default
                appPrefs.edit().putString("currency", "Rs").apply()
                currencyRadioGroup.check(R.id.rupeeRadio)
                
                // Reset categories to default
                val defaultCategories = listOf(
                    "Food", "Transport", "Shopping", "Entertainment",
                    "Bills", "Health", "Education", "Other"
                )
                val categoriesJson = JSONArray()
                defaultCategories.forEach { category ->
                    val categoryObj = JSONObject()
                    categoryObj.put("id", UUID.randomUUID().toString())
                    categoryObj.put("name", category)
                    categoryObj.put("icon", 0) // Default icon
                    categoryObj.put("total", 0.0)
                    categoriesJson.put(categoryObj)
                }
                appPrefs.edit().putString("categories", categoriesJson.toString()).apply()
                
                Toast.makeText(this, "All data has been reset. A backup has been created.", Toast.LENGTH_LONG).show()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
} 