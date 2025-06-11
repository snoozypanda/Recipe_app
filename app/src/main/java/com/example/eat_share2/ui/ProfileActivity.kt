package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eat_share2.R
import com.example.eat_share2.databinding.ActivityProfileBinding
import com.example.eat_share2.data.repository.AuthRepository
import com.example.eat_share2.utils.TokenManager

class ProfileActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TokenManager and AuthRepository
        tokenManager = TokenManager(this)
        authRepository = AuthRepository(tokenManager)

        setupWindowInsets()
        setupClickListeners()
        loadUserData()
        setupBottomNavigation()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        // Edit Profile
        binding.editProfileLayout.setOnClickListener {
            showToast("Edit Profile feature coming soon!")
        }

        // My Recipes
        binding.myRecipesLayout.setOnClickListener {
            showToast("My Recipes feature coming soon!")
        }

        // Settings
        binding.settingsLayout.setOnClickListener {
            showToast("Settings feature coming soon!")
        }

        // Logout with confirmation
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        // Delete Account with confirmation
        binding.deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun loadUserData() {
        try {
            // Get user data from TokenManager
            val fullName = tokenManager.getFullName()
//            val username = tokenManager.getUsername()
            val email = tokenManager.getEmail()

            // Display user information
            binding.profileName.text = when {
                !fullName.isNullOrBlank() -> fullName
//                !username.isNullOrBlank() -> username
                else -> "User"
            }

            binding.profileEmail.text = email ?: "No email available"

            // Show loading state briefly then update
            binding.profileName.text = binding.profileName.text
            binding.profileEmail.text = binding.profileEmail.text

        } catch (e: Exception) {
            binding.profileName.text = "Error loading profile"
            binding.profileEmail.text = "Please try again"
            showToast("Failed to load profile data")
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        try {
            // Clear token and user data
            authRepository.logout()

            // Show success message
            showToast("Logged out successfully")

            // Navigate back to login screen
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            showToast("Error during logout: ${e.message}")
        }
    }

    private fun performDeleteAccount() {
        // For now, just show a message since this would require API integration
        showToast("Delete account feature will be implemented with backend integration")

        // In a real implementation, you would:
        // 1. Call API to delete account
        // 2. Clear local data
        // 3. Navigate to login screen
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when returning to this activity
        loadUserData()
    }
}