package com.testtask.fitnesstest.data.repository

import com.testtask.fitnesstest.data.mapper.toDomain
import com.testtask.fitnesstest.data.remote.FitnessApiService
import com.testtask.fitnesstest.domain.model.VideoWorkout
import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.repository.WorkoutRepository
import com.testtask.fitnesstest.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class WorkoutRepositoryImpl @Inject constructor(
    private val apiService: FitnessApiService
) : WorkoutRepository {
    override suspend fun getWorkouts(): Result<List<Workout>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getWorkouts()
                Result.Success(response.toDomain())
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.io.IOException -> "Проблемы с интернет-соединением"
                    is retrofit2.HttpException -> "Ошибка сервера"
                    else -> e.localizedMessage ?: "Произошла неизвестная ошибка"
                }
                Result.Error(exception = e, message = errorMessage)
            }
        }
    }

    override suspend fun getVideoWorkout(id: Int): Result<VideoWorkout> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVideo(id)
                Result.Success(response.toDomain())
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.io.IOException -> "Проблемы с интернет-соединением"
                    is retrofit2.HttpException -> "Ошибка сервера"
                    else -> e.localizedMessage ?: "Произошла неизвестная ошибка"
                }
                Result.Error(exception = e, message = errorMessage)
            }
        }
    }
}