package com.testtask.fitnesstest.data.remote

import com.testtask.fitnesstest.data.remote.dto.VideoWorkoutDto
import com.testtask.fitnesstest.data.remote.dto.WorkoutDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FitnessApiService {
    @GET("get_workouts")
    suspend fun getWorkouts(): List<WorkoutDto>

    @GET("get_video")
    suspend fun getVideo(
        @Query("id") id: Int
    ): VideoWorkoutDto
}