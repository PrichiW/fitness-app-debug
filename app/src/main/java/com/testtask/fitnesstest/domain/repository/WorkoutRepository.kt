package com.testtask.fitnesstest.domain.repository

import com.testtask.fitnesstest.domain.model.VideoWorkout
import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.util.Result

interface WorkoutRepository {
    suspend fun getWorkouts(): Result<List<Workout>>

    suspend fun getVideoWorkout(id: Int): Result<VideoWorkout>
}