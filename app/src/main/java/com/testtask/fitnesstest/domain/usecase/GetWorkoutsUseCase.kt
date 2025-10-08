package com.testtask.fitnesstest.domain.usecase

import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.repository.WorkoutRepository
import com.testtask.fitnesstest.domain.util.Result
import javax.inject.Inject

class GetWorkoutsUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): Result<List<Workout>> {
        return repository.getWorkouts()
    }
}