package com.testtask.fitnesstest.domain.usecase

import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.model.WorkoutType
import javax.inject.Inject

class FilterWorkoutsByTypeUseCase @Inject constructor() {
    operator fun invoke(workouts: List<Workout>, type: WorkoutType?): List<Workout> {
        return if (type == null) {
            workouts
        } else {
            workouts.filter { it.type == type }
        }
    }
}