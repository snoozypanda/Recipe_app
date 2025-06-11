package com.example.eat_share2.ui.viewmodels

import android.app.Application
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Fetch recipe details
                recipeRepository.getRecipeDetailById(recipeId)
                    .onSuccess { recipeDetail ->
                        _uiState.value = _uiState.value.copy(
                            recipeDetail = recipeDetail,
                            isLoading = false
                        )

                        // Fetch user details
                        loadUserDetails(recipeDetail.userId)
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load recipe details"
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

    private fun loadUserDetails(userId: String) {
        viewModelScope.launch {
            try {
                recipeRepository.getUserById(userId)
                    .onSuccess { userDetail ->
                        _uiState.value = _uiState.value.copy(userDetail = userDetail)
                    }
                    .onFailure { exception ->
                        // Don't show error for user details failure, just log it
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