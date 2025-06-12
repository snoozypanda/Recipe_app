package com.example.eat_share2.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eat_share2.data.models.RecipeDetailResponse
import com.example.eat_share2.data.models.UserDetail
import com.example.eat_share2.data.repository.RecipeRepository
import com.example.eat_share2.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewRecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val recipeRepository = RecipeRepository(tokenManager)

    private val _uiState = MutableStateFlow(ViewRecipeUiState())
    val uiState: StateFlow<ViewRecipeUiState> = _uiState.asStateFlow()

    fun loadRecipeDetails(recipeId: String) {
        Log.d("ViewRecipeViewModel", "Loading recipe details for ID: $recipeId")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Fetch recipe details
                recipeRepository.getRecipeDetailById(recipeId)
                    .onSuccess { recipeDetail ->
                        Log.d("ViewRecipeViewModel", "Recipe loaded successfully: ${recipeDetail.name}")
                        _uiState.value = _uiState.value.copy(
                            recipeDetail = recipeDetail,
                            isLoading = false
                        )

                        // Fetch user details if userId is available
                        recipeDetail.userId?.let { userId ->
                            loadUserDetails(userId)
                        } ?: run {
                            // Set default user if no userId
                            _uiState.value = _uiState.value.copy(
                                userDetail = UserDetail(
                                    id = "unknown",
                                    name = "Unknown Chef",
                                    email = "",
                                    createdAt = ""
                                )
                            )
                        }
                    }
                    .onFailure { exception ->
                        Log.e("ViewRecipeViewModel", "Failed to load recipe", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load recipe details"
                        )
                    }
            } catch (e: Exception) {
                Log.e("ViewRecipeViewModel", "Exception while loading recipe", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.getUserById(userId)
                    .onSuccess { userDetail ->
                        Log.d("ViewRecipeViewModel", "User loaded successfully: ${userDetail.name}")
                        _uiState.value = _uiState.value.copy(userDetail = userDetail)
                    }
                    .onFailure { exception ->
                        Log.w("ViewRecipeViewModel", "Failed to load user details", exception)
                        // Don't show error for user details failure, just use fallback
                        _uiState.value = _uiState.value.copy(
                            userDetail = UserDetail(
                                id = userId,
                                name = "Unknown Chef",
                                email = "",
                                createdAt = ""
                            )
                        )
                    }
            } catch (e: Exception) {
                Log.w("ViewRecipeViewModel", "Exception while loading user details", e)
                // Fallback user detail
                _uiState.value = _uiState.value.copy(
                    userDetail = UserDetail(
                        id = userId,
                        name = "Unknown Chef",
                        email = "",
                        createdAt = ""
                    )
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ViewRecipeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val recipeDetail: RecipeDetailResponse? = null,
    val userDetail: UserDetail? = null
)