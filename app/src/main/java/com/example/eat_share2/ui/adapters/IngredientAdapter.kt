package com.example.eat_share2.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.RecipeIngredient

class IngredientAdapter : ListAdapter<RecipeIngredient, IngredientAdapter.IngredientViewHolder>(IngredientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        private val ingredientQuantity: TextView = itemView.findViewById(R.id.ingredientQuantity)

        fun bind(ingredient: RecipeIngredient) {
            ingredientName.text = ingredient.name
            ingredientQuantity.text = ingredient.quantity
        }
    }

    private class IngredientDiffCallback : DiffUtil.ItemCallback<RecipeIngredient>() {
        override fun areItemsTheSame(oldItem: RecipeIngredient, newItem: RecipeIngredient): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: RecipeIngredient, newItem: RecipeIngredient): Boolean {
            return oldItem == newItem
        }
    }
}