package com.example.eat_share2.ui.fragments

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.AddIngredient

class IngredientsFragmentAdapter(
    private val ingredients: MutableList<AddIngredient>,
    private val onIngredientChanged: (Int, AddIngredient) -> Unit,
    private val onRemoveIngredient: (Int) -> Unit
) : RecyclerView.Adapter<IngredientsFragmentAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position], position)
    }

    override fun getItemCount(): Int = ingredients.size

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientNameEditText: EditText = itemView.findViewById(R.id.ingredientNameEditText)
        private val ingredientQuantityEditText: EditText = itemView.findViewById(R.id.ingredientQuantityEditText)
        private val removeButton: ImageView = itemView.findViewById(R.id.removeIngredientButton)

        private var nameTextWatcher: TextWatcher? = null
        private var quantityTextWatcher: TextWatcher? = null
        private var isUpdating = false

        fun bind(ingredient: AddIngredient, position: Int) {
            // Prevent recursive updates
            if (isUpdating) return
            isUpdating = true

            // Remove previous text watchers
            nameTextWatcher?.let { ingredientNameEditText.removeTextChangedListener(it) }
            quantityTextWatcher?.let { ingredientQuantityEditText.removeTextChangedListener(it) }

            // Set text only if different to avoid cursor reset
            if (ingredientNameEditText.text.toString() != ingredient.name) {
                ingredientNameEditText.setText(ingredient.name)
            }
            if (ingredientQuantityEditText.text.toString() != ingredient.quantity) {
                ingredientQuantityEditText.setText(ingredient.quantity)
            }

            // Create and add new text watchers
            nameTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isUpdating) {
                        val currentPosition = adapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < ingredients.size) {
                            val newIngredient = ingredients[currentPosition].copy(name = s?.toString() ?: "")
                            ingredients[currentPosition] = newIngredient
                            onIngredientChanged(currentPosition, newIngredient)
                        }
                    }
                }
            }

            quantityTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isUpdating) {
                        val currentPosition = adapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < ingredients.size) {
                            val newIngredient = ingredients[currentPosition].copy(quantity = s?.toString() ?: "")
                            ingredients[currentPosition] = newIngredient
                            onIngredientChanged(currentPosition, newIngredient)
                        }
                    }
                }
            }

            ingredientNameEditText.addTextChangedListener(nameTextWatcher)
            ingredientQuantityEditText.addTextChangedListener(quantityTextWatcher)

            // Remove button
            removeButton.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onRemoveIngredient(currentPosition)
                }
            }

            // Show/hide remove button
            removeButton.visibility = if (ingredients.size > 1) View.VISIBLE else View.GONE

            isUpdating = false
        }
    }
}