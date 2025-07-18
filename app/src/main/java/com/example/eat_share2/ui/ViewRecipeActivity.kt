package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eat_share2.databinding.ActivityViewRecipieBinding
import com.example.eat_share2.ui.adapters.IngredientAdapter
import com.example.eat_share2.ui.adapters.InstructionAdapter
import com.example.eat_share2.ui.viewmodels.ViewRecipeViewModel
import com.example.eat_share2.utils.RatingManager
import kotlinx.coroutines.launch

class ViewRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewRecipieBinding
    private val viewModel: ViewRecipeViewModel by viewModels()

    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var instructionAdapter: InstructionAdapter
    private lateinit var ratingManager: RatingManager

    private var recipeId: String? = null
    private var currentRating: Float = 0f
    private var currentReviewCount: Int = 0
    private var hasRatingChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewRecipieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom, top = systemBars.top)
            insets
        }

        // Initialize RatingManager
        ratingManager = RatingManager(this)

        // Get recipe ID from intent
        recipeId = intent.getStringExtra("recipeId")
        val recipeName = intent.getStringExtra("recipeName")
        currentRating = intent.getFloatExtra("current_rating", 0f)
        currentReviewCount = intent.getIntExtra("current_review_count", 0)

        Log.d("ViewRecipeActivity", "Received recipeId: $recipeId")
        Log.d("ViewRecipeActivity", "Received recipeName: $recipeName")
        Log.d("ViewRecipeActivity", "Current rating: $currentRating, reviews: $currentReviewCount")

        if (recipeId == null) {
            Log.e("ViewRecipeActivity", "Recipe ID is null")
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()

        // Load recipe details
        viewModel.loadRecipeDetails(recipeId!!)
    }

    private fun setupRecyclerViews() {
        // Setup ingredients adapter
        ingredientAdapter = IngredientAdapter()
        binding.ingredientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewRecipeActivity)
            adapter = ingredientAdapter
        }

        // Setup instructions adapter
        instructionAdapter = InstructionAdapter()
        binding.instructionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ViewRecipeActivity)
            adapter = instructionAdapter
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            Log.d("ViewRecipeActivity", "Back button clicked")
            finishWithResult()
        }

        binding.favoriteButton.setOnClickListener {
            // Show rating dialog
            showRatingDialog()
        }

        binding.retryButton.setOnClickListener {
            recipeId?.let { id ->
                Log.d("ViewRecipeActivity", "Retry button clicked, reloading recipe: $id")
                viewModel.loadRecipeDetails(id)
            }
        }
    }

    private fun showRatingDialog() {
        val ratingOptions = arrayOf("1 ⭐", "2 ⭐⭐", "3 ⭐⭐⭐", "4 ⭐⭐⭐⭐", "5 ⭐⭐⭐⭐⭐")

        AlertDialog.Builder(this)
            .setTitle("Rate this Recipe")
            .setItems(ratingOptions) { _, which ->
                val newRating = (which + 1).toFloat()
                updateRating(newRating)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateRating(newRating: Float) {
        recipeId?.let { id ->
            // Simulate review count increase
            val newReviewCount = currentReviewCount + 1

            // Calculate new average rating (simplified)
            val totalRating = (currentRating * currentReviewCount) + newRating
            val averageRating = totalRating / newReviewCount

            // Update current values
            currentRating = averageRating
            currentReviewCount = newReviewCount
            hasRatingChanged = true

            // Save the rating update
            ratingManager.saveRatingUpdate(id, averageRating, newReviewCount)

            Toast.makeText(this, "Thank you for rating! New rating: ${String.format("%.1f", averageRating)} ⭐", Toast.LENGTH_SHORT).show()

            Log.d("ViewRecipeActivity", "Rating updated: $averageRating, reviews: $newReviewCount")
        }
    }

    private fun finishWithResult() {
        if (hasRatingChanged && recipeId != null) {
            val resultIntent = Intent().apply {
                putExtra("rating_updated", true)
                putExtra("recipe_id", recipeId)
                putExtra("new_rating", currentRating)
                putExtra("new_review_count", currentReviewCount)
            }
            setResult(RESULT_OK, resultIntent)
        }
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishWithResult()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                Log.d("ViewRecipeActivity", "UI State updated - Loading: ${uiState.isLoading}, Error: ${uiState.error}, Recipe: ${uiState.recipeDetail?.name}")

                // Handle loading state
                binding.loadingLayout.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                binding.contentScrollView.visibility = if (uiState.isLoading) View.GONE else View.VISIBLE
                binding.errorLayout.visibility = View.GONE

                // Handle error state
                uiState.error?.let { error ->
                    Log.e("ViewRecipeActivity", "Error state: $error")
                    binding.loadingLayout.visibility = View.GONE
                    binding.contentScrollView.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.errorMessage.text = error

                    Toast.makeText(this@ViewRecipeActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                // Handle recipe data
                uiState.recipeDetail?.let { recipe ->
                    Log.d("ViewRecipeActivity", "Displaying recipe: ${recipe.name}")
                    Log.d("ViewRecipeActivity", "Ingredients count: ${recipe.ingredients.size}")
                    Log.d("ViewRecipeActivity", "Steps count: ${recipe.steps.size}")

                    binding.loadingLayout.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    binding.contentScrollView.visibility = View.VISIBLE

                    // Update UI with recipe data
                    binding.recipeTitle.text = recipe.name
                    binding.recipeDescription.text = recipe.description

                    // Update ingredients
                    ingredientAdapter.submitList(recipe.ingredients)

                    // Update instructions (sort by step number)
                    val sortedSteps = recipe.steps.sortedBy { it.stepNumber }
                    instructionAdapter.submitList(sortedSteps)
                }

                // Handle user data
                uiState.userDetail?.let { user ->
                    Log.d("ViewRecipeActivity", "Displaying chef: ${user.name}")
                    binding.chefName.text = user.name
                }
            }
        }
    }
}