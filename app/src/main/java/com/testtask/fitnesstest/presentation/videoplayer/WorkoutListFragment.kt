package com.testtask.fitnesstest.presentation.videoplayer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.testtask.fitnesstest.R
import com.testtask.fitnesstest.databinding.FragmentWorkoutListBinding
import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.model.WorkoutType
import com.testtask.fitnesstest.presentation.model.WorkoutListUiState
import com.testtask.fitnesstest.presentation.videoplayer.adapter.WorkoutAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutListFragment : Fragment() {

    private val chipTypeMap = mapOf(
        R.id.chipTraining to WorkoutType.TRAINING,
        R.id.chipLiveStream to WorkoutType.LIVE_STREAM,
        R.id.chipComplex to WorkoutType.COMPLEX
    )

    private var _binding: FragmentWorkoutListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutListViewModel by viewModels()

    private val workoutAdapter by lazy {
        WorkoutAdapter { workout ->
            navigateToVideoPlayer(workout)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupFilterChips()
        observeUiState()
        setupRetryButton()
    }

    private fun setupRecyclerView() {
        binding.workoutsRecyclerView.adapter = workoutAdapter
    }

    private fun setupSearchView() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchWorkouts(s?.toString() ?: "")
            }
        })
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val selectedType = checkedIds.firstOrNull()?.let { chipTypeMap[it] }
            viewModel.filterByType(selectedType)
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WorkoutListUiState.Loading -> showLoading()
                is WorkoutListUiState.Success -> showSuccess(state)
                is WorkoutListUiState.Error -> showError(state.message)
                is WorkoutListUiState.Empty -> showEmpty()
            }
        }
    }

    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener {
            viewModel.refreshWorkouts()
        }
    }

    private fun updateUiForState(vararg visibleViews: View) {
        val stateViews = listOf(
            binding.progressBar,
            binding.workoutsRecyclerView,
            binding.emptyStateLayout,
            binding.errorStateLayout
        )

        val visibleViewsSet = visibleViews.toSet()

        for (view in stateViews) {
            view.visibility = if (view in visibleViewsSet) View.VISIBLE else View.GONE
        }
    }

    private fun showLoading() {
        updateUiForState(binding.progressBar)
    }

    private fun showSuccess(state: WorkoutListUiState.Success) {
        updateUiForState(
            binding.workoutsRecyclerView,
            binding.filterChipGroup,
            binding.searchEditText
        )
        workoutAdapter.submitList(state.filteredWorkouts)
    }

    private fun showError(message: String) {
        updateUiForState(binding.errorStateLayout)
        binding.errorMessageTextView.text = message
    }

    private fun showEmpty() {
        updateUiForState(binding.emptyStateLayout)
    }

    private fun navigateToVideoPlayer(workout: Workout) {
        val action = WorkoutListFragmentDirections
            .actionWorkoutListFragmentToVideoPlayerFragment(workout)

        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}