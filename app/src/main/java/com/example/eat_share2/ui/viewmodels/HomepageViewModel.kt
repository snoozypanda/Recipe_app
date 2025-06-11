package com.example.eat_share2.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eat_share2.data.models.Recipe
import com.example.eat_share2.data.repository.RecipeRepository
import com.example.eat_share2.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomepageViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val recipeRepository = RecipeRepository(tokenManager)

    private val _uiState = MutableStateFlow(HomepageUiState())
    val uiState: StateFlow<HomepageUiState> = _uiState.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private var allRecipes: List<Recipe> = emptyList()
    private var currentSearchQuery: String = ""

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load recipes from API
                recipeRepository.getPopularRecipes()
                    .onSuccess { recipes ->
                        allRecipes = recipes
                        _recipes.value = recipes
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Failed to load recipes"
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

    fun searchRecipes(query: String) {
        currentSearchQuery = query

        if (query.isBlank()) {
            // Show all recipes if no search
            _recipes.value = allRecipes
            _uiState.value = _uiState.value.copy(searchQuery = query)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            try {
                recipeRepository.searchRecipes(query)
                    .onSuccess { searchResults ->
                        _recipes.value = searchResults
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            searchQuery = query
                        )
                    }
                    .onFailure { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            error = exception.message ?: "Search failed"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun clearSearch() {
        currentSearchQuery = ""
        _recipes.value = allRecipes
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            error = null
        )
    }

    fun refreshData() {
        loadInitialData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomepageUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)