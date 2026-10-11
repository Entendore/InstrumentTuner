package com.example.instrumenttuner.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TheoryTest {

    @Test
    fun A4_at_440_is_exactly_440() {
        assertEquals(440.0f, Note(69).frequency(440f), 0.001f)
    }

    @Test
    fun octave_up_doubles_frequency() {
        assertEquals(Note(69).frequency(440f) * 2f, Note(81).frequency(440f), 0.01f)
    }

    @Test
    fun nearest_note_to_442_is_A4_sharp() {
        val (note, cents) = Note.nearest(442f, 440f)
        assertEquals(69, note.midi)
        assertTrue(cents > 0f)
        assertEquals(7.8f, cents, 0.2f)
    }

    @Test
    fun cents_between_10_cent_offset_is_exact() {
        val freq = 440f * kotlin.math.exp((10f / 1200f) * kotlin.math.ln(2f))
        assertEquals(10.0f, Note.centsBetween(freq, Note(69), 440f), 0.01f)
    }

    @Test
    fun equal_temperament_has_zero_offsets_everywhere() {
        for (midi in 0..127) assertEquals(0f, Temperament.EQUAL.offsetOf(midi, 0), 0.0001f)
    }

    @Test
    fun pythagorean_fifth_is_wider_than_equal() {
        assertTrue(Temperament.PYTHAGOREAN.offsetOf(55, 0) > 1.5f)
    }

    @Test
    fun meantone_major_third_is_narrow() {
        assertTrue(Temperament.MEANTONE.offsetOf(52, 0) < -13f)
    }

    @Test
    fun tempered_frequency_applies_offset() {
        val note = Note(52)
        val base = note.frequency(440f)
        val tempered = Temperament.MEANTONE.temperedFrequency(note, 440f, 0)
        val expected = base * kotlin.math.exp((-13.69f / 1200f) * kotlin.math.ln(2f))
        assertEquals(expected, tempered, 0.05f)
    }

    @Test
    fun changing_tonic_transposes_the_offset_pattern() {
        val fifthAboveC = Temperament.PYTHAGOREAN.offsetOf(55, 0)
        val fifthAboveD = Temperament.PYTHAGOREAN.offsetOf(57, 2)
        assertEquals(fifthAboveC, fifthAboveD, 0.0001f)
    }

    @Test
    fun every_scale_starts_on_root_and_stays_within_octave() {
        SCALES.forEach { scale ->
            assertEquals("Scale ${scale.name} must start at 0¢", 0f, scale.tones.first().cents, 0.0001f)
            scale.tones.forEach { assertTrue("Tone out of range in ${scale.name}", it.cents in 0f..1200f) }
        }
    }

    @Test
    fun scale_tone_labels_are_unique_per_scale() {
        SCALES.forEach { scale ->
            val labels = scale.tones.map { it.label }
            assertEquals("Duplicate label in ${scale.name}", labels.size, labels.toSet().size)
        }
    }
}
