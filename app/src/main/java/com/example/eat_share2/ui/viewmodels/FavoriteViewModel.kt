package com.example.eat_share2.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eat_share2.data.models.Recipe
import com.example.eat_share2.data.repository.RecipeRepository
import com.example.eat_share2.utils.TokenManager
import com.example.eat_share2.utils.RatingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val recipeRepository = RecipeRepository(tokenManager)
    private val ratingManager = RatingManager(application)

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    init {
        loadFavoriteRecipes()
    }

    private fun loadFavoriteRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load all recipes and sort by rating (highest first)
                recipeRepository.getPopularRecipes()
                    .onSuccess { allRecipes ->
                        // Apply any saved rating updates
                        val updatedRecipes = applyRatingUpdates(allRecipes)
                        val sortedRecipes = updatedRecipes.sortedByDescending { it.rating }
                        _recipes.value = sortedRecipes
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load favorite recipes"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun applyRatingUpdates(recipes: List<Recipe>): List<Recipe> {
        val ratingUpdates = ratingManager.getRatingUpdates()

        return recipes.map { recipe ->
            val update = ratingUpdates[recipe.id]
            if (update != null) {
                recipe.copy(
                    rating = update.newRating,
                    reviewCount = update.newReviewCount
                )
            } else {
                recipe
            }
        }
    }

    fun refreshData() {
        loadFavoriteRecipes()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun updateRecipeRating(recipeId: String, newRating: Float, newReviewCount: Int) {
        // Save the rating update
        ratingManager.saveRatingUpdate(recipeId, newRating, newReviewCount)

        // Update the current list
        val currentRecipes = _recipes.value.toMutableList()
        val recipeIndex = currentRecipes.indexOfFirst { it.id == recipeId }

        if (recipeIndex != -1) {
            val updatedRecipe = currentRecipes[recipeIndex].copy(
                rating = newRating,
                reviewCount = newReviewCount
            )
            currentRecipes[recipeIndex] = updatedRecipe

            // Re-sort by rating and update the list
            val sortedRecipes = currentRecipes.sortedByDescending { it.rating }
            _recipes.value = sortedRecipes
        }
    }
}

data class FavoriteUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)