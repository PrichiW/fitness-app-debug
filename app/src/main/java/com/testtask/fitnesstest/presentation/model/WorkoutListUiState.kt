package com.testtask.fitnesstest.presentation.model

import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.model.WorkoutType

sealed class WorkoutListUiState {
    data object Loading : WorkoutListUiState()
    data class Success(
        val workouts: List<Workout>,
        val filteredWorkouts: List<Workout>,
        val selectedType: WorkoutType? = null,
        val searchQuery: String = ""
    ) : WorkoutListUiState()

    data class Error(val message: String) : WorkoutListUiState()
    data object Empty : WorkoutListUiState()
}