package com.example.eat_share2.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.RecipeStep

class InstructionAdapter : ListAdapter<RecipeStep, InstructionAdapter.InstructionViewHolder>(InstructionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstructionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_instruction, parent, false)
        return InstructionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InstructionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InstructionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stepNumber: TextView = itemView.findViewById(R.id.stepNumber)
        private val instructionText: TextView = itemView.findViewById(R.id.instructionText)

        fun bind(step: RecipeStep) {
            stepNumber.text = step.stepNumber.toString()
            instructionText.text = step.instruction
        }
    }

    private class InstructionDiffCallback : DiffUtil.ItemCallback<RecipeStep>() {
        override fun areItemsTheSame(oldItem: RecipeStep, newItem: RecipeStep): Boolean {
            return oldItem.stepNumber == newItem.stepNumber
        }

        override fun areContentsTheSame(oldItem: RecipeStep, newItem: RecipeStep): Boolean {
            return oldItem == newItem
        }
    }
}