package com.example.instrumenttuner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.session.PracticeSession
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeHistorySheet(
    subtitle: String,
    sessions: List<PracticeSession>,
    onClear: () -> Unit,
    onExportCsv: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val dateFormat = remember { SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Practice history", style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(14.dp))

            if (sessions.isEmpty()) {
                Text("No sessions yet for this instrument.\nTap ⏱ Practice to run timed rounds.",
                    color = palette.textSub, fontSize = 14.sp)
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    sessions.reversed().forEach { s ->
                        val best = s.rounds.minOf { it.seconds }
                        val avg = s.rounds.map { it.avgCents }.average()
                        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text(dateFormat.format(s.timeMs), fontSize = 12.sp, color = palette.textSub)
                            Text("${s.rounds.size} rounds · best ${best}s · avg ±${"%.1f".format(avg)}¢", fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                s.rounds.forEachIndexed { i, r ->
                                    val isBest = r.seconds == best
                                    Box(Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(if (isBest) palette.chipActive else palette.chip)
                                        .padding(horizontal = 9.dp, vertical = 5.dp)) {
                                        Text("R${i + 1} · ${r.seconds}s", fontSize = 11.sp,
                                            color = if (isBest) palette.primary else palette.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (sessions.isNotEmpty()) {
                    TextButton(onClick = onExportCsv) { Text("CSV") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onClear) { Text("Clear") }
                    Spacer(Modifier.width(8.dp))
                }
                Button(onClick = onDismiss) { Text("Done") }
            }
        }
    }
}
