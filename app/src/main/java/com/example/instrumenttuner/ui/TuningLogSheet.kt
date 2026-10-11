package com.example.instrumenttuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuningLogSheet(
    subtitle: String,
    entries: List<TuningEntry>,
    onClear: () -> Unit,
    onShare: () -> Unit,
    onExportCsv: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val rows = remember(entries) {
        val seen = mutableMapOf<String, TuningEntry>()
        entries.map { e ->
            val key = "${e.instrumentName}|${e.stringLabel}"
            val prev = seen[key]
            seen[key] = e
            e to prev
        }
    }
    val statsText = remember(entries) {
        if (entries.isEmpty()) null else run {
            val avg = entries.map { kotlin.math.abs(it.cents) }.average()
            val tight = entries.count { kotlin.math.abs(it.cents) <= 2f }
            "${entries.size} entries · avg ±${"%.1f".format(avg)}¢ · $tight within ±2¢"
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Tuning log", style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            statsText?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, fontSize = 12.sp, color = palette.textSub)
            }
            Spacer(Modifier.height(14.dp))

            if (entries.isEmpty()) {
                Text("Nothing recorded yet.\nLock a string and hold it in tune — each settled string lands here, and the log survives restarts.",
                    color = palette.textSub, fontSize = 14.sp)
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    rows.forEach { (e, prev) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(timeFormat.format(e.timeMs), fontSize = 12.sp, color = palette.textSub)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(buildString {
                                    append(e.noteLabel)
                                    if (e.stringLabel.isNotEmpty()) append(" · ${e.stringLabel}")
                                }, fontSize = 15.sp)
                                Text(e.instrumentName, fontSize = 11.sp, color = palette.textSub)
                                prev?.let { p ->
                                    val improved = kotlin.math.abs(e.cents) < kotlin.math.abs(p.cents)
                                    Text("was ${"%+.1f".format(p.cents)}¢", fontSize = 10.sp,
                                        color = if (improved) palette.primary else palette.textSub)
                                }
                            }
                            Text("${"%+.1f".format(e.cents)}¢", fontSize = 14.sp,
                                color = if (kotlin.math.abs(e.cents) <= 2f) palette.primary else palette.warn)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                if (entries.isNotEmpty()) {
                    TextButton(onClick = onExportCsv) { Text("CSV") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onClear) { Text("Clear") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onShare) { Text("Share") }
                    Spacer(Modifier.width(8.dp))
                }
                Button(onClick = onDismiss) { Text("Done") }
            }
        }
    }
}
