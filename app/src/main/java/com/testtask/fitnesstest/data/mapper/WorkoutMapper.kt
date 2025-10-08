package com.testtask.fitnesstest.data.mapper

import com.testtask.fitnesstest.data.remote.dto.VideoWorkoutDto
import com.testtask.fitnesstest.data.remote.dto.WorkoutDto
import com.testtask.fitnesstest.domain.model.VideoWorkout
import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.model.WorkoutType

fun WorkoutDto.toDomain(): Workout {
    return Workout(
        id = id,
        title = title,
        description = description,
        type = WorkoutType.fromId(type),
        durationInMinutes = duration.toMinutes()
    )
}

fun List<WorkoutDto>.toDomain(): List<Workout> {
    return map { it.toDomain() }
}

fun VideoWorkoutDto.toDomain(): VideoWorkout {
    return VideoWorkout(
        id = id,
        durationInMinutes = duration.toMinutes(),
        videoUrl = link
    )
}

private fun String.toMinutes(): Int {
    val parts = this.trim().split(" ")
    return parts.firstOrNull()?.toIntOrNull() ?: 0
}