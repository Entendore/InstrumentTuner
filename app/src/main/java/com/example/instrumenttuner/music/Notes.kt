package com.example.instrumenttuner.music

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

data class Note(val midi: Int) {
    val octave: Int get() = midi / 12 - 1
    fun name(useFlats: Boolean = false): String =
        (if (useFlats) NAMES_FLAT else NAMES_SHARP)[((midi % 12) + 12) % 12]
    fun label(useFlats: Boolean = false): String = "${name(useFlats)}$octave"
    fun frequency(a4: Float = 440f): Float = (a4 * 2.0.pow((midi - 69) / 12.0)).toFloat()

    companion object {
        val NAMES_SHARP = listOf("C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B")
        val NAMES_FLAT  = listOf("C","D♭","D","E♭","E","F","G♭","G","A♭","A","B♭","B")

        fun nearest(frequency: Float, a4: Float): Pair<Note, Float> {
            val midiExact = 69f + 12f * log2(frequency / a4)
            val midi = midiExact.roundToInt().coerceIn(0, 127)
            return Note(midi) to (midiExact - midi) * 100f
        }
        fun centsBetween(frequency: Float, target: Note, a4: Float): Float =
            1200f * log2(frequency / target.frequency(a4))
    }
}
