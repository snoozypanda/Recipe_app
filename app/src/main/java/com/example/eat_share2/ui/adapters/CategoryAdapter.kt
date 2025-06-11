package com.example.eat_share2.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.Category
import com.google.android.material.chip.Chip

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_chip, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSelectedCategory(categoryId: String?) {
        val previousSelected = selectedCategoryId
        selectedCategoryId = categoryId
        
        // Notify changes for previous and current selection
        currentList.forEachIndexed { index, category ->
            if (category.id == previousSelected || category.id == categoryId) {
                notifyItemChanged(index)
            }
        }
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryChip: Chip = itemView.findViewById(R.id.categoryChip)

        fun bind(category: Category) {
            categoryChip.text = "${category.icon} ${category.name}"
            categoryChip.isChecked = category.id == selectedCategoryId

            categoryChip.setOnClickListener {
                val newSelectedId = if (selectedCategoryId == category.id) null else category.id
                setSelectedCategory(newSelectedId)
                onCategoryClick(category)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}