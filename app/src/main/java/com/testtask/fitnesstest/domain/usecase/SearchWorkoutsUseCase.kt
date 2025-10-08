package com.testtask.fitnesstest.domain.usecase

import com.testtask.fitnesstest.domain.model.Workout
import java.util.Locale
import javax.inject.Inject

class SearchWorkoutsUseCase @Inject constructor() {
    operator fun invoke(workouts: List<Workout>, query: String): List<Workout> {
        if (query.isBlank()) return workouts

        val lowerCaseQuery = query.lowercase().trim()
        return workouts.filter {
            it.title.lowercase(Locale.ROOT).contains(lowerCaseQuery)
        }
    }
}