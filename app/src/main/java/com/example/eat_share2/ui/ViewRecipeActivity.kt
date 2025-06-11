package com.example.eat_share2.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eat_share2.databinding.ActivityViewRecipieBinding
import com.example.eat_share2.ui.adapters.IngredientAdapter
import com.example.eat_share2.ui.adapters.InstructionAdapter
import com.example.eat_share2.ui.viewmodels.ViewRecipeViewModel
import kotlinx.coroutines.launch

class ViewRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewRecipieBinding
    private val viewModel: ViewRecipeViewModel by viewModels()

    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var instructionAdapter: InstructionAdapter

    private var recipeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewRecipieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get recipe ID from intent
        recipeId = intent.getStringExtra("recipeId")

        if (recipeId == null) {
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
            finish()
        }

        binding.favoriteButton.setOnClickListener {
            // TODO: Implement favorite functionality
            Toast.makeText(this, "Favorite feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.retryButton.setOnClickListener {
            recipeId?.let { id ->
                viewModel.loadRecipeDetails(id)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                // Handle loading state
                binding.loadingLayout.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                binding.contentScrollView.visibility = if (uiState.isLoading) View.GONE else View.VISIBLE
                binding.errorLayout.visibility = View.GONE

                // Handle error state
                uiState.error?.let { error ->
                    binding.loadingLayout.visibility = View.GONE
                    binding.contentScrollView.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.errorMessage.text = error

                    Toast.makeText(this@ViewRecipeActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }

                // Handle recipe data
                uiState.recipeDetail?.let { recipe ->
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
                    binding.chefName.text = user.name
                }
            }
        }
    }
}