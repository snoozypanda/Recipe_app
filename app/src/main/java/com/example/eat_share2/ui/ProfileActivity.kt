package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eat_share2.R
import com.example.eat_share2.repository.AuthRepository
import com.example.eat_share2.utils.TokenManager

class ProfileActivity : BaseActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Initialize TokenManager and AuthRepository
        tokenManager = TokenManager(this)
        authRepository = AuthRepository(tokenManager)

        // Load user data
        loadUserData()

        // Set up logout button
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logout()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup bottom navigation
        setupBottomNavigation()
    }

    private fun loadUserData() {
        // Display user information from cache
        findViewById<TextView>(R.id.profileName).text = tokenManager.getFullName() ?: tokenManager.getUsername() ?: "User"
        findViewById<TextView>(R.id.profileEmail).text = tokenManager.getEmail() ?: "No email available"
    }

    private fun logout() {
        // Clear token and user data
        authRepository.logout()

        // Navigate back to login screen
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}