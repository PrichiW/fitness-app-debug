package com.testtask.fitnesstest.presentation.model

import com.testtask.fitnesstest.domain.model.Workout

sealed class VideoPlayerUiState {
    data object Loading : VideoPlayerUiState()
    data class Success(
        val workout: Workout,
        val videoUrl: String
    ) : VideoPlayerUiState()

    data class Error(val message: String) : VideoPlayerUiState()
}