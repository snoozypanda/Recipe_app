package com.example.eat_share2.data.models

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val prepTime: String,
    val rating: Float,
    val reviewCount: Int,
    val category: String,
    val difficulty: String = "Medium",
    val servings: Int = 4,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val isFavorite: Boolean = false
)

data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val color: String = "#4CAF50"
)