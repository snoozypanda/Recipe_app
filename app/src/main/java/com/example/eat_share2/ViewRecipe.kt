package com.example.eat_share2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class ViewRecipe : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipie)

        val recipeName = intent.getStringExtra("recipeName")

    }
}
