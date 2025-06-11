package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.eat_share2.R
import com.example.eat_share2.databinding.ActivityHomepageBinding
import com.example.eat_share2.ui.adapters.CategoryAdapter
import com.example.eat_share2.ui.adapters.RecipeAdapter
import com.example.eat_share2.ui.viewmodels.HomepageUiState
import com.example.eat_share2.ui.viewmodels.HomepageViewModel
import kotlinx.coroutines.launch

class HomepageActivity : BaseActivity() {

    private lateinit var binding: ActivityHomepageBinding
    private val viewModel: HomepageViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        setupRecyclerViews()
        setupSearchView()
        setupSwipeRefresh()
        setupEmptyState()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Setup category adapter
        categoryAdapter = CategoryAdapter { category ->
            if (viewModel.uiState.value.selectedCategory == category.name) {
                viewModel.selectCategory(null) // Deselect if already selected
            } else {
                viewModel.selectCategory(category)
            }
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomepageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        // Setup recipe adapter
        recipeAdapter = RecipeAdapter { recipe ->
            val intent = Intent(this, ViewRecipeActivity::class.java)
            intent.putExtra("recipeId", recipe.id)
            intent.putExtra("recipeName", recipe.title)
            startActivity(intent)
        }

        binding.recipesRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = recipeAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchEditText.addTextChangedListener { editable ->
            val query = editable?.toString() ?: ""
            viewModel.searchRecipes(query)
        }

        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            viewModel.clearSearch()
            categoryAdapter.setSelectedCategory(null)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primaryColor,
            R.color.accentColor
        )
    }

    private fun setupEmptyState() {
        binding.clearFiltersButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            viewModel.clearSearch()
            categoryAdapter.setSelectedCategory(null)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.swipeRefreshLayout.isRefreshing = uiState.isLoading

                // Show/hide loading indicator
                binding.loadingProgressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE

                // Show/hide search loading
                binding.searchProgressBar.visibility = if (uiState.isSearching) View.VISIBLE else View.GONE

                // Handle errors
                uiState.error?.let { error ->
                    Toast.makeText(this@HomepageActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                // Update search query
                if (uiState.searchQuery != binding.searchEditText.text.toString()) {
                    binding.searchEditText.setText(uiState.searchQuery)
                    binding.searchEditText.setSelection(uiState.searchQuery.length)
                }

                // Show/hide clear button
                binding.clearSearchButton.visibility =
                    if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != null)
                        View.VISIBLE else View.GONE

                // Update results text
                updateResultsText(uiState)
            }
        }

        lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                categoryAdapter.submitList(categories)
            }
        }

        lifecycleScope.launch {
            viewModel.recipes.collect { recipes ->
                recipeAdapter.submitList(recipes)

                // Show/hide empty state
                val showEmptyState = recipes.isEmpty() &&
                        !viewModel.uiState.value.isLoading &&
                        !viewModel.uiState.value.isSearching

                binding.emptyStateLayout.visibility = if (showEmptyState) View.VISIBLE else View.GONE
                binding.recipesRecyclerView.visibility = if (showEmptyState) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateResultsText(uiState: HomepageUiState) {
        val recipesCount = recipeAdapter.itemCount
        val resultsText = when {
            uiState.searchQuery.isNotEmpty() && uiState.selectedCategory != null ->
                "Found $recipesCount recipes for \"${uiState.searchQuery}\" in ${uiState.selectedCategory}"
            uiState.searchQuery.isNotEmpty() ->
                "Found $recipesCount recipes for \"${uiState.searchQuery}\""
            uiState.selectedCategory != null ->
                "$recipesCount ${uiState.selectedCategory} recipes"
            else -> "Popular Recipes ($recipesCount)"
        }

        binding.sectionTitle.text = resultsText
    }
}