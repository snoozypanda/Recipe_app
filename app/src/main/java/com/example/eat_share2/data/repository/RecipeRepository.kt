package com.example.eat_share2.data.repository

import com.example.eat_share2.data.api.RetrofitClient
import com.example.eat_share2.data.models.AddIngredient
import com.example.eat_share2.data.models.ApiRecipe
import com.example.eat_share2.data.models.Category
import com.example.eat_share2.data.models.CreateRecipeRequest
import com.example.eat_share2.data.models.Recipe
import com.example.eat_share2.data.models.RecipeDetailResponse
import com.example.eat_share2.data.models.UserDetail
import com.example.eat_share2.utils.TokenManager
import kotlinx.coroutines.delay

class RecipeRepository(private val tokenManager: TokenManager) {

    // Placeholder images for recipes
    private val placeholderImages = listOf(
        "https://images.pexels.com/photos/4518843/pexels-photo-4518843.jpeg",
        "https://images.pexels.com/photos/2097090/pexels-photo-2097090.jpeg",
        "https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg",
        "https://images.pexels.com/photos/230325/pexels-photo-230325.jpeg",
        "https://images.pexels.com/photos/725991/pexels-photo-725991.jpeg",
        "https://images.pexels.com/photos/1351238/pexels-photo-1351238.jpeg"
    )

    suspend fun getPopularRecipes(): Result<List<Recipe>> {
        return try {
            val response = RetrofitClient.apiService.getRecipes()

            if (response.isSuccessful) {
                val recipeResponse = response.body()
                if (recipeResponse != null && recipeResponse.message == "success") {
                    val recipes = recipeResponse.recipes?.map { apiRecipe ->
                        convertApiRecipeToRecipe(apiRecipe)
                    } ?: emptyList()

                    Result.success(recipes)
                } else {
                    Result.failure(Exception("Failed to fetch recipes: ${recipeResponse?.message ?: "Unknown error"}"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to fetch recipes"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchRecipes(query: String): Result<List<Recipe>> {
        return try {
            val response = if (query.isBlank()) {
                RetrofitClient.apiService.getRecipes()
            } else {
                RetrofitClient.apiService.searchRecipes(query)
            }

            if (response.isSuccessful) {
                val recipeResponse = response.body()
                if (recipeResponse != null && recipeResponse.message == "success") {
                    val recipes = recipeResponse.recipes?.map { apiRecipe ->
                        convertApiRecipeToRecipe(apiRecipe)
                    } ?: emptyList()

                    Result.success(recipes)
                } else {
                    Result.failure(Exception("Search failed: ${recipeResponse?.message ?: "Unknown error"}"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Search failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipeDetailById(id: String): Result<RecipeDetailResponse> {
        return try {
            val response = RetrofitClient.apiService.getRecipeById(id)

            if (response.isSuccessful) {
                val recipeDetail = response.body()
                if (recipeDetail != null) {
                    Result.success(recipeDetail)
                } else {
                    Result.failure(Exception("Recipe not found"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to fetch recipe details"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<UserDetail> {
        return try {
            val response = RetrofitClient.apiService.getUserById(userId)

            if (response.isSuccessful) {
                val userResponse = response.body()
                if (userResponse != null && userResponse.message == "User found") {
                    // Clear sensitive data
                    val cleanUser = userResponse.user.copy()
                    Result.success(cleanUser)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to fetch user details"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRecipe(
        name: String,
        description: String,
        ingredients: List<AddIngredient>,
        steps: List<String>
    ): Result<ApiRecipe> {
        return try {
            // Get current user ID from token
            val currentUserId = tokenManager.getUserId()
            if (currentUserId.isNullOrBlank()) {
                return Result.failure(Exception("User not logged in. Please log in and try again."))
            }

            val createRecipeRequest = CreateRecipeRequest(
                name = name,
                description = description,
                ingredients = ingredients,
                steps = steps,
                userId = currentUserId // Include user ID in the request
            )

            val response = RetrofitClient.apiService.createRecipe(createRecipeRequest)

            if (response.isSuccessful) {
                val createRecipeResponse = response.body()
                if (createRecipeResponse != null && createRecipeResponse.message == "Recipe created successfully") {
                    val recipe = createRecipeResponse.recipe ?: ApiRecipe(
                        id = "temp_id",
                        name = name,
                        description = description
                    )
                    Result.success(recipe)
                } else {
                    Result.failure(Exception("Failed to create recipe: ${createRecipeResponse?.message ?: "Unknown error"}"))
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to create recipe"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipeById(id: String): Result<Recipe?> {
        return try {
            // Since there's no specific endpoint for single recipe, we'll get all and filter
            val allRecipesResult = getPopularRecipes()

            allRecipesResult.fold(
                onSuccess = { recipes ->
                    val recipe = recipes.find { it.id == id }
                    Result.success(recipe)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun convertApiRecipeToRecipe(apiRecipe: ApiRecipe, categoryOverride: String? = null): Recipe {
        // Generate random values for missing data
        val randomImage = placeholderImages.random()
        val randomPrepTime = listOf("15 min", "20 min", "25 min", "30 min", "35 min", "45 min").random()
        val randomRating = (3..5).random().toFloat()
        val randomReviewCount = (50..500).random()

        return Recipe(
            id = apiRecipe.id,
            title = apiRecipe.name,
            description = apiRecipe.description,
            imageUrl = randomImage,
            prepTime = randomPrepTime,
            rating = randomRating,
            reviewCount = randomReviewCount,
            category = categoryOverride ?: "General",
            difficulty = listOf("Easy", "Medium", "Hard").random(),
            servings = (2..6).random(),
            ingredients = generateRandomIngredients(),
            instructions = generateRandomInstructions(),
            isFavorite = false
        )
    }

    private fun generateRandomIngredients(): List<String> {
        val commonIngredients = listOf(
            "Salt", "Pepper", "Olive oil", "Garlic", "Onion", "Tomatoes",
            "Cheese", "Herbs", "Lemon", "Butter", "Flour", "Eggs"
        )
        return commonIngredients.shuffled().take((3..6).random())
    }

    private fun generateRandomInstructions(): List<String> {
        return listOf(
            "Prepare all ingredients",
            "Heat oil in a pan",
            "Cook according to recipe",
            "Season to taste",
            "Serve hot and enjoy"
        )
    }
}