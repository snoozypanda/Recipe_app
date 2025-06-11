package com.example.eat_share2.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eat_share2.R

class ViewRecipeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipie)

        val recipeName = intent.getStringExtra("recipeName")
    }
}