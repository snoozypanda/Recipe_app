package com.example.eat_share2.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eat_share2.data.models.AddIngredient
import com.example.eat_share2.data.models.AddStep
import com.example.eat_share2.data.repository.RecipeRepository
import com.example.eat_share2.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddRecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val recipeRepository = RecipeRepository(tokenManager)

    private val _uiState = MutableStateFlow(AddRecipeUiState())
    val uiState: StateFlow<AddRecipeUiState> = _uiState.asStateFlow()

    private val _ingredients = MutableStateFlow<List<AddIngredient>>(emptyList())
    val ingredients: StateFlow<List<AddIngredient>> = _ingredients.asStateFlow()

    private val _steps = MutableStateFlow<List<AddStep>>(emptyList())
    val steps: StateFlow<List<AddStep>> = _steps.asStateFlow()

    // Combined flow to check if save button should be enabled
    val canSave: StateFlow<Boolean> = combine(_ingredients, _steps) { ingredients, steps ->
        val hasValidIngredient = ingredients.any { it.name.isNotBlank() && it.quantity.isNotBlank() }
        val hasValidStep = steps.any { it.instruction.isNotBlank() }
        hasValidIngredient && hasValidStep
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun updateIngredients(newIngredients: List<AddIngredient>) {
        _ingredients.value = newIngredients
    }

    fun updateSteps(newSteps: List<AddStep>) {
        _steps.value = newSteps
    }

    fun saveRecipe(
        name: String,
        description: String,
        ingredients: List<AddIngredient>,
        steps: List<AddStep>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Check if user is logged in
                val currentUserId = tokenManager.getUserId()
                if (currentUserId.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "You must be logged in to create a recipe. Please log in and try again."
                    )
                    return@launch
                }

                Log.d("AddRecipeViewModel", "Creating recipe for user ID: $currentUserId")

                // Validate ingredients
                val validIngredients = ingredients.filter {
                    it.name.trim().isNotBlank() && it.quantity.trim().isNotBlank()
                }

                if (validIngredients.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Please add at least one ingredient with both name and quantity"
                    )
                    return@launch
                }

                // Validate steps
                val validSteps = steps.filter {
                    it.instruction.trim().isNotBlank()
                }.map { it.instruction }

                if (validSteps.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Please add at least one instruction step"
                    )
                    return@launch
                }

                Log.d("AddRecipeViewModel", "Saving recipe with ${validIngredients.size} ingredients and ${validSteps.size} steps")

                // Save recipe (user ID will be added in the repository)
                recipeRepository.createRecipe(name, description, validIngredients, validSteps)
                    .onSuccess { recipe ->
                        Log.d("AddRecipeViewModel", "Recipe created successfully: ${recipe.id}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                    .onFailure { exception ->
                        Log.e("AddRecipeViewModel", "Failed to create recipe", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to save recipe"
                        )
                    }
            } catch (e: Exception) {
                Log.e("AddRecipeViewModel", "Exception while creating recipe", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AddRecipeUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)