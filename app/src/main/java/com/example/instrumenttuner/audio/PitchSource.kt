package com.example.instrumenttuner.audio

import kotlinx.coroutines.flow.StateFlow

interface PitchSource {
    val pitch: StateFlow<PitchSample>
    val spectrum: StateFlow<FloatArray>
    var sensitivity: Float
    fun start()
    fun stop()
}
