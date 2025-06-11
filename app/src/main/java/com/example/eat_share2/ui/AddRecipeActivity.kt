package com.example.eat_share2.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.eat_share2.databinding.ActivityAddRecipeBinding
import com.example.eat_share2.ui.fragments.IngredientsFragment
import com.example.eat_share2.ui.fragments.StepsFragment
import com.example.eat_share2.ui.viewmodels.AddRecipeViewModel
import com.example.eat_share2.data.models.AddIngredient
import com.example.eat_share2.data.models.AddStep
import kotlinx.coroutines.launch

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRecipeBinding
    private val viewModel: AddRecipeViewModel by viewModels()

    private lateinit var ingredientsFragment: IngredientsFragment
    private lateinit var stepsFragment: StepsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addRecipeActivity) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = systemBars.bottom)
            insets
        }

        setupFragments()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()

        // Initialize button as enabled
        binding.saveRecipeButton.isEnabled = true
        binding.saveRecipeButton.alpha = 1.0f
        binding.saveRecipeButton.text = "Save Recipe"
    }

    private fun setupFragments() {
        // Create fragments
        ingredientsFragment = IngredientsFragment()
        stepsFragment = StepsFragment()

        // Set up fragment callbacks
        ingredientsFragment.onIngredientsChanged = { ingredients ->
            Log.d("AddRecipeActivity", "Ingredients changed: ${ingredients.size}")
            Log.d("AddRecipeActivity", "Ingredients details: ${ingredients.map { "${it.name.trim()}|${it.quantity.trim()}" }}")
            viewModel.updateIngredients(ingredients)
        }

        stepsFragment.onStepsChanged = { steps ->
            Log.d("AddRecipeActivity", "Steps changed: ${steps.size}")
            Log.d("AddRecipeActivity", "Steps details: ${steps.map { it.instruction.trim() }}")
            viewModel.updateSteps(steps)
        }

        // Add fragments to containers
        supportFragmentManager.beginTransaction()
            .replace(binding.ingredientsContainer.id, ingredientsFragment)
            .replace(binding.stepsContainer.id, stepsFragment)
            .commit()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.saveRecipeButton.setOnClickListener {
            saveRecipe()
        }
    }

    private fun setupTextWatchers() {
        // Add text watchers to monitor form changes (for logging purposes only)
        binding.recipeNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                Log.d("AddRecipeActivity", "Recipe name changed: ${s.toString()}")
            }
        })

        binding.recipeDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                Log.d("AddRecipeActivity", "Recipe description changed: ${s.toString()}")
            }
        })
    }

    private fun saveRecipe() {
        val name = binding.recipeNameEditText.text.toString().trim()
        val description = binding.recipeDescriptionEditText.text.toString().trim()

        if (name.isEmpty()) {
            binding.recipeNameEditText.error = "Recipe name is required"
            Toast.makeText(this, "Please enter a recipe name", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            binding.recipeDescriptionEditText.error = "Recipe description is required"
            Toast.makeText(this, "Please enter a recipe description", Toast.LENGTH_SHORT).show()
            return
        }

        // Get data from fragments - ensure fragments are initialized
        val ingredients = if (::ingredientsFragment.isInitialized) {
            ingredientsFragment.getIngredients()
        } else {
            emptyList()
        }

        val steps = if (::stepsFragment.isInitialized) {
            stepsFragment.getSteps()
        } else {
            emptyList()
        }

        Log.d("AddRecipeActivity", "Save Recipe - Raw Ingredients: ${ingredients.size}")
        ingredients.forEachIndexed { index, ingredient ->
            Log.d("AddRecipeActivity", "Ingredient $index: name='${ingredient.name}' quantity='${ingredient.quantity}'")
        }

        Log.d("AddRecipeActivity", "Save Recipe - Raw Steps: ${steps.size}")
        steps.forEachIndexed { index, step ->
            Log.d("AddRecipeActivity", "Step $index: instruction='${step.instruction}'")
        }

        // Validate ingredients - check for non-empty trimmed values
        val validIngredients = ingredients.filter { ingredient ->
            val hasName = ingredient.name.trim().isNotBlank()
            val hasQuantity = ingredient.quantity.trim().isNotBlank()
            Log.d("AddRecipeActivity", "Validating ingredient: name='${ingredient.name.trim()}' (${hasName}) quantity='${ingredient.quantity.trim()}' (${hasQuantity})")
            hasName && hasQuantity
        }

        if (validIngredients.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient with both name and quantity", Toast.LENGTH_LONG).show()
            Log.d("AddRecipeActivity", "No valid ingredients found. Total ingredients: ${ingredients.size}")
            return
        }

        // Validate steps - check for non-empty trimmed values
        val validSteps = steps.filter { step ->
            val hasInstruction = step.instruction.trim().isNotBlank()
            Log.d("AddRecipeActivity", "Validating step: instruction='${step.instruction.trim()}' (${hasInstruction})")
            hasInstruction
        }

        if (validSteps.isEmpty()) {
            Toast.makeText(this, "Please add at least one instruction step", Toast.LENGTH_LONG).show()
            Log.d("AddRecipeActivity", "No valid steps found. Total steps: ${steps.size}")
            return
        }

        Log.d("AddRecipeActivity", "Valid ingredients: ${validIngredients.size}")
        Log.d("AddRecipeActivity", "Valid steps: ${validSteps.size}")

        viewModel.saveRecipe(name, description, validIngredients, validSteps)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                // Handle loading state - only disable during actual saving
                val isLoading = uiState.isLoading
                binding.saveRecipeButton.isEnabled = !isLoading
                binding.saveRecipeButton.text = if (isLoading) "Saving..." else "Save Recipe"
                binding.saveRecipeButton.alpha = if (isLoading) 0.7f else 1.0f

                // Handle success
                if (uiState.isSuccess) {
                    Toast.makeText(this@AddRecipeActivity, "Recipe saved successfully!", Toast.LENGTH_SHORT).show()

                    // Navigate back to homepage and refresh
                    val intent = Intent(this@AddRecipeActivity, HomepageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("refresh", true)
                    startActivity(intent)
                    finish()
                }

                // Handle error
                uiState.error?.let { error ->
                    Toast.makeText(this@AddRecipeActivity, error, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }
}