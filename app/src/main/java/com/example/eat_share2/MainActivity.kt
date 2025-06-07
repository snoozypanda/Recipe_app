package com.example.eat_share2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.eat_share2.databinding.ActivityMainBinding
import com.example.eat_share2.repository.AuthRepository
import com.example.eat_share2.utils.TokenManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TokenManager and AuthRepository
        tokenManager = TokenManager(this)
        authRepository = AuthRepository(tokenManager)

        // Check if user is already logged in
        if (authRepository.isLoggedIn()) {
            navigateToHomepage()
            return
        }

        // Set up login button click listener
        binding.loginButton.setOnClickListener {
            performLogin()
        }

        // Edge-to-edge handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun performLogin() {
        val username = binding.username.text.toString().trim()
        val password = binding.password.text.toString().trim()

        // Validate input
        if (username.isEmpty()) {
            binding.username.error = "Username is required"
            return
        }

        if (password.isEmpty()) {
            binding.password.error = "Password is required"
            return
        }

        // Disable login button to prevent multiple requests
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Logging in..."

        // Perform login API call
        lifecycleScope.launch {
            authRepository.login(username, password)
                .onSuccess { loginResponse ->
                    // Login successful
                    Toast.makeText(this@MainActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToHomepage()
                }
                .onFailure { exception ->
                    // Login failed
                    Toast.makeText(this@MainActivity, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()

                    // Re-enable login button
                    binding.loginButton.isEnabled = true
                    binding.loginButton.text = "Login"
                }
        }
    }

    private fun navigateToHomepage() {
        startActivity(Intent(this, Homepage::class.java))
        finish() // Prevent going back to login screen
    }

    // This function handles the login button click from XML (keeping for backward compatibility)
    fun navigateToHomepage(view: View) {
        performLogin()
    }

    fun navigateToRegisterPage(view: View) {
        startActivity(Intent(this, RegistrationPage::class.java))
    }
}