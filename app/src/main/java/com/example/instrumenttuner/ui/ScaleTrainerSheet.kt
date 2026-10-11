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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DisposableEffect
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.music.Note
import com.example.instrumenttuner.music.SCALES
import com.example.instrumenttuner.music.Scale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleTrainerSheet(
    a4: Float,
    onPlayFreq: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val scope = rememberCoroutineScope()
    var rootMidi by remember { mutableStateOf(60) }
    var scale by remember { mutableStateOf<Scale>(SCALES[0]) }
    var playAllJob by remember { mutableStateOf<Job?>(null) }
    val octave = rootMidi / 12 - 1

    val rootFreq = Note(rootMidi).frequency(a4)
    fun freqOf(cents: Float) = rootFreq * 2f.pow(cents / 1200f)

    DisposableEffect(Unit) { onDispose { playAllJob?.cancel() } }

    fun playScale() {
        playAllJob?.cancel()
        playAllJob = scope.launch {
            scale.tones.forEach { t ->
                onPlayFreq(freqOf(t.cents))
                delay(700)
            }
            onPlayFreq(rootFreq * 2f)
            playAllJob = null
        }
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp).verticalScroll(rememberScrollState())) {
            Text("Scale & maqam trainer", style = MaterialTheme.typography.titleLarge)
            Text("Reference tones for microtonal practice. Values are common approximations — trust your tradition and your ear. Tones follow A4 = ${a4.toInt()} Hz.",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Root: ${Note(rootMidi).label()}", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { rootMidi = (((octave - 1).coerceIn(0, 8) + 1) * 12 + rootMidi % 12) }) {
                    Text("Oct −", fontSize = 12.sp)
                }
                TextButton(onClick = { rootMidi = (((octave + 1).coerceIn(0, 8) + 1) * 12 + rootMidi % 12) }) {
                    Text("Oct +", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Note.NAMES_SHARP.forEachIndexed { pc, label ->
                    val selected = rootMidi % 12 == pc
                    Box(Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (selected) palette.chipActive else palette.chip)
                        .clickable { rootMidi = (octave + 1) * 12 + pc }
                        .padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text(label, fontSize = 13.sp,
                            color = if (selected) palette.primary else palette.textPrimary)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Scale", fontSize = 13.sp, color = palette.textSub)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SCALES.forEach { s ->
                    FilterChip(selected = scale.name == s.name,
                        onClick = { playAllJob?.cancel(); scale = s },
                        label = { Text("${s.name} · ${s.region}") }, colors = chipColors)
                }
            }
            if (scale.note.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(scale.note, fontSize = 11.sp, color = palette.dim)
            }

            Spacer(Modifier.height(14.dp))
            scale.tones.forEach { t ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t.label, fontSize = 14.sp)
                        Text("${"%+.0f".format(t.cents)}¢ from root", fontSize = 11.sp, color = palette.textSub)
                    }
                    Text("${"%.1f".format(freqOf(t.cents))} Hz", fontSize = 12.sp, color = palette.dim)
                    TextButton(onClick = { playAllJob?.cancel(); onPlayFreq(freqOf(t.cents)) }) {
                        Text("▶", fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = {
                    if (playAllJob?.isActive == true) playAllJob?.cancel() else playScale()
                }) { Text(if (playAllJob?.isActive == true) "■ Stop" else "▶ Play scale") }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}
