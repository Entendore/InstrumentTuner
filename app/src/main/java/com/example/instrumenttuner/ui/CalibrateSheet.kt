package com.example.instrumenttuner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.music.Note
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

private const val CAPTURE_SAMPLES = 16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrateSheet(
    a4: Float,
    liveFrequency: Float,
    hasPitch: Boolean,
    history: List<String>,
    onApply: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    var refMidi by remember { mutableStateOf(69) }
    val holder = remember { object { val list = ArrayList<Float>(); var lastRef = 69 } }
    val refNote = Note(refMidi)

    if (holder.lastRef != refMidi) { holder.list.clear(); holder.lastRef = refMidi }
    if (hasPitch && abs(Note.centsBetween(liveFrequency, refNote, a4)) <= 60f) {
        holder.list.add(liveFrequency)
        if (holder.list.size > 24) holder.list.removeAt(0)
    }

    val ready = holder.list.size >= CAPTURE_SAMPLES
    val median = if (ready) holder.list.sorted()[holder.list.size / 2] else null
    val previewA4 = median?.let { (it / 2f.pow((refMidi - 69f) / 12f)).coerceIn(400f, 480f) }
    val heardCents = if (hasPitch) Note.centsBetween(liveFrequency, refNote, a4) else null
    val octave = refMidi / 12 - 1

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Calibrate A4 by ear", style = MaterialTheme.typography.titleLarge)
            Text("Play a steady reference pitch — an oboe A, a piano key, a tuning fork near the mic. Any note works; pick which one you're playing.",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reference: ${refNote.label()}", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { refMidi = ((((octave - 1).coerceIn(0, 8)) + 1) * 12 + refMidi % 12) }) {
                    Text("Oct −", fontSize = 12.sp)
                }
                TextButton(onClick = { refMidi = ((((octave + 1).coerceIn(0, 8)) + 1) * 12 + refMidi % 12) }) {
                    Text("Oct +", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Note.NAMES_SHARP.forEachIndexed { pc, label ->
                    val selected = refMidi % 12 == pc
                    Box(Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (selected) palette.chipActive else palette.chip)
                        .clickable { refMidi = (octave + 1) * 12 + pc }
                        .padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text(label, fontSize = 13.sp,
                            color = if (selected) palette.primary else palette.textPrimary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                when {
                    !hasPitch -> "Listening…"
                    heardCents != null && abs(heardCents) <= 60f ->
                        "Capturing ${"%.1f".format(liveFrequency)} Hz (${if (heardCents < 0) "♭" else "♯"} ${"%.1f".format(abs(heardCents))}¢ vs ${refNote.label()} at A4=${a4.toInt()})"
                    else -> "Play closer to ${refNote.label()}…"
                },
                fontSize = 13.sp, color = if (ready) palette.primary else palette.textSub
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { min(1f, holder.list.size.toFloat() / CAPTURE_SAMPLES) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = palette.primary, trackColor = palette.track
            )

            Spacer(Modifier.height(14.dp))
            previewA4?.let {
                Text("Detected ${refNote.label()} = ${"%.2f".format(median!!)} Hz → A4 = ${"%.1f".format(it)} Hz",
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.primary)
            }

            if (history.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Recent calibrations", fontSize = 13.sp, color = palette.textSub)
                history.takeLast(5).reversed().forEach { line ->
                    Text(line, fontSize = 11.sp, color = palette.dim)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { holder.list.clear() }) { Text("Reset") }
                TextButton(onClick = onDismiss) { Text("Close") }
                Spacer(Modifier.weight(1f))
                Button(enabled = ready && previewA4 != null, onClick = { onApply(previewA4!!) }) {
                    Text("Apply calibration")
                }
            }
        }
    }
}
