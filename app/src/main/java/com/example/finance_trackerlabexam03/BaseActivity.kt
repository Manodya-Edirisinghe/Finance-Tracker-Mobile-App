package com.example.finance_trackerlabexam03

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupBottomNavigation() {
        try {
            // Set click listeners for each navigation item
            findViewById<LinearLayout>(R.id.homeNavItem)?.setOnClickListener {
                if (this !is HomeActivity) {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
            
            findViewById<LinearLayout>(R.id.statsNavItem)?.setOnClickListener {
                if (this !is AnalyserActivity) {
                    val intent = Intent(this, AnalyserActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
            
            findViewById<LinearLayout>(R.id.transferNavItem)?.setOnClickListener {
                if (this !is TransactionActivity) {
                    val intent = Intent(this, TransactionActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
            
            findViewById<LinearLayout>(R.id.walletNavItem)?.setOnClickListener {
                if (this !is CategoriesActivity) {
                    val intent = Intent(this, CategoriesActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
            
            findViewById<LinearLayout>(R.id.profileNavItem)?.setOnClickListener {
                if (this !is ProfileActivity) {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun navigateTo(activityClass: Class<*>) {
        if (this::class.java == activityClass) return
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    protected fun updateBottomNavigationState() {
        try {
            val defaultColor = ContextCompat.getColor(this, R.color.text_primary)
            val selectedColor = ContextCompat.getColor(this, R.color.primary_blue)
            
            // Get all navigation items
            val homeNavItem = findViewById<LinearLayout>(R.id.homeNavItem)
            val statsNavItem = findViewById<LinearLayout>(R.id.statsNavItem)
            val transferNavItem = findViewById<LinearLayout>(R.id.transferNavItem)
            val walletNavItem = findViewById<LinearLayout>(R.id.walletNavItem)
            val profileNavItem = findViewById<LinearLayout>(R.id.profileNavItem)
            
            // Get all ImageViews and TextViews
            val homeIcon = homeNavItem?.findViewById<ImageView>(R.id.homeIcon)
            val statsIcon = statsNavItem?.findViewById<ImageView>(R.id.statsIcon)
            val transferIcon = transferNavItem?.findViewById<ImageView>(R.id.transferIcon)
            val walletIcon = walletNavItem?.findViewById<ImageView>(R.id.walletIcon)
            val profileIcon = profileNavItem?.findViewById<ImageView>(R.id.profileIcon)
            
            val homeText = homeNavItem?.getChildAt(1) as? TextView
            val statsText = statsNavItem?.getChildAt(1) as? TextView
            val transferText = transferNavItem?.getChildAt(1) as? TextView
            val walletText = walletNavItem?.getChildAt(1) as? TextView
            val profileText = profileNavItem?.getChildAt(1) as? TextView
            
            // Reset all icons and text
            homeIcon?.setColorFilter(defaultColor)
            statsIcon?.setColorFilter(defaultColor)
            transferIcon?.setColorFilter(defaultColor)
            walletIcon?.setColorFilter(defaultColor)
            profileIcon?.setColorFilter(defaultColor)
            
            homeText?.setTextColor(defaultColor)
            statsText?.setTextColor(defaultColor)
            transferText?.setTextColor(defaultColor)
            walletText?.setTextColor(defaultColor)
            profileText?.setTextColor(defaultColor)
            
            // Highlight selected icon and text based on current activity
            when (this) {
                is HomeActivity -> {
                    homeIcon?.setColorFilter(selectedColor)
                    homeText?.setTextColor(selectedColor)
                }
                is AnalyserActivity -> {
                    statsIcon?.setColorFilter(selectedColor)
                    statsText?.setTextColor(selectedColor)
                }
                is TransactionActivity -> {
                    transferIcon?.setColorFilter(selectedColor)
                    transferText?.setTextColor(selectedColor)
                }
                is CategoriesActivity -> {
                    walletIcon?.setColorFilter(selectedColor)
                    walletText?.setTextColor(selectedColor)
                }
                is ProfileActivity -> {
                    profileIcon?.setColorFilter(selectedColor)
                    profileText?.setTextColor(selectedColor)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
} 