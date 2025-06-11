package com.example.eat_share2.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eat_share2.databinding.FragmentStepsBinding
import com.example.eat_share2.data.models.AddStep

class StepsFragment : Fragment() {

    private var _binding: FragmentStepsBinding? = null
    private val binding get() = _binding!!

    private val steps = mutableListOf<AddStep>()
    private lateinit var adapter: StepsFragmentAdapter

    var onStepsChanged: ((List<AddStep>) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()

        // Add initial step if list is empty
        if (steps.isEmpty()) {
            addStep()
        }
    }

    private fun setupRecyclerView() {
        adapter = StepsFragmentAdapter(
            steps = steps,
            onStepChanged = { position, step ->
                if (position < steps.size) {
                    steps[position] = step
                    Log.d("StepsFragment", "Step updated at $position: ${step.instruction}")
                    notifyStepsChanged()
                }
            },
            onRemoveStep = { position ->
                if (position < steps.size && steps.size > 1) {
                    steps.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, steps.size)
                    Log.d("StepsFragment", "Step removed at $position")
                    notifyStepsChanged()
                }
            }
        )

        binding.stepsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@StepsFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.addStepButton.setOnClickListener {
            addStep()
        }
    }

    private fun addStep() {
        steps.add(AddStep())
        adapter.notifyItemInserted(steps.size - 1)
        Log.d("StepsFragment", "Step added, total: ${steps.size}")
        notifyStepsChanged()
    }

    private fun notifyStepsChanged() {
        Log.d("StepsFragment", "Notifying steps changed: ${steps.size} items")
        onStepsChanged?.invoke(steps.toList())
    }

    fun getSteps(): List<AddStep> = steps.toList()

    fun setSteps(newSteps: List<AddStep>) {
        steps.clear()
        steps.addAll(newSteps)
        adapter.notifyDataSetChanged()
        notifyStepsChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}