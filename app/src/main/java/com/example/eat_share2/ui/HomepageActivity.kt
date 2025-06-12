package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.eat_share2.R
import com.example.eat_share2.databinding.ActivityHomepageBinding
import com.example.eat_share2.ui.adapters.RecipeAdapter
import com.example.eat_share2.ui.viewmodels.HomepageUiState
import com.example.eat_share2.ui.viewmodels.HomepageViewModel
import kotlinx.coroutines.launch

class HomepageActivity : BaseActivity() {

    private lateinit var binding: ActivityHomepageBinding
    private val viewModel: HomepageViewModel by viewModels()

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupBottomNavigation()
        setupRecyclerViews()
        setupSearchView()
        setupSwipeRefresh()
        setupEmptyState()
        setupFab()
        observeViewModel()

        // Check if we need to refresh (coming back from add recipe)
        if (intent.getBooleanExtra("refresh", false)) {
            viewModel.refreshData()
        }
    }

    private fun setupRecyclerViews() {
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
        // Search button - only search when clicked
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchRecipes(query)
            } else {
                viewModel.clearSearch()
            }
        }

        // Clear search button
        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            viewModel.clearSearch()
        }

        // Handle enter key in search field
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchRecipes(query)
                } else {
                    viewModel.clearSearch()
                }
                true
            } else {
                false
            }
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
        }
    }

    private fun setupFab() {
        binding.addRecipeFab.setOnClickListener {
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
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

                // Show/hide clear button based on search text
                val hasSearchText = binding.searchEditText.text.toString().isNotEmpty()
                binding.clearSearchButton.visibility = if (hasSearchText) View.VISIBLE else View.GONE

                // Update results text
                updateResultsText(uiState)
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
            uiState.searchQuery.isNotEmpty() ->
                "Found $recipesCount recipes for \"${uiState.searchQuery}\""
            else -> "Popular Recipes ($recipesCount)"
        }

        binding.sectionTitle.text = resultsText
    }
}