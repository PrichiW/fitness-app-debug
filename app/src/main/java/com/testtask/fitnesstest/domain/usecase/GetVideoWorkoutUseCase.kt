package com.testtask.fitnesstest.domain.usecase

import com.testtask.fitnesstest.domain.model.VideoWorkout
import com.testtask.fitnesstest.domain.repository.WorkoutRepository
import com.testtask.fitnesstest.domain.util.Result
import javax.inject.Inject

class GetVideoWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workoutId: Int): Result<VideoWorkout> {
        return repository.getVideoWorkout(workoutId)
    }
}