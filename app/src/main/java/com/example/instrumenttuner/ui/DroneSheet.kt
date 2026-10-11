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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.music.Note

data class DroneStack(val label: String, val ratios: List<Float>)

val DRONE_STACKS = listOf(
    DroneStack("Root", listOf(1f)),
    DroneStack("Octave (2:1)", listOf(1f, 2f)),
    DroneStack("Fifth (3:2)", listOf(1f, 1.5f)),
    DroneStack("Fourth (4:3)", listOf(1f, 4f / 3f)),
    DroneStack("Sa–Pa (1 · 3:2 · 2:1)", listOf(1f, 1.5f, 2f))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroneSheet(
    rootMidi: Int,
    stackIndex: Int,
    isPlaying: Boolean,
    subtitle: String,
    onRoot: (Int) -> Unit,
    onStack: (Int) -> Unit,
    onToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val octave = rootMidi / 12 - 1
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Drone", style = MaterialTheme.typography.titleLarge)
            Text("A sustained reference for raga, maqam & ear practice. Intervals are pure ratios above the root (which follows your temperament).",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Root: ${Note(rootMidi).label()}", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onRoot((((octave - 1).coerceIn(0, 8)) + 1) * 12 + (rootMidi % 12)) }) {
                    Text("Oct −", fontSize = 12.sp)
                }
                TextButton(onClick = { onRoot((((octave + 1).coerceIn(0, 8)) + 1) * 12 + (rootMidi % 12)) }) {
                    Text("Oct +", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Note.NAMES_SHARP.forEachIndexed { pc, label ->
                    val selected = rootMidi % 12 == pc
                    Box(Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (selected) palette.chipActive else palette.chip)
                        .clickable { onRoot((octave + 1) * 12 + pc) }
                        .padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text(label, fontSize = 13.sp,
                            color = if (selected) palette.primary else palette.textPrimary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Interval stack", fontSize = 13.sp, color = palette.textSub)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DRONE_STACKS.forEachIndexed { i, stack ->
                    FilterChip(selected = stackIndex == i, onClick = { onStack(i) },
                        label = { Text(stack.label) }, colors = chipColors)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(subtitle, fontSize = 12.sp, color = palette.textSub)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Close") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) palette.danger else palette.primary)) {
                    Text(if (isPlaying) "Stop drone" else "Start drone")
                }
            }
        }
    }
}
