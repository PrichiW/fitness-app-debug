package com.testtask.fitnesstest.presentation.videoplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.model.WorkoutType
import com.testtask.fitnesstest.domain.usecase.FilterWorkoutsByTypeUseCase
import com.testtask.fitnesstest.domain.usecase.GetWorkoutsUseCase
import com.testtask.fitnesstest.domain.usecase.SearchWorkoutsUseCase
import com.testtask.fitnesstest.domain.util.Result
import com.testtask.fitnesstest.presentation.model.WorkoutListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WorkoutListViewModel @Inject constructor(
    private val getWorkoutsUseCase: GetWorkoutsUseCase,
    private val filterWorkoutsByTypeUseCase: FilterWorkoutsByTypeUseCase,
    private val searchWorkoutsUseCase: SearchWorkoutsUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<WorkoutListUiState>()
    val uiState: LiveData<WorkoutListUiState> = _uiState

    private var allWorkouts: List<Workout> = emptyList()
    private var currentType: WorkoutType? = null
    private var currentSearchQuery: String = ""

    init {
        loadWorkouts()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            _uiState.value = WorkoutListUiState.Loading

            when (val result = getWorkoutsUseCase()) {
                is Result.Success -> {
                    allWorkouts = result.data
                    if (allWorkouts.isEmpty()) {
                        _uiState.value = WorkoutListUiState.Empty
                    } else {
                        applyFiltersAndSearch()
                    }
                }

                is Result.Error -> {
                    _uiState.value = WorkoutListUiState.Error(
                        result.message ?: "Ошибка загрузки тренировок"
                    )
                }

                is Result.Loading -> {

                }
            }
        }
    }

    fun filterByType(type: WorkoutType?) {
        currentType = type
        applyFiltersAndSearch()
    }

    fun searchWorkouts(query: String) {
        currentSearchQuery = query
        applyFiltersAndSearch()
    }

    fun clearFilters() {
        currentType = null
        currentSearchQuery = ""
        applyFiltersAndSearch()
    }

    fun refreshWorkouts() {
        loadWorkouts()
    }

    private fun applyFiltersAndSearch() {
        var filtered = allWorkouts

        filtered = filterWorkoutsByTypeUseCase(filtered, currentType)

        filtered = searchWorkoutsUseCase(filtered, currentSearchQuery)

        _uiState.value = if (filtered.isEmpty()) {
            WorkoutListUiState.Empty
        } else {
            WorkoutListUiState.Success(
                workouts = allWorkouts,
                filteredWorkouts = filtered,
                selectedType = currentType,
                searchQuery = currentSearchQuery
            )
        }
    }
}