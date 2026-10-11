package com.example.instrumenttuner

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object TestAudio {
    fun sine(freq: Float, sampleRate: Int = 44_100, size: Int = 4096, amplitude: Float = 0.5f) =
        FloatArray(size) { i -> (amplitude * sin(2.0 * PI * freq * i / sampleRate)).toFloat() }

    fun sineWithHarmonics(f0: Float, sampleRate: Int = 44_100, size: Int = 4096) =
        FloatArray(size) { i ->
            val t = i.toDouble()
            (0.50 * sin(2 * PI * f0 * t / sampleRate) +
             0.30 * sin(2 * PI * 2 * f0 * t / sampleRate) +
             0.15 * sin(2 * PI * 3 * f0 * t / sampleRate)).toFloat()
        }

    fun noise(size: Int = 4096, amplitude: Float = 0.3f, seed: Long = 42): FloatArray {
        val rnd = Random(seed)
        return FloatArray(size) { (rnd.nextFloat() * 2f - 1f) * amplitude }
    }

    fun silence(size: Int = 4096) = FloatArray(size)
}
