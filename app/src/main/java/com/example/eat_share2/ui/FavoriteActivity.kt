package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.eat_share2.R
import com.example.eat_share2.databinding.ActivityFavoriteBinding
import com.example.eat_share2.ui.adapters.RecipeAdapter
import com.example.eat_share2.ui.viewmodels.FavoriteViewModel
import kotlinx.coroutines.launch

class FavoriteActivity : BaseActivity() {

    private lateinit var binding: ActivityFavoriteBinding
    private val viewModel: FavoriteViewModel by viewModels()

    private lateinit var recipeAdapter: RecipeAdapter

    // Activity result launcher to handle returning from ViewRecipeActivity
    private val viewRecipeActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Check if rating was updated
        val data = result.data
        if (data != null && data.hasExtra("rating_updated")) {
            val recipeId = data.getStringExtra("recipe_id")
            val newRating = data.getFloatExtra("new_rating", 0f)
            val newReviewCount = data.getIntExtra("new_review_count", 0)

            if (recipeId != null && newRating > 0) {
                // Update the rating in the ViewModel
                viewModel.updateRecipeRating(recipeId, newRating, newReviewCount)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // Setup recipe adapter with click listener to navigate to ViewRecipeActivity
        recipeAdapter = RecipeAdapter { recipe ->
            val intent = Intent(this, ViewRecipeActivity::class.java)
            intent.putExtra("recipeId", recipe.id)
            intent.putExtra("recipeName", recipe.title)
            intent.putExtra("current_rating", recipe.rating)
            intent.putExtra("current_review_count", recipe.reviewCount)

            // Launch with result to capture rating updates
            viewRecipeActivityLauncher.launch(intent)
        }

        binding.recipesRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = recipeAdapter
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

    private fun setupClickListeners() {
        binding.retryButton.setOnClickListener {
            viewModel.refreshData()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                // Handle loading state
                binding.swipeRefreshLayout.isRefreshing = uiState.isLoading
                binding.loadingProgressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                binding.loadingLayout.visibility = if (uiState.isLoading && recipeAdapter.itemCount == 0) View.VISIBLE else View.GONE

                // Handle errors
                uiState.error?.let { error ->
                    Toast.makeText(this@FavoriteActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.recipes.collect { recipes ->
                recipeAdapter.submitList(recipes)

                // Update section title with count
                binding.sectionTitle.text = "Top rated recipes (${recipes.size})"

                // Show/hide empty state
                val showEmptyState = recipes.isEmpty() &&
                        !viewModel.uiState.value.isLoading

                binding.emptyStateLayout.visibility = if (showEmptyState) View.VISIBLE else View.GONE
                binding.recipesRecyclerView.visibility = if (showEmptyState) View.GONE else View.VISIBLE
                binding.loadingLayout.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity to pick up any rating changes
        viewModel.refreshData()
    }
}