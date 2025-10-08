package com.testtask.fitnesstest.domain.util

class PlaybackSpeedManager(
    private val initialSpeedIndex: Int = 2
) {
    private val speedOptions = floatArrayOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    private var currentIndex = initialSpeedIndex

    val currentSpeed: Float
        get() = speedOptions[currentIndex]

    fun getNextSpeed(): Float {
        currentIndex = (currentIndex + 1) % speedOptions.size
        return currentSpeed
    }
}