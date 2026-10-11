package com.example.instrumenttuner.ui

import androidx.compose.foundation.background
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
import com.example.instrumenttuner.music.Instrument
import com.example.instrumenttuner.music.Note

data class DetectMatch(val instrument: Instrument, val matched: Int, val total: Int)

fun rankCandidates(notes: List<Note>, all: List<Instrument>): List<DetectMatch> {
    if (notes.isEmpty()) return emptyList()
    val heard = notes.map { it.midi }.toSet()
    return all
        .map { inst -> DetectMatch(inst, inst.strings.count { it.note.midi in heard }, inst.strings.size) }
        .filter { it.matched > 0 }
        .sortedWith(compareByDescending<DetectMatch> { it.matched }
            .thenBy { it.total - it.matched }.thenBy { it.total })
        .take(5)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectSheet(
    collected: List<Note>,
    candidates: List<DetectMatch>,
    useFlats: Boolean,
    onUse: (Instrument) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Detect instrument", style = MaterialTheme.typography.titleLarge)
            Text("Play each open string once, one at a time (remove the capo). The tuner listens and matches what it hears against every preset.",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Heard so far (${collected.size})", fontSize = 13.sp,
                    color = palette.textSub, modifier = Modifier.weight(1f))
                if (collected.isNotEmpty()) {
                    TextButton(onClick = onClear) { Text("Clear", fontSize = 12.sp) }
                }
            }
            Spacer(Modifier.height(6.dp))
            if (collected.isEmpty()) {
                Text("Waiting for notes…", fontSize = 13.sp, color = palette.dim)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    collected.forEach { n ->
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(palette.chipActive)
                            .padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Text(n.label(useFlats), fontSize = 13.sp, color = palette.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("Best matches", fontSize = 13.sp, color = palette.textSub)
            Spacer(Modifier.height(6.dp))
            if (candidates.isEmpty()) {
                Text("Play a few strings to see suggestions.", fontSize = 13.sp, color = palette.dim)
            } else {
                candidates.forEach { m ->
                    val full = m.matched == m.total
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(m.instrument.name, fontSize = 15.sp,
                                fontWeight = if (full) FontWeight.Bold else FontWeight.Normal)
                            Text("${m.instrument.regionName()} · ${m.matched} of ${m.total} strings",
                                fontSize = 11.sp, color = if (full) palette.primary else palette.textSub)
                        }
                        Button(onClick = { onUse(m.instrument) }) {
                            Text(if (full) "Use ✓" else "Use")
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}
