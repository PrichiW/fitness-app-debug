package com.testtask.fitnesstest.presentation.workoutlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testtask.fitnesstest.data.remote.ApiConstants
import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.usecase.GetVideoWorkoutUseCase
import com.testtask.fitnesstest.domain.util.Result
import com.testtask.fitnesstest.presentation.model.VideoPlayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val getVideoWorkoutUseCase: GetVideoWorkoutUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<VideoPlayerUiState>()
    val uiState: LiveData<VideoPlayerUiState> = _uiState

    fun loadVideoWorkout(workout: Workout) {
        viewModelScope.launch {
            _uiState.value = VideoPlayerUiState.Loading

            when (val result = getVideoWorkoutUseCase(workout.id)) {
                is Result.Success -> {
                    val relativeUrl = result.data.videoUrl
                    val fullUrl = ApiConstants.BASE_URL + relativeUrl

                    _uiState.value = VideoPlayerUiState.Success(
                        workout = workout,
                        videoUrl = fullUrl
                    )
                }

                is Result.Error -> {
                    _uiState.value = VideoPlayerUiState.Error(
                        result.message ?: "Ошибка загрузки видео"
                    )
                }

                is Result.Loading -> {

                }
            }
        }
    }

    fun retry(workout: Workout) {
        loadVideoWorkout(workout)
    }
}