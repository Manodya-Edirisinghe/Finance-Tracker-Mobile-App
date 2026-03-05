package com.example.finance_trackerlabexam03

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finance_trackerlabexam03.auth.AuthManager

class SignupActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize AuthManager if not already initialized
        try {
            AuthManager.initialize(applicationContext)
        } catch (e: Exception) {
            Log.e("SignupActivity", "Failed to initialize AuthManager", e)
            Toast.makeText(this, "Failed to initialize app. Please try again.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText)
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signupButton)
        loginText = findViewById(R.id.tvLogin)

        // Set up back button click listener
        findViewById<android.widget.ImageButton>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Set up sign up button click listener
        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            Log.d("SignupActivity", "Attempting signup with username: $username, email: $email")

            // Validate input
            if (name.isEmpty()) {
                nameEditText.error = "Please enter your name"
                return@setOnClickListener
            }

            if (username.isEmpty()) {
                usernameEditText.error = "Please enter a username"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailEditText.error = "Please enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Please enter a password"
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.error = "Please confirm your password"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Passwords do not match"
                return@setOnClickListener
            }

            try {
                // Attempt to sign up
                val result = AuthManager.signUp(username, email, password, name)
                result.onSuccess { user ->
                    Log.d("SignupActivity", "Signup successful for user: ${user.username}")
                    Toast.makeText(this, "Sign up successful! Please login.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }.onFailure { exception ->
                    Log.e("SignupActivity", "Signup failed: ${exception.message}")
                    Toast.makeText(this, exception.message ?: "Sign up failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SignupActivity", "Exception during signup", e)
                Toast.makeText(this, "An error occurred during signup. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up login text click listener
        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}