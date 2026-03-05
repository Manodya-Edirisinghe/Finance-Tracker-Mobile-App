package com.example.finance_trackerlabexam03

import android.app.Application
import com.example.finance_trackerlabexam03.auth.AuthManager

class FinanceTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AuthManager when the application starts
        AuthManager.initialize(applicationContext)
    }
} 