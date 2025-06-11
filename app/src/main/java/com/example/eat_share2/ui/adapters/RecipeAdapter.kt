package com.example.eat_share2.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.Recipe

class RecipeAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.recipeCard)
        private val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
        private val recipeTitle: TextView = itemView.findViewById(R.id.recipeTitle)
        private val recipeTime: TextView = itemView.findViewById(R.id.recipeTime)
        private val recipeRating: TextView = itemView.findViewById(R.id.recipeRating)
        private val recipeCategory: TextView = itemView.findViewById(R.id.recipeCategory)

        fun bind(recipe: Recipe) {
            recipeTitle.text = recipe.title
            recipeTime.text = recipe.prepTime
            recipeRating.text = "${recipe.rating} ★ (${recipe.reviewCount})"
            recipeCategory.text = recipe.category

            // For now, we'll use a placeholder background
            // In a real app, you'd use an image loading library like Glide or Picasso
            recipeImage.setBackgroundResource(R.drawable.background)

            cardView.setOnClickListener {
                onRecipeClick(recipe)
            }
        }
    }

    private class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}