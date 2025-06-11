package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.eat_share2.R
import com.google.android.material.search.SearchBar

class HomepageActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup bottom navigation
        setupBottomNavigation()

        // Search bar
        val searchBar = findViewById<SearchBar>(R.id.searchBar)
        searchBar.setOnClickListener {
            // Handle search activation
        }

        // Recipe card clicks
        val spaghettiCard = findViewById<CardView>(R.id.spaghetticard)
        spaghettiCard.setOnClickListener {
            val intent = Intent(this, ViewRecipeActivity::class.java)
            intent.putExtra("recipeName", "Spaghetti Carbonara")
            startActivity(intent)
        }
    }
}