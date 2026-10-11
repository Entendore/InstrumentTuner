package com.example.instrumenttuner.audio

import com.example.instrumenttuner.TestAudio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.sin

class FftTest {

    @Test
    fun magnitudeDb_finds_peak_near_440Hz() {
        val sampleRate = 44100
        val size = 4096
        val spectrum = Fft.magnitudeDb(TestAudio.sine(440f, sampleRate, size))
        assertEquals(size / 2, spectrum.size)
        val peakBin = spectrum.indices.maxByOrNull { spectrum[it] }!!
        val binHz = sampleRate / size.toFloat()
        val detectedFreq = peakBin * binHz
        assertTrue("Detected $detectedFreq, expected ~440", abs(detectedFreq - 440f) < binHz)
    }

    @Test
    fun octaveDeviation_returns_null_for_empty_spectrum() {
        assertEquals(null, Fft.octaveDeviation(FloatArray(0), 44100, 440f))
    }

    @Test
    fun octaveDeviation_detects_sharp_second_harmonic() {
        val sampleRate = 44100
        val size = 8192
        val f0 = 110f
        val f1 = f0 * 2.02f   // 2nd harmonic ~2% sharp (~34¢)
        val samples = FloatArray(size) { i ->
            (sin(2.0 * PI * f0 * i / sampleRate) +
             0.8 * sin(2.0 * PI * f1 * i / sampleRate)).toFloat()
        }
        val deviation = Fft.octaveDeviation(Fft.magnitudeDb(samples), sampleRate, f0)
        assertNotNull(deviation)
        assertTrue("Expected ~34¢, got $deviation", deviation!! > 30f && deviation < 40f)
    }
}
