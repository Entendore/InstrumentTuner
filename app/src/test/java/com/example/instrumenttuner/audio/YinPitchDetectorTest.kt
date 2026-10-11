package com.example.instrumenttuner.audio

import com.example.instrumenttuner.TestAudio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YinPitchDetectorTest {

    private val detector = YinPitchDetector(sampleRate = 44_100, bufferSize = 4096)

    @Test
    fun detects_A4_440Hz_within_1Hz() {
        val r = detector.detect(TestAudio.sine(440f))
        assertEquals(440f, r.frequency, 1f)
        assertTrue("clarity should be high for a pure tone", r.clarity > 0.9f)
    }

    @Test
    fun detects_bass_E1_41Hz_at_window_limit() {
        val r = detector.detect(TestAudio.sine(41.2f))
        assertEquals(41.2f, r.frequency, 1.5f)
    }

    @Test
    fun detects_guitar_low_E2() {
        val r = detector.detect(TestAudio.sine(82.41f))
        assertEquals(82.41f, r.frequency, 1f)
    }

    @Test
    fun detects_violin_E5_659Hz() {
        val r = detector.detect(TestAudio.sine(659.3f))
        assertEquals(659.3f, r.frequency, 2f)
    }

    @Test
    fun tracks_fundamental_through_harmonics() {
        val r = detector.detect(TestAudio.sineWithHarmonics(110f))
        assertEquals("must not jump to a partial", 110f, r.frequency, 1.5f)
    }

    @Test
    fun detects_detuned_442Hz() {
        val r = detector.detect(TestAudio.sine(442f))
        assertEquals(442f, r.frequency, 1f)
    }

    @Test
    fun silence_returns_no_pitch() {
        val r = detector.detect(TestAudio.silence())
        assertEquals(-1f, r.frequency, 0f)
    }

    @Test
    fun white_noise_is_rejected_or_flagged_low_clarity() {
        val r = detector.detect(TestAudio.noise())
        assertTrue(r.frequency < 0f || r.clarity < 0.8f)
    }
}
