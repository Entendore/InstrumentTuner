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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.instrumenttuner.music.Note
import com.example.instrumenttuner.presets.CustomPresetData

private data class DraftNote(val midi: Int, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditorSheet(
    initial: CustomPresetData?,
    takenNames: Set<String>,
    onSave: (name: String, midis: List<Int>, labels: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    val notes = remember {
        mutableStateListOf<DraftNote>().apply {
            initial?.midis?.forEachIndexed { i, m ->
                add(DraftNote(m, initial.labels.getOrNull(i).orEmpty()))
            }
        }
    }
    var octave by remember {
        mutableIntStateOf(initial?.midis?.lastOrNull()?.let { it / 12 - 1 }?.coerceIn(0, 8) ?: 3)
    }
    var editingIndex by remember { mutableStateOf(-1) }

    val trimmed = name.trim()
    val isTaken = trimmed.isNotEmpty() && trimmed != initial?.name &&
        takenNames.any { it.equals(trimmed, ignoreCase = true) }
    val canSave = trimmed.isNotEmpty() && notes.isNotEmpty() && !isTaken

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(if (initial == null) "New custom preset" else "Edit “${initial.name}”",
                style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Preset name") }, singleLine = true, isError = isTaken,
                supportingText = if (isTaken) { { Text("A preset with this name already exists") } } else null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("input_preset_name")
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Add notes in octave:", fontSize = 13.sp, color = palette.textSub, modifier = Modifier.weight(1f))
                TextButton(onClick = { octave = (octave - 1).coerceIn(0, 8) }) { Text("−", fontSize = 20.sp) }
                Text("$octave", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { octave = (octave + 1).coerceIn(0, 8) }) { Text("+", fontSize = 20.sp) }
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Note.NAMES_SHARP.forEachIndexed { pc, label ->
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(palette.chip)
                        .clickable { notes.add(DraftNote((octave + 1) * 12 + pc, "")) }
                        .padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text(label, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Notes (${notes.size}) — tap one to label or remove", fontSize = 13.sp,
                    color = palette.textSub, modifier = Modifier.weight(1f))
                TextButton(onClick = { notes.sortBy { it.midi } }) { Text("Sort", fontSize = 12.sp) }
                TextButton(onClick = { notes.clear() }) { Text("Clear", fontSize = 12.sp) }
            }
            if (notes.isEmpty()) {
                Text("Tap the note buttons above to add strings, in playing order.",
                    fontSize = 12.sp, color = palette.dim)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    notes.forEachIndexed { i, dn ->
                        val custom = dn.label.takeIf { it.isNotBlank() }
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(palette.chipActive)
                            .clickable { editingIndex = i }
                            .padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Text(buildString {
                                append(Note(dn.midi).label())
                                if (custom != null) append(" · $custom")
                            }, fontSize = 13.sp, color = palette.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(enabled = canSave, onClick = {
                    onSave(trimmed, notes.map { it.midi }, notes.map { it.label })
                }) { Text("Save preset") }
            }
        }
    }

    if (editingIndex in notes.indices) {
        val dn = notes[editingIndex]
        var labelText by remember(editingIndex) { mutableStateOf(dn.label) }
        AlertDialog(
            onDismissRequest = { editingIndex = -1 },
            title = { Text("Note ${Note(dn.midi).label()}") },
            text = {
                OutlinedTextField(value = labelText, onValueChange = { labelText = it },
                    label = { Text("Label (optional)") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp))
            },
            confirmButton = {
                TextButton(onClick = {
                    notes[editingIndex] = dn.copy(label = labelText.trim())
                    editingIndex = -1
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        notes.removeAt(editingIndex); editingIndex = -1
                    }) { Text("Remove", color = palette.danger) }
                    TextButton(onClick = { editingIndex = -1 }) { Text("Cancel") }
                }
            }
        )
    }
}
