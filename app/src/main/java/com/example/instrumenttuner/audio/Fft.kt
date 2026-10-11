package com.example.instrumenttuner.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

object Fft {

    fun magnitudeDb(samples: FloatArray): FloatArray {
        val n = samples.size
        if (n < 2 || (n and (n - 1)) != 0) return FloatArray(0)
        val re = DoubleArray(n)
        val im = DoubleArray(n)
        for (i in 0 until n) {
            val w = 0.5 * (1.0 - cos(2.0 * PI * i / (n - 1)))
            re[i] = samples[i] * w
        }
        transform(re, im)
        val half = n / 2
        val out = FloatArray(half)
        for (k in 0 until half) {
            val mag = sqrt(re[k] * re[k] + im[k] * im[k]) / n
            out[k] = (20.0 * log10(max(mag, 1e-7))).toFloat()
        }
        return out
    }

    fun octaveDeviation(spectrum: FloatArray, sampleRate: Int, f0: Float): Float? {
        if (spectrum.isEmpty() || f0 <= 0f) return null
        val binHz = sampleRate / (spectrum.size * 2f)
        val targetBin = (f0 * 2f / binHz).toInt()
        if (targetBin < 2 || targetBin >= spectrum.size - 1) return null
        val search = (targetBin * 0.03f).toInt().coerceAtLeast(3)
        val lo = (targetBin - search).coerceAtLeast(1)
        val hi = (targetBin + search).coerceAtMost(spectrum.size - 2)
        var bestK = targetBin
        var bestDb = -100f
        for (k in lo..hi) { if (spectrum[k] > bestDb) { bestDb = spectrum[k]; bestK = k } }
        if (bestDb <= -45f) return null
        val l = spectrum[bestK - 1]; val r = spectrum[bestK + 1]
        val denom = l - 2f * bestDb + r
        val p = if (abs(denom) > 1e-6f) (0.5f * (l - r) / denom).coerceIn(-0.5f, 0.5f) else 0f
        return 1200f * log2(((bestK + p) * binHz) / (2f * f0))
    }

    private fun transform(re: DoubleArray, im: DoubleArray) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j or bit
            if (i < j) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
                val ti = im[i]; im[i] = im[j]; im[j] = ti
            }
        }
        var len = 2
        while (len <= n) {
            val ang = -2.0 * PI / len
            val wr = cos(ang); val wi = sin(ang)
            val halfLen = len / 2
            var i = 0
            while (i < n) {
                var curR = 1.0; var curI = 0.0
                for (k in 0 until halfLen) {
                    val ur = re[i + k]; val ui = im[i + k]
                    val vr = re[i + k + halfLen] * curR - im[i + k + halfLen] * curI
                    val vi = re[i + k + halfLen] * curI + im[i + k + halfLen] * curR
                    re[i + k] = ur + vr; im[i + k] = ui + vi
                    re[i + k + halfLen] = ur - vr; im[i + k + halfLen] = ui - vi
                    val nr = curR * wr - curI * wi
                    curI = curR * wi + curI * wr; curR = nr
                }
                i += len
            }
            len *= 2
        }
    }
}
