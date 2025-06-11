package com.example.eat_share2.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.AddStep

class AddStepAdapter(
    private val onStepChanged: (Int, AddStep) -> Unit,
    private val onRemoveStep: (Int) -> Unit
) : ListAdapter<AddStep, AddStepAdapter.StepViewHolder>(StepDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stepNumber: TextView = itemView.findViewById(R.id.stepNumber)
        private val stepEditText: EditText = itemView.findViewById(R.id.stepEditText)
        private val removeButton: ImageView = itemView.findViewById(R.id.removeStepButton)

        private var textWatcher: TextWatcher? = null
        private var isUpdating = false

        fun bind(step: AddStep, position: Int) {
            // Prevent recursive updates
            if (isUpdating) return
            isUpdating = true

            // Set step number
            stepNumber.text = (position + 1).toString()

            // Remove previous text watcher to avoid conflicts
            textWatcher?.let { stepEditText.removeTextChangedListener(it) }

            // Store current cursor position
            val selection = stepEditText.selectionStart

            // Set current value only if it's different
            if (stepEditText.text.toString() != step.instruction) {
                stepEditText.setText(step.instruction)
                // Restore cursor position if text was updated
                if (step.instruction.isNotEmpty() && selection >= 0 && selection <= step.instruction.length) {
                    stepEditText.setSelection(selection)
                }
            }

            // Create new text watcher
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isUpdating) {
                        val newStep = step.copy(instruction = s?.toString() ?: "")
                        onStepChanged(position, newStep)
                    }
                }
            }

            // Add text watcher
            stepEditText.addTextChangedListener(textWatcher)

            // Remove button
            removeButton.setOnClickListener {
                onRemoveStep(position)
            }

            // Show/hide remove button based on list size
            removeButton.visibility = if (currentList.size > 1) View.VISIBLE else View.GONE

            isUpdating = false
        }
    }

    private class StepDiffCallback : DiffUtil.ItemCallback<AddStep>() {
        override fun areItemsTheSame(oldItem: AddStep, newItem: AddStep): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AddStep, newItem: AddStep): Boolean {
            return oldItem == newItem
        }
    }
}