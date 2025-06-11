package com.example.eat_share2.ui.fragments

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eat_share2.R
import com.example.eat_share2.data.models.AddStep

class StepsFragmentAdapter(
    private val steps: MutableList<AddStep>,
    private val onStepChanged: (Int, AddStep) -> Unit,
    private val onRemoveStep: (Int) -> Unit
) : RecyclerView.Adapter<StepsFragmentAdapter.StepViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_step, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        holder.bind(steps[position], position)
    }

    override fun getItemCount(): Int = steps.size

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

            // Remove previous text watcher
            textWatcher?.let { stepEditText.removeTextChangedListener(it) }

            // Set text only if different to avoid cursor reset
            if (stepEditText.text.toString() != step.instruction) {
                stepEditText.setText(step.instruction)
            }

            // Create and add new text watcher
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isUpdating) {
                        val currentPosition = adapterPosition
                        if (currentPosition != RecyclerView.NO_POSITION && currentPosition < steps.size) {
                            val newStep = steps[currentPosition].copy(instruction = s?.toString() ?: "")
                            steps[currentPosition] = newStep
                            onStepChanged(currentPosition, newStep)
                        }
                    }
                }
            }

            stepEditText.addTextChangedListener(textWatcher)

            // Remove button
            removeButton.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onRemoveStep(currentPosition)
                }
            }

            // Show/hide remove button
            removeButton.visibility = if (steps.size > 1) View.VISIBLE else View.GONE

            isUpdating = false
        }
    }
}