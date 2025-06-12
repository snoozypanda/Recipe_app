package com.example.eat_share2.data.models

data class RecipeDetailApiResponse(
    val message: String,
    val recipe: RecipeDetailResponse
)

data class RecipeDetailResponse(
    val id: String,
    val name: String,
    val description: String,
    val userId: String? = null,
    val steps: List<RecipeStep>,
    val ingredients: List<RecipeIngredient>
)

data class RecipeStep(
    val instruction: String,
    val stepNumber: Int
)

data class RecipeIngredient(
    val name: String,
    val quantity: String
)

data class UserResponse(
    val message: String,
    val user: UserDetail
)

data class UserDetail(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String
)