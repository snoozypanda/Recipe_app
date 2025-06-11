package com.example.eat_share2.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.eat_share2.data.models.Category
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
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    
    private var allRecipes: List<Recipe> = emptyList()
    private var currentSearchQuery: String = ""
    private var selectedCategory: String? = null
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load categories (local data)
                _categories.value = recipeRepository.getCategories()
                
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
        
        if (query.isBlank() && selectedCategory == null) {
            // Show all recipes if no search and no category
            _recipes.value = allRecipes
            _uiState.value = _uiState.value.copy(searchQuery = query)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            
            try {
                when {
                    // Search with category filter
                    selectedCategory != null && query.isNotBlank() -> {
                        recipeRepository.searchRecipes(query)
                            .onSuccess { searchResults ->
                                val filteredResults = searchResults.filter { 
                                    it.category.equals(selectedCategory, ignoreCase = true) 
                                }
                                _recipes.value = filteredResults
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
                    }
                    
                    // Search only
                    query.isNotBlank() -> {
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
                    }
                    
                    // Category only
                    selectedCategory != null -> {
                        recipeRepository.getRecipesByCategory(selectedCategory!!)
                            .onSuccess { categoryResults ->
                                _recipes.value = categoryResults
                                _uiState.value = _uiState.value.copy(
                                    isSearching = false,
                                    searchQuery = query
                                )
                            }
                            .onFailure { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isSearching = false,
                                    error = exception.message ?: "Failed to filter by category"
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }
    
    fun selectCategory(category: Category?) {
        selectedCategory = category?.name
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                when {
                    // No category selected
                    category == null -> {
                        if (currentSearchQuery.isBlank()) {
                            _recipes.value = allRecipes
                        } else {
                            recipeRepository.searchRecipes(currentSearchQuery)
                                .onSuccess { searchResults ->
                                    _recipes.value = searchResults
                                }
                                .onFailure { exception ->
                                    _uiState.value = _uiState.value.copy(
                                        error = exception.message ?: "Search failed"
                                    )
                                }
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            selectedCategory = null
                        )
                    }
                    
                    // Category selected
                    else -> {
                        recipeRepository.getRecipesByCategory(category.name)
                            .onSuccess { categoryResults ->
                                val filteredResults = if (currentSearchQuery.isBlank()) {
                                    categoryResults
                                } else {
                                    categoryResults.filter { recipe ->
                                        recipe.title.contains(currentSearchQuery, ignoreCase = true) ||
                                        recipe.description.contains(currentSearchQuery, ignoreCase = true) ||
                                        recipe.ingredients.any { ingredient -> 
                                            ingredient.contains(currentSearchQuery, ignoreCase = true) 
                                        }
                                    }
                                }
                                
                                _recipes.value = filteredResults
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    selectedCategory = category.name
                                )
                            }
                            .onFailure { exception ->
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = exception.message ?: "Failed to filter by category"
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to filter by category"
                )
            }
        }
    }
    
    fun clearSearch() {
        currentSearchQuery = ""
        selectedCategory = null
        _recipes.value = allRecipes
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCategory = null,
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
    val searchQuery: String = "",
    val selectedCategory: String? = null
)