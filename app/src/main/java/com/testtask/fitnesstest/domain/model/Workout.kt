package com.testtask.fitnesstest.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Workout(
    val id: Int,
    val title: String,
    val description: String?,
    val type: WorkoutType,
    val durationInMinutes: Int
) : Parcelable