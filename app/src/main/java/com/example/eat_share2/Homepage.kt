package com.example.eat_share2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.search.SearchBar

class Homepage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation buttons
        findViewById<Button>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, Homepage::class.java))
        }
        findViewById<Button>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, profile::class.java))
        }
        findViewById<Button>(R.id.nav_favorite).setOnClickListener {
            startActivity(Intent(this, Favorite::class.java))
        }

        // Search bar
        val searchBar = findViewById<SearchBar>(R.id.searchBar)
        searchBar.setOnClickListener {
            // Handle search activation
        }

        // Recipe card clicks
        val spaghettiCard = findViewById<CardView>(R.id.spaghetticard)
        spaghettiCard.setOnClickListener {
            val intent = Intent(this, ViewRecipe::class.java)
            intent.putExtra("recipeName", "Spaghetti Carbonara")
            startActivity(intent)
        }
    }
}
