package com.example.instrumenttuner.audio

import kotlin.math.abs
import kotlin.math.min

class YinPitchDetector(
    private val sampleRate: Int = 44_100,
    bufferSize: Int = 4_096,
    private val threshold: Float = 0.15f,
    private val minFrequency: Float = 27.5f,
    private val maxFrequency: Float = 2_093f
) {
    data class Result(val frequency: Float, val clarity: Float)

    private val window = bufferSize / 2
    private val cmnd = FloatArray(window + 1)

    fun detect(samples: FloatArray): Result {
        val n = min(samples.size / 2, window)
        val tauMin = maxOf(2, (sampleRate / maxFrequency).toInt())
        val tauMax = minOf(n - 1, (sampleRate / minFrequency).toInt())
        if (tauMax <= tauMin) return Result(-1f, 0f)
        var runningSum = 0f
        cmnd[0] = 1f
        for (tau in 1..tauMax) {
            var sum = 0f
            for (i in 0 until n) {
                val d = samples[i] - samples[i + tau]
                sum += d * d
            }
            runningSum += sum
            cmnd[tau] = if (runningSum > 1e-9f) sum * tau / runningSum else 1f
        }
        var tau = tauMin
        var tauEstimate = -1
        while (tau <= tauMax) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 <= tauMax && cmnd[tau + 1] < cmnd[tau]) tau++
                tauEstimate = tau
                break
            }
            tau++
        }
        if (tauEstimate == -1) return Result(-1f, 0f)
        val betterTau = interpolate(tauEstimate, tauMax)
        return Result(sampleRate / betterTau, 1f - cmnd[tauEstimate])
    }

    private fun interpolate(tau: Int, tauMax: Int): Float {
        val x0 = if (tau > 1) tau - 1 else tau
        val x2 = if (tau + 1 <= tauMax) tau + 1 else tau
        if (x0 == x2) return tau.toFloat()
        val s0 = cmnd[x0]; val s1 = cmnd[tau]; val s2 = cmnd[x2]
        val denom = 2f * s1 - s2 - s0
        if (abs(denom) < 1e-9f) return tau.toFloat()
        return tau + (s2 - s0) / (2f * denom)
    }
}
