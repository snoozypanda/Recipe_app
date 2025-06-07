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

class MainActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize TokenManager and AuthRepository
        tokenManager = TokenManager(this)
        authRepository = AuthRepository(tokenManager)

        // Check if user is already logged in
        if (authRepository.isLoggedIn()) {
            navigateToHomepage()
            return
        }

        // Initialize views
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)

        // Set up login button click listener
        loginButton.setOnClickListener {
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
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate input
        if (username.isEmpty()) {
            usernameEditText.error = "Username is required"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        // Disable login button to prevent multiple requests
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."

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
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
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
        startActivity(Intent(this, Registration_page::class.java))
    }
}