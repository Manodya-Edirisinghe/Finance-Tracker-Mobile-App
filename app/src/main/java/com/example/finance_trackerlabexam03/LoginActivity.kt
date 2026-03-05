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

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpText: TextView
    private lateinit var forgotPasswordText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginActivity", "onCreate called")
        setContentView(R.layout.activity_login)

        // Initialize AuthManager
        AuthManager.initialize(applicationContext)

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpText = findViewById(R.id.signUpText)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)

        // Set up back button click listener
        findViewById<android.widget.ImageButton>(R.id.backButton).setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Set up login button click listener
        loginButton.setOnClickListener {
            val identifier = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (identifier.isEmpty()) {
                emailEditText.error = "Please enter your email or username"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Please enter your password"
                return@setOnClickListener
            }

            Log.d("LoginActivity", "Attempting login with identifier: $identifier")
            // Attempt to login
            val result = AuthManager.login(identifier, password)
            result.onSuccess { user ->
                Log.d("LoginActivity", "Login successful for user: ${user.username}")
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                
                // Create intent for HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                // Clear the activity stack and start a new task
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure { exception ->
                Log.e("LoginActivity", "Login failed: ${exception.message}")
                Log.e("LoginActivity", "Stack trace: ${exception.stackTraceToString()}")
                Toast.makeText(this, exception.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up sign up text click listener
        signUpText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Set up forgot password text click listener
        forgotPasswordText.setOnClickListener {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }
}