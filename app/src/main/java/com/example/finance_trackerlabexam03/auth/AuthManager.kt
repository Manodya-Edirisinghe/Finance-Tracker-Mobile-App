package com.example.finance_trackerlabexam03.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.finance_trackerlabexam03.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import java.util.regex.Pattern

object AuthManager {
    private const val PREFS_NAME = "FinanceTrackerPrefs"
    private const val KEY_USERS = "users"
    private const val KEY_CURRENT_USER = "current_user"
    
    private lateinit var prefs: SharedPreferences
    private val users = mutableListOf<User>()
    private var currentUser: User? = null
    private val gson = Gson()

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadUsers()
        loadCurrentUser()
    }

    private fun loadUsers() {
        try {
            val usersJson = prefs.getString(KEY_USERS, "[]")
            val type = object : TypeToken<List<User>>() {}.type
            val loadedUsers: List<User> = gson.fromJson(usersJson, type)
            users.clear()
            users.addAll(loadedUsers)
            Log.d("AuthManager", "Loaded ${users.size} users")
        } catch (e: Exception) {
            Log.e("AuthManager", "Error loading users", e)
            users.clear()
            saveUsers()
        }
    }

    private fun saveUsers() {
        try {
            val usersJson = gson.toJson(users)
            prefs.edit().putString(KEY_USERS, usersJson).apply()
            Log.d("AuthManager", "Saved ${users.size} users")
        } catch (e: Exception) {
            Log.e("AuthManager", "Error saving users", e)
        }
    }

    private fun loadCurrentUser() {
        try {
            val currentUserJson = prefs.getString(KEY_CURRENT_USER, null)
            currentUser = if (currentUserJson != null) {
                gson.fromJson(currentUserJson, User::class.java)
            } else null
            Log.d("AuthManager", "Loaded current user: ${currentUser?.username}")
        } catch (e: Exception) {
            Log.e("AuthManager", "Error loading current user", e)
            currentUser = null
            saveCurrentUser()
        }
    }

    private fun saveCurrentUser() {
        try {
            val currentUserJson = currentUser?.let { gson.toJson(it) }
            prefs.edit().putString(KEY_CURRENT_USER, currentUserJson).apply()
            Log.d("AuthManager", "Saved current user: ${currentUser?.username}")
        } catch (e: Exception) {
            Log.e("AuthManager", "Error saving current user", e)
        }
    }

    fun signUp(username: String, email: String, password: String, name: String): Result<User> {
        // Validate input
        if (username.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters long"))
        }

        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters long"))
        }

        // Check if username or email already exists
        if (users.any { it.username == username }) {
            return Result.failure(IllegalArgumentException("Username already exists"))
        }

        if (users.any { it.email == email }) {
            return Result.failure(IllegalArgumentException("Email already exists"))
        }

        // Create new user
        val user = User(
            id = UUID.randomUUID().toString(),
            username = username,
            email = email,
            password = password, // In a real app, this should be hashed
            name = name
        )

        users.add(user)
        saveUsers()
        return Result.success(user)
    }

    fun register(username: String, email: String, password: String): Result<User> {
        return signUp(username, email, password, "")
    }

    fun login(identifier: String, password: String): Result<User> {
        Log.d("AuthManager", "Attempting login with identifier: $identifier")
        Log.d("AuthManager", "Current users list size: ${users.size}")
        users.forEach { user ->
            Log.d("AuthManager", "Checking user: ${user.username}, email: ${user.email}")
        }

        // Find user by username or email
        val user = users.find { it.username == identifier || it.email == identifier }
            ?: return Result.failure(IllegalArgumentException("User not found"))

        Log.d("AuthManager", "User found: ${user.username}")

        // Verify password
        if (user.password != password) {
            Log.d("AuthManager", "Password verification failed")
            return Result.failure(IllegalArgumentException("Invalid password"))
        }

        currentUser = user
        saveCurrentUser()
        Log.d("AuthManager", "Login successful for user: ${user.username}")
        return Result.success(user)
    }

    fun logout() {
        currentUser = null
        saveCurrentUser()
    }

    fun isLoggedIn(): Boolean {
        return currentUser != null
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun updateUser(updatedUser: User) {
        // Find and update the user in the list
        val index = users.indexOfFirst { it.id == updatedUser.id }
        if (index != -1) {
            users[index] = updatedUser
            saveUsers()
            
            // If this is the current user, update it as well
            if (currentUser?.id == updatedUser.id) {
                currentUser = updatedUser
                saveCurrentUser()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
        )
        return emailPattern.matcher(email).matches()
    }
} 