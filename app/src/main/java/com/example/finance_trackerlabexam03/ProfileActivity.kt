package com.example.finance_trackerlabexam03

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.finance_trackerlabexam03.auth.AuthManager
import com.example.finance_trackerlabexam03.models.User
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : BaseActivity() {
    private lateinit var nameText: TextView
    private lateinit var idText: TextView
    private lateinit var editProfileOption: LinearLayout
    private lateinit var settingsOption: LinearLayout
    private lateinit var logoutOption: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize views
        nameText = findViewById(R.id.nameText)
        idText = findViewById(R.id.idText)
        editProfileOption = findViewById(R.id.editProfileOption)
        settingsOption = findViewById(R.id.settingsOption)
        logoutOption = findViewById(R.id.logoutOption)

        // Set up back button
        findViewById<View>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Set up bottom navigation
        setupBottomNavigation()
        updateBottomNavigationState()

        // Load and display user data
        loadUserData()

        // Set up button click listeners
        setupButtonClickListeners()
    }

    private fun setupButtonClickListeners() {
        editProfileOption.setOnClickListener {
            showEditProfileDialog()
        }

        settingsOption.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        logoutOption.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showEditProfileDialog() {
        val currentUser = AuthManager.getCurrentUser() ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.nameInput)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.emailInput)
        val usernameInput = dialogView.findViewById<TextInputEditText>(R.id.usernameInput)

        // Set current values
        nameInput.setText(currentUser.name)
        emailInput.setText(currentUser.email)
        usernameInput.setText(currentUser.username)

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newEmail = emailInput.text.toString().trim()
                val newUsername = usernameInput.text.toString().trim()

                if (newName.isEmpty() || newEmail.isEmpty() || newUsername.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Create a new user object with updated values
                val updatedUser = User(
                    id = currentUser.id,
                    username = newUsername,
                    email = newEmail,
                    password = currentUser.password,
                    name = newName
                )

                // Save changes
                AuthManager.updateUser(updatedUser)
                loadUserData()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSettingsDialog() {
        val currencies = arrayOf("Rupees (Rs)", "Dollars ($)", "Pounds (£)", "Euros (€)")
        var selectedCurrency = 0 // Default to Rupees

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setSingleChoiceItems(currencies, selectedCurrency) { _, which ->
                selectedCurrency = which
            }
            .setPositiveButton("Save") { _, _ ->
                // Save selected currency
                val selectedCurrencyStr = currencies[selectedCurrency]
                Toast.makeText(this, "Currency set to: $selectedCurrencyStr", Toast.LENGTH_SHORT).show()
                // TODO: Save currency preference to SharedPreferences
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                AuthManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadUserData() {
        val currentUser = AuthManager.getCurrentUser()
        if (currentUser != null) {
            nameText.text = currentUser.name
            idText.text = "ID: ${currentUser.id}"
        } else {
            Log.e("ProfileActivity", "No user data found")
        }
    }
} 