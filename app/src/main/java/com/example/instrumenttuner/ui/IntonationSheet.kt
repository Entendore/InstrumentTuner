package com.example.instrumenttuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.audio.Fft
import kotlin.math.abs
import kotlin.math.log2

private data class IntervalTarget(val label: String, val cents: Float)

private val INTERVALS = listOf(
    IntervalTarget("Octave (1200¢)", 1200f),
    IntervalTarget("Fifth (700¢)", 700f),
    IntervalTarget("Fourth (500¢)", 500f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntonationSheet(
    liveFrequency: Float,
    hasPitch: Boolean,
    spectrum: FloatArray,
    sampleRate: Int,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    var targetIdx by remember { mutableStateOf(0) }
    var f1 by remember { mutableStateOf<Float?>(null) }
    var f2 by remember { mutableStateOf<Float?>(null) }
    val holder = remember { object { var lastF = 0f; var stable = 0 } }

    if (hasPitch) {
        val steady = holder.lastF > 0f && abs(1200f * log2(liveFrequency / holder.lastF)) < 2.5f
        holder.stable = if (steady) holder.stable + 1 else 0
        if (holder.stable == 12) {
            holder.stable = 0
            if (f1 == null) f1 = liveFrequency else if (f2 == null) f2 = liveFrequency
        }
        holder.lastF = liveFrequency
    } else {
        holder.lastF = 0f
        holder.stable = 0
    }

    val harmonicDev: Float? = Fft.octaveDeviation(spectrum, sampleRate, liveFrequency)
    val expected = INTERVALS[targetIdx].cents
    val interval = if (f1 != null && f2 != null) 1200f * log2(f2!! / f1!!) else null
    val deviation = interval?.minus(expected)

    val verdict = when {
        deviation == null -> null
        abs(deviation) <= 3f -> "Intonation good ✓"
        deviation > 0 -> if (targetIdx == 0)
            "${"%+.1f".format(deviation)}¢ sharp — lengthen the string (saddle toward tailpiece)"
        else "${"%+.1f".format(deviation)}¢ sharp"
        else -> if (targetIdx == 0)
            "${"%+.1f".format(deviation)}¢ flat — shorten the string (saddle toward neck)"
        else "${"%+.1f".format(deviation)}¢ flat"
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Intonation check", style = MaterialTheme.typography.titleLarge)
            Text("Setup & repair tool: play the first note, hold it still until captured, then play the second (e.g., open string → 12th fret, or two adjacent strings).",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(12.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                INTERVALS.forEachIndexed { i, t ->
                    FilterChip(selected = targetIdx == i,
                        onClick = { targetIdx = i; f1 = null; f2 = null },
                        label = { Text(t.label) }, colors = chipColors)
                }
            }

            if (harmonicDev != null) {
                Spacer(Modifier.height(12.dp))
                Text("Single-note harmonic check (live)", fontSize = 13.sp, color = palette.textSub)
                Text(
                    "2nd-harmonic octave: ${"%+.1f".format(harmonicDev)}¢" +
                        if (abs(harmonicDev) <= 2f) " — clean ✓"
                        else if (harmonicDev > 0) " — sharp: stiffness or saddle compensation"
                        else " — flat: check pickup/placement",
                    fontSize = 14.sp,
                    color = if (abs(harmonicDev) <= 2f) palette.primary else palette.warn
                )
                Spacer(Modifier.height(12.dp))
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("First note", fontSize = 13.sp, color = palette.textSub)
                    Text(f1?.let { "${"%.2f".format(it)} Hz ✓" } ?: "play & hold…",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = if (f1 != null) palette.primary else palette.dim)
                }
                if (f1 != null) TextButton(onClick = { f1 = null; f2 = null }) { Text("Retake") }
            }
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Second note", fontSize = 13.sp, color = palette.textSub)
                    Text(
                        when {
                            f1 == null -> "capture the first note first"
                            f2 == null -> "play & hold…"
                            else -> "${"%.2f".format(f2!!)} Hz ✓"
                        },
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = if (f2 != null) palette.primary else palette.dim
                    )
                }
                if (f2 != null) TextButton(onClick = { f2 = null }) { Text("Retake") }
            }

            Spacer(Modifier.height(16.dp))
            if (interval != null && deviation != null) {
                Text("Measured interval: ${"%.1f".format(interval)}¢ (target ${expected.toInt()}¢)", fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(verdict!!, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    color = if (abs(deviation) <= 3f) palette.primary else palette.warn)
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { f1 = null; f2 = null }) { Text("Reset") }
                Spacer(Modifier.weight(1f))
                Button(onClick = onDismiss) { Text("Done") }
            }
        }
    }
}
