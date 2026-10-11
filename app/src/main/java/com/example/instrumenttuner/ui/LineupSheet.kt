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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.music.Instrument
import com.example.instrumenttuner.session.LineupTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineupSheet(
    lineup: List<String>,
    all: List<Instrument>,
    active: Boolean,
    currentName: String?,
    templates: List<LineupTemplate>,
    templateName: String,
    onTemplateName: (String) -> Unit,
    onSaveTemplate: (String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onUseTemplate: (LineupTemplate, start: Boolean) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val available = remember(all, lineup) { all.filter { it.name !in lineup } }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp).verticalScroll(rememberScrollState())) {
            Text("Multi-instrument lineup", style = MaterialTheme.typography.titleLarge)
            Text("Queue several instruments and tune them back-to-back. Once one is fully tuned, the app moves to the next automatically.",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(14.dp))

            Text("Queue (${lineup.size})", fontSize = 13.sp, color = palette.textSub)
            Spacer(Modifier.height(6.dp))
            if (lineup.isEmpty()) {
                Text("Nothing queued yet — add instruments below.", fontSize = 13.sp, color = palette.dim)
            } else {
                lineup.forEachIndexed { i, name ->
                    val isCurrent = active && name == currentName
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}.", fontSize = 13.sp, color = palette.textSub, modifier = Modifier.width(24.dp))
                        Text(name, fontSize = 15.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) palette.primary else palette.textPrimary,
                            modifier = Modifier.weight(1f))
                        if (isCurrent) Text("tuning ▸", fontSize = 11.sp, color = palette.primary)
                        TextButton(onClick = { onRemove(name) }) { Text("✕", color = palette.danger) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = templateName, onValueChange = onTemplateName,
                    placeholder = { Text("Template name (e.g., Stage setup)") },
                    singleLine = true, modifier = Modifier.weight(1f))
                TextButton(onClick = { onSaveTemplate(templateName.trim()) },
                    enabled = lineup.isNotEmpty() && templateName.isNotBlank()) { Text("💾 Save") }
            }
            Text("Saves the current queue; an existing template with the same name is replaced.",
                fontSize = 10.sp, color = palette.dim)

            Spacer(Modifier.height(14.dp))
            Text("Templates (${templates.size})", fontSize = 13.sp, color = palette.textSub)
            Spacer(Modifier.height(6.dp))
            if (templates.isEmpty()) {
                Text("No templates yet.", fontSize = 13.sp, color = palette.dim)
            } else {
                templates.forEach { t ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(t.instruments.joinToString(" · "), fontSize = 11.sp,
                                color = palette.textSub, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        TextButton(onClick = { onUseTemplate(t, false) }) { Text("Load", fontSize = 12.sp) }
                        TextButton(onClick = { onUseTemplate(t, true) }) { Text("▶ Start", fontSize = 12.sp, color = palette.primary) }
                        TextButton(onClick = { onDeleteTemplate(t.name) }) { Text("✕", color = palette.danger) }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Add instruments", fontSize = 13.sp, color = palette.textSub)
            Spacer(Modifier.height(6.dp))
            available.forEach { inst ->
                Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(inst.name, fontSize = 14.sp)
                        Text(inst.regionName(), fontSize = 10.sp, color = palette.textSub)
                    }
                    TextButton(onClick = { onAdd(inst.name) }) { Text("＋ Add", fontSize = 12.sp) }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Close") }
                Spacer(Modifier.width(8.dp))
                if (active) {
                    Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = palette.danger)) {
                        Text("Stop lineup")
                    }
                } else {
                    Button(onClick = onStart, enabled = lineup.isNotEmpty()) { Text("Start lineup") }
                }
            }
        }
    }
}
