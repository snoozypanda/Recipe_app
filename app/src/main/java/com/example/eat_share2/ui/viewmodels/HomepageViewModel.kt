package com.example.eat_share2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eat_share2.data.models.Category
import com.example.eat_share2.data.models.Recipe
import com.example.eat_share2.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomepageViewModel : ViewModel() {
    
    private val recipeRepository = RecipeRepository()
    
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                // Load categories and recipes in parallel
                val categoriesDeferred = recipeRepository.getCategories()
                val recipesDeferred = recipeRepository.getPopularRecipes()
                
                _categories.value = categoriesDeferred
                allRecipes = recipesDeferred
                _recipes.value = allRecipes
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            
            try {
                val searchResults = if (query.isBlank() && selectedCategory == null) {
                    allRecipes
                } else if (selectedCategory != null && query.isBlank()) {
                    recipeRepository.getRecipesByCategory(selectedCategory!!)
                } else {
                    recipeRepository.searchRecipes(query).let { results ->
                        if (selectedCategory != null) {
                            results.filter { it.category.equals(selectedCategory, ignoreCase = true) }
                        } else {
                            results
                        }
                    }
                }
                
                _recipes.value = searchResults
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    searchQuery = query
                )
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val filteredRecipes = if (category == null) {
                    if (currentSearchQuery.isBlank()) {
                        allRecipes
                    } else {
                        recipeRepository.searchRecipes(currentSearchQuery)
                    }
                } else {
                    val categoryRecipes = recipeRepository.getRecipesByCategory(category.name)
                    if (currentSearchQuery.isBlank()) {
                        categoryRecipes
                    } else {
                        categoryRecipes.filter { 
                            it.title.contains(currentSearchQuery, ignoreCase = true) ||
                            it.description.contains(currentSearchQuery, ignoreCase = true) ||
                            it.ingredients.any { ingredient -> 
                                ingredient.contains(currentSearchQuery, ignoreCase = true) 
                            }
                        }
                    }
                }
                
                _recipes.value = filteredRecipes
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedCategory = category?.name
                )
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
            selectedCategory = null
        )
    }
    
    fun refreshData() {
        loadInitialData()
    }
}

data class HomepageUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategory: String? = null
)