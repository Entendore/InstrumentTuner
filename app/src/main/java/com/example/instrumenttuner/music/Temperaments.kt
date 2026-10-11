package com.example.instrumenttuner.music

import kotlin.math.pow

enum class Temperament(val label: String, private val offsets: FloatArray) {
    EQUAL("Equal (modern)", FloatArray(12)),
    PYTHAGOREAN("Pythagorean", floatArrayOf(
        0f, -9.78f, 3.91f, -5.87f, 7.82f, -1.96f,
        -11.73f, 1.96f, -7.82f, 5.87f, -3.91f, 9.78f)),
    MEANTONE("¼-comma meantone", floatArrayOf(
        0f, 17.11f, -6.84f, 10.26f, -13.69f, 3.42f,
        -20.53f, -3.42f, 13.69f, -10.27f, 6.84f, -17.11f)),
    JUST("Just intonation (major)", floatArrayOf(
        0f, 11.73f, 3.91f, 15.64f, -13.69f, -1.96f,
        -9.78f, 1.96f, 13.69f, -15.64f, 17.60f, -11.73f));

    fun offsetOf(midi: Int, tonicPc: Int): Float =
        offsets[((midi % 12) - tonicPc + 24) % 12]

    fun temperedFrequency(note: Note, a4: Float, tonicPc: Int): Float {
        val off = offsetOf(note.midi, tonicPc)
        return if (off == 0f) note.frequency(a4) else note.frequency(a4) * 2f.pow(off / 1200f)
    }
}
