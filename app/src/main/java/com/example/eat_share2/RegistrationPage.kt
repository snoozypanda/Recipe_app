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
import com.example.eat_share2.databinding.ActivityRegistrationPageBinding
import com.example.eat_share2.repository.AuthRepository
import com.example.eat_share2.utils.TokenManager
import kotlinx.coroutines.launch

class RegistrationPage : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager

    private  lateinit var binding: ActivityRegistrationPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrationPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TokenManager and AuthRepository
        tokenManager = TokenManager(this)
        authRepository = AuthRepository(tokenManager)

        // Set up register button click listener
        binding.registerButton.setOnClickListener {
            performSignup()
        }

        // Handle padding for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun performSignup() {
        val fullName = binding.fullName.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val username = binding.registerUsername.text.toString().trim()
        val password = binding.registerPassword.text.toString().trim()

        // Validate input
        if (fullName.isEmpty()) {
            binding.fullName.error = "Full name is required"
            return
        }

        if (email.isEmpty()) {
            binding.email.error = "Email is required"
            return
        }

        if (!isValidEmail(email)) {
            binding.email.error = "Please enter a valid email"
            return
        }

        if (username.isEmpty()) {
            binding.registerUsername.error = "Username is required"
            return
        }

        if (password.isEmpty()) {
            binding.registerPassword.error = "Password is required"
            return
        }

        if (password.length < 6) {
            binding.registerPassword.error = "Password must be at least 6 characters"
            return
        }

        // Disable register button to prevent multiple requests
        binding.registerButton.isEnabled = false
        binding.registerButton.text = "Creating Account..."

        // Perform signup API call
        lifecycleScope.launch {
            authRepository.signup(fullName, email, username, password)
                .onSuccess { signupResponse ->
                    // Signup successful
                    Toast.makeText(this@RegistrationPage,
                        signupResponse.message ?: "Account created successfully!",
                        Toast.LENGTH_SHORT).show()

                    // Check if auto-login happened (token was returned)
                    if (signupResponse.token != null) {
                        // Navigate to homepage (auto-logged in)
                        navigateToHomepage()
                    } else {
                        // Navigate to login page
                        navigateToLoginPage()
                    }
                }
                .onFailure { exception ->
                    // Signup failed
                    Toast.makeText(this@RegistrationPage,
                        "Signup failed: ${exception.message}",
                        Toast.LENGTH_LONG).show()

                    // Re-enable register button
                    binding.registerButton.isEnabled = true
                    binding.registerButton.text = "Register"
                }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToHomepage() {
        val intent = Intent(this, Homepage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Method for the Login Link (keeping for backward compatibility)
    fun navigateToLoginPage(view: View) {
        navigateToLoginPage()
    }
}