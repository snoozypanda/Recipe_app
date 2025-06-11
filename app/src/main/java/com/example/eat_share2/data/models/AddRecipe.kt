package com.example.eat_share2.data.models

data class AddIngredient(
    val name: String = "",
    val quantity: String = ""
)

data class AddStep(
    val instruction: String = ""
)

data class CreateRecipeRequest(
    val name: String,
    val description: String,
    val ingredients: List<AddIngredient>,
    val steps: List<String>,
    val userId: String // Add user ID to the request
)

data class CreateRecipeResponse(
    val message: String,
    val recipe: ApiRecipe? = null
)