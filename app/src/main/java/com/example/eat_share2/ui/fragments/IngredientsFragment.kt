package com.example.eat_share2.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eat_share2.databinding.FragmentIngredientsBinding
import com.example.eat_share2.data.models.AddIngredient

class IngredientsFragment : Fragment() {

    private var _binding: FragmentIngredientsBinding? = null
    private val binding get() = _binding!!

    private val ingredients = mutableListOf<AddIngredient>()
    private lateinit var adapter: IngredientsFragmentAdapter

    var onIngredientsChanged: ((List<AddIngredient>) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIngredientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()

        // Add initial ingredient if list is empty
        if (ingredients.isEmpty()) {
            addIngredient()
        }
    }

    private fun setupRecyclerView() {
        adapter = IngredientsFragmentAdapter(
            ingredients = ingredients,
            onIngredientChanged = { position, ingredient ->
                if (position < ingredients.size) {
                    ingredients[position] = ingredient
                    Log.d("IngredientsFragment", "Ingredient updated at $position: ${ingredient.name}|${ingredient.quantity}")
                    notifyIngredientsChanged()
                }
            },
            onRemoveIngredient = { position ->
                if (position < ingredients.size && ingredients.size > 1) {
                    ingredients.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, ingredients.size)
                    Log.d("IngredientsFragment", "Ingredient removed at $position")
                    notifyIngredientsChanged()
                }
            }
        )

        binding.ingredientsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@IngredientsFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.addIngredientButton.setOnClickListener {
            addIngredient()
        }
    }

    private fun addIngredient() {
        ingredients.add(AddIngredient())
        adapter.notifyItemInserted(ingredients.size - 1)
        Log.d("IngredientsFragment", "Ingredient added, total: ${ingredients.size}")
        notifyIngredientsChanged()
    }

    private fun notifyIngredientsChanged() {
        Log.d("IngredientsFragment", "Notifying ingredients changed: ${ingredients.size} items")
        onIngredientsChanged?.invoke(ingredients.toList())
    }

    fun getIngredients(): List<AddIngredient> = ingredients.toList()

    fun setIngredients(newIngredients: List<AddIngredient>) {
        ingredients.clear()
        ingredients.addAll(newIngredients)
        adapter.notifyDataSetChanged()
        notifyIngredientsChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}