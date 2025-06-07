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
import com.example.eat_share2.repository.AuthRepository
import com.example.eat_share2.utils.TokenManager
import kotlinx.coroutines.launch

class RegistrationPage : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration_page)

        // Initialize TokenManager and AuthRepository
        tokenManager = TokenManager(this)
        authRepository = AuthRepository(tokenManager)

        // Initialize views
        fullNameEditText = findViewById(R.id.fullName)
        emailEditText = findViewById(R.id.email)
        usernameEditText = findViewById(R.id.registerUsername)
        passwordEditText = findViewById(R.id.registerPassword)
        registerButton = findViewById(R.id.registerButton)

        // Set up register button click listener
        registerButton.setOnClickListener {
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
        val fullName = fullNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate input
        if (fullName.isEmpty()) {
            fullNameEditText.error = "Full name is required"
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }

        if (!isValidEmail(email)) {
            emailEditText.error = "Please enter a valid email"
            return
        }

        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return
        }

        // Disable register button to prevent multiple requests
        registerButton.isEnabled = false
        registerButton.text = "Creating Account..."

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
                    registerButton.isEnabled = true
                    registerButton.text = "Register"
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