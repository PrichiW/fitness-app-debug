package com.testtask.fitnesstest.presentation.workoutlist

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerControlView
import androidx.navigation.fragment.navArgs
import com.testtask.fitnesstest.R
import com.testtask.fitnesstest.databinding.CustomPlayerControlBinding
import com.testtask.fitnesstest.databinding.FragmentVideoPlayerBinding
import com.testtask.fitnesstest.domain.model.Workout
import com.testtask.fitnesstest.domain.util.PlaybackSpeedManager
import com.testtask.fitnesstest.presentation.model.VideoPlayerUiState
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.OkHttpClient
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayerFragment : Fragment() {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    private var _binding: FragmentVideoPlayerBinding? = null
    private val binding get() = _binding ?: error("Binding is null. View was destroyed")

    private var playerControlsBinding: CustomPlayerControlBinding? =
        null

    private val viewModel: VideoPlayerViewModel by viewModels()

    private var player: ExoPlayer? = null
    private val speedManager = PlaybackSpeedManager()
    private var videoUrl: String? = null
    private var playWhenReady = false
    private var playbackPosition = 0L

    private var isFullscreen = false

    private val args: VideoPlayerFragmentArgs by navArgs()
    private val mainActivity: AppCompatActivity?
        get() = activity as? AppCompatActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        if (savedInstanceState != null) {
            isFullscreen = savedInstanceState.getBoolean("IS_FULLSCREEN_KEY", false)
        }

        if (isFullscreen) {
            enterFullscreen(mainActivity!!)
        }

        val workout = args.workout
        viewModel.loadVideoWorkout(workout)
        observeUiState()
        setupRetryButton(workout)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("IS_FULLSCREEN_KEY", isFullscreen)
    }

    private fun observeUiState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is VideoPlayerUiState.Loading -> showLoading()
                is VideoPlayerUiState.Success -> showSuccess(state)
                is VideoPlayerUiState.Error -> showError(state.message)
            }
        }
    }

    private fun setupRetryButton(workout: Workout) {
        binding.retryButton.setOnClickListener {
            viewModel.retry(workout)
        }
    }

    private fun showLoading() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            videoPlayerContainer.visibility = View.GONE
            contentScrollView.visibility = View.GONE
            errorStateLayout.visibility = View.GONE
        }
    }

    private fun showSuccess(state: VideoPlayerUiState.Success) {
        with(binding) {
            progressBar.visibility = View.GONE
            videoPlayerContainer.visibility = View.VISIBLE
            contentScrollView.visibility = View.VISIBLE
            errorStateLayout.visibility = View.GONE

            titleTextView.text = state.workout.title
            typeTextView.text = state.workout.type.displayName

            val durationInMinutes = state.workout.durationInMinutes
            durationTextView.text = resources.getQuantityString(
                R.plurals.minutes_plural,
                durationInMinutes,
                durationInMinutes
            )

            if (state.workout.description.isNullOrBlank()) {
                descriptionTextView.text = "Нет описания"
            } else {
                descriptionTextView.text = state.workout.description
            }
        }

        this.videoUrl = state.videoUrl
        if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            initializePlayer()
        }
    }

    private fun showError(message: String) {
        with(binding) {
            progressBar.visibility = View.GONE
            videoPlayerContainer.visibility = View.GONE
            contentScrollView.visibility = View.GONE
            errorStateLayout.visibility = View.VISIBLE
            errorMessageTextView.text = message
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupPlayerControls() {
        val controllerView =
            binding.playerView.findViewById<PlayerControlView>(androidx.media3.ui.R.id.exo_controller)

        val customControlLayout = controllerView?.getChildAt(0)

        if (customControlLayout != null) {
            playerControlsBinding =
                CustomPlayerControlBinding.bind(
                    customControlLayout
                )
        }

        playerControlsBinding?.speedButton?.setOnClickListener {
            val newSpeed = speedManager.getNextSpeed()
            player?.playbackParameters = PlaybackParameters(newSpeed)

            val speedText = getString(R.string.playback_speed_format, newSpeed)
            playerControlsBinding?.speedButton?.text = speedText
        }

        playerControlsBinding?.fullscreenButton?.setOnClickListener {
            toggleFullscreen()
        }

        playerControlsBinding?.exoSettings?.setOnClickListener {
            showQualityDialog()
        }
    }

    private fun showQualityDialog() {
        val qualities = listOf("Авто", "1080", "720p", "480p")
        AlertDialog.Builder(requireContext())
            .setTitle("Выбор качества")
            .setItems(qualities.toTypedArray()) { _, which ->
                Toast.makeText(requireContext(), "Выбрано: ${qualities[which]}", Toast.LENGTH_SHORT)
                    .show()
            }
            .show()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (isFullscreen) {
                toggleFullscreen()
            } else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        onBackPressedCallback.isEnabled = isFullscreen

        val activity = mainActivity ?: return

        if (isFullscreen) {
            enterFullscreen(activity)
        } else {
            exitFullscreen(activity)
        }
    }

    private fun enterFullscreen(activity: AppCompatActivity) {
        activity.supportActionBar?.hide()
        binding.contentScrollView.visibility = View.GONE

        val params = binding.videoPlayerContainer.layoutParams as ConstraintLayout.LayoutParams
        params.dimensionRatio = null
        params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        binding.videoPlayerContainer.layoutParams = params

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        hideSystemUI()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun exitFullscreen(activity: AppCompatActivity) {
        activity.supportActionBar?.show()
        binding.contentScrollView.visibility = View.VISIBLE
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.root.post {
            val params = binding.videoPlayerContainer.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "16:9"
            params.height = 0
            binding.videoPlayerContainer.layoutParams = params
        }

        showSystemUI()
    }

    private fun hideSystemUI() {
        val window = requireActivity().window
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        val window = requireActivity().window
        WindowInsetsControllerCompat(window, binding.root)
            .show(WindowInsetsCompat.Type.systemBars())
    }

    private fun initializePlayer() {
        if (player != null) return
        val url = videoUrl ?: return

        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(requireContext())
            .setDataSourceFactory(dataSourceFactory)

        player = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        showError("Ошибка воспроизведения: ${error.message}")
                    }
                })

                val mediaItem = MediaItem.fromUri(url)
                setMediaItem(mediaItem)
                seekTo(playbackPosition)
                this.playWhenReady = this@VideoPlayerFragment.playWhenReady
                prepare()
            }

        binding.playerView.player = player
        setupPlayerControls()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            this.playWhenReady = exoPlayer.playWhenReady
            this.playbackPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
        player = null
        playerControlsBinding = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (requireActivity().requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            showSystemUI()
        }

        playerControlsBinding = null
        _binding = null
    }
}