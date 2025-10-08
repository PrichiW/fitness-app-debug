package com.testtask.fitnesstest.presentation.videoplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.testtask.fitnesstest.R
import com.testtask.fitnesstest.databinding.ItemWorkoutBinding
import com.testtask.fitnesstest.domain.model.Workout


class WorkoutAdapter(
    private val onWorkoutClick: (Workout) -> Unit
) : ListAdapter<Workout, WorkoutAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WorkoutViewHolder(binding, onWorkoutClick)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WorkoutViewHolder(
        private val binding: ItemWorkoutBinding,
        private val onWorkoutClick: (Workout) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: Workout) {
            with(binding) {
                titleTextView.text = workout.title
                typeTextView.text = workout.type.displayName
                val context = root.context
                val durationInMinutes = workout.durationInMinutes
                durationTextView.text = context.resources.getQuantityString(
                    R.plurals.minutes_plural,
                    durationInMinutes,
                    durationInMinutes
                )

                if (workout.description.isNullOrBlank()) {
                    descriptionTextView.visibility = View.GONE
                } else {
                    descriptionTextView.visibility = View.VISIBLE
                    descriptionTextView.text = workout.description
                }

                root.setOnClickListener {
                    onWorkoutClick(workout)
                }
            }
        }
    }

    class WorkoutDiffCallback : DiffUtil.ItemCallback<Workout>() {
        override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
            return oldItem == newItem
        }
    }
}