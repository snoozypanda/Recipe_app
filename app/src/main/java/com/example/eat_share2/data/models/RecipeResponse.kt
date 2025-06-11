package com.example.eat_share2.data.models

data class RecipeResponse(
    val message: String,
    val recipes: List<ApiRecipe>?
)

data class ApiRecipe(
    val id: String,
    val name: String,
    val description: String
)