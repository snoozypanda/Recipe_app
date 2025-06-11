package com.example.eat_share2.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.AddIngredient

class AddIngredientAdapter(
    private val onIngredientChanged: (Int, AddIngredient) -> Unit,
    private val onRemoveIngredient: (Int) -> Unit
) : ListAdapter<AddIngredient, AddIngredientAdapter.IngredientViewHolder>(IngredientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

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

            // Remove previous text watchers to avoid conflicts
            nameTextWatcher?.let { ingredientNameEditText.removeTextChangedListener(it) }
            quantityTextWatcher?.let { ingredientQuantityEditText.removeTextChangedListener(it) }

            // Store current cursor positions
            val nameSelection = ingredientNameEditText.selectionStart
            val quantitySelection = ingredientQuantityEditText.selectionStart

            // Set current values only if they're different
            if (ingredientNameEditText.text.toString() != ingredient.name) {
                ingredientNameEditText.setText(ingredient.name)
                // Restore cursor position if text was updated
                if (ingredient.name.isNotEmpty() && nameSelection >= 0 && nameSelection <= ingredient.name.length) {
                    ingredientNameEditText.setSelection(nameSelection)
                }
            }

            if (ingredientQuantityEditText.text.toString() != ingredient.quantity) {
                ingredientQuantityEditText.setText(ingredient.quantity)
                // Restore cursor position if text was updated
                if (ingredient.quantity.isNotEmpty() && quantitySelection >= 0 && quantitySelection <= ingredient.quantity.length) {
                    ingredientQuantityEditText.setSelection(quantitySelection)
                }
            }

            // Create new text watchers
            nameTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isUpdating) {
                        val newIngredient = ingredient.copy(name = s?.toString() ?: "")
                        onIngredientChanged(position, newIngredient)
                    }
                }
            }

            quantityTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isUpdating) {
                        val newIngredient = ingredient.copy(quantity = s?.toString() ?: "")
                        onIngredientChanged(position, newIngredient)
                    }
                }
            }

            // Add text watchers
            ingredientNameEditText.addTextChangedListener(nameTextWatcher)
            ingredientQuantityEditText.addTextChangedListener(quantityTextWatcher)

            // Remove button
            removeButton.setOnClickListener {
                onRemoveIngredient(position)
            }

            // Show/hide remove button based on list size
            removeButton.visibility = if (currentList.size > 1) View.VISIBLE else View.GONE

            isUpdating = false
        }
    }

    private class IngredientDiffCallback : DiffUtil.ItemCallback<AddIngredient>() {
        override fun areItemsTheSame(oldItem: AddIngredient, newItem: AddIngredient): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AddIngredient, newItem: AddIngredient): Boolean {
            return oldItem == newItem
        }
    }
}