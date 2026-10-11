package com.example.instrumenttuner.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Deterministic pitch input for E2E tests — no microphone needed. */
class FakePitchSource : PitchSource {
    private val _pitch = MutableStateFlow(PitchSample())
    private val _spectrum = MutableStateFlow(FloatArray(0))

    override val pitch: StateFlow<PitchSample> = _pitch
    override val spectrum: StateFlow<FloatArray> = _spectrum
    override var sensitivity: Float = 4f

    var started = false
        private set

    override fun start() { started = true }
    override fun stop() { started = false }

    fun emit(frequency: Float, clarity: Float = 0.95f, level: Float = 0.05f) {
        _pitch.value = PitchSample(frequency, clarity, level, 0.003f)
    }

    fun emitSilence() { _pitch.value = PitchSample() }
}
