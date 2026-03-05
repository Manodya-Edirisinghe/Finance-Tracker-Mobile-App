package com.example.finance_trackerlabexam03

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.finance_trackerlabexam03.auth.AuthManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "Initializing AuthManager")
        
        // Initialize AuthManager
        AuthManager.initialize(applicationContext)
        
        // Check if user is already logged in
        val isLoggedIn = AuthManager.isLoggedIn()
        Log.d("MainActivity", "User logged in: $isLoggedIn")
        
        if (isLoggedIn) {
            // User is logged in, navigate to HomeActivity
            Log.d("MainActivity", "Navigating to HomeActivity")
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // User is not logged in, navigate to LoginActivity
            Log.d("MainActivity", "Navigating to LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
} 