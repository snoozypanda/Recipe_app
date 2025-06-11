package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eat_share2.R
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (this !is HomepageActivity) {
                        navigateToActivity(HomepageActivity::class.java)
                    }
                    true
                }
                R.id.nav_favorite -> {
                    if (this !is FavoriteActivity) {
                        navigateToActivity(FavoriteActivity::class.java)
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (this !is ProfileActivity) {
                        navigateToActivity(ProfileActivity::class.java)
                    }
                    true
                }
                else -> false
            }
        }

        // Set the selected item based on current activity
        setSelectedNavigationItem()
    }

    private fun setSelectedNavigationItem() {
        when (this) {
            is HomepageActivity -> bottomNavigationView.selectedItemId = R.id.nav_home
            is FavoriteActivity -> bottomNavigationView.selectedItemId = R.id.nav_favorite
            is ProfileActivity -> bottomNavigationView.selectedItemId = R.id.nav_profile
        }
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNavigationView.isInitialized) {
            setSelectedNavigationItem()
        }
    }
}