package com.testtask.fitnesstest.domain.model

enum class WorkoutType(val id: Int, val displayName: String) {
    TRAINING(1, "Тренировка"),
    LIVE_STREAM(2, "Эфир"),
    COMPLEX(3, "Комплекс");

    companion object {
        fun fromId(id: Int): WorkoutType {
            return entries.find { it.id == id } ?: TRAINING
        }

        fun getAllTypes(): List<WorkoutType> = entries
    }
}