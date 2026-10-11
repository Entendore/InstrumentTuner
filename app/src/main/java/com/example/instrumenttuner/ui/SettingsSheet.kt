package com.example.instrumenttuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.align
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.instrumenttuner.music.Note
import com.example.instrumenttuner.music.Temperament

data class TunerSettings(
    val useFlats: Boolean = false,
    val tolerance: Float = 5f,
    val haptics: Boolean = true,
    val autoAdvance: Boolean = true,
    val sensitivity: Float = 4f,
    val temperament: String = "Equal (modern)",
    val tonicPc: Int = 0,
    val practiceRounds: Int = 3,
    val theme: String = "Midnight",
    val stretch: Float = 0f,
    val transposition: Int = 0,
    val toneTimbre: String = "Pure"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: TunerSettings,
    a4: Float,
    onA4: (Float) -> Unit,
    onOpenCalibrate: () -> Unit,
    onOpenBeatTrainer: () -> Unit,
    onOpenOffsets: () -> Unit,
    onOpenScaleTrainer: () -> Unit,
    onChange: (TunerSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary)

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge)

            Column {
                Text("Note names", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !settings.useFlats, onClick = { onChange(settings.copy(useFlats = false)) }, label = { Text("Sharps  C♯") }, colors = chipColors)
                    FilterChip(selected = settings.useFlats, onClick = { onChange(settings.copy(useFlats = true)) }, label = { Text("Flats  D♭") }, colors = chipColors)
                }
            }

            Column {
                Text("Theme", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    THEMES.forEach { t ->
                        FilterChip(selected = settings.theme == t.name, onClick = { onChange(settings.copy(theme = t.name)) }, label = { Text(t.name) }, colors = chipColors)
                    }
                }
            }

            Column {
                Text("Transposing display (wind players)", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Concert", 2 to "B♭ (+2)", 3 to "A (+3)", 7 to "F (+7)", 9 to "E♭ (+9)").forEach { (semi, label) ->
                        FilterChip(selected = settings.transposition == semi, onClick = { onChange(settings.copy(transposition = semi)) }, label = { Text(label) }, colors = chipColors)
                    }
                }
                Text("Shows written pitch; the needle still measures the sounding note.", style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            }

            Column {
                Text("Quick calibration (A4)", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(415f, 432f, 440f, 442f).forEach { f ->
                        FilterChip(selected = kotlin.math.abs(a4 - f) < 0.5f, onClick = { onA4(f) }, label = { Text("${f.toInt()}") }, colors = chipColors)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onOpenCalibrate, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.chipActive)) {
                    Text("🎯 Capture A4 from a reference sound…")
                }
            }

            Column {
                Text("Reference tone timbre", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Pure", "Hollow", "Rich").forEach { t ->
                        FilterChip(selected = settings.toneTimbre == t, onClick = { onChange(settings.copy(toneTimbre = t)) }, label = { Text(t) }, colors = chipColors)
                    }
                }
                Text("Hollow adds the octave partial; Rich adds octave + 3rd partial.", style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            }

            Column {
                Text("In-tune window", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2f, 3f, 5f, 10f).forEach { t ->
                        FilterChip(selected = settings.tolerance == t, onClick = { onChange(settings.copy(tolerance = t)) }, label = { Text("±${t.toInt()}¢") }, colors = chipColors)
                    }
                }
            }

            Column {
                Text("Temperament", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Temperament.entries.forEach { t ->
                        FilterChip(selected = settings.temperament == t.label, onClick = { onChange(settings.copy(temperament = t.label)) }, label = { Text(t.label) }, colors = chipColors)
                    }
                }
                if (settings.temperament != Temperament.EQUAL.label) {
                    Spacer(Modifier.height(10.dp))
                    Text("Tonic (key): ${Note.NAMES_SHARP[settings.tonicPc]}", style = MaterialTheme.typography.bodySmall, color = palette.textSub)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Note.NAMES_SHARP.forEachIndexed { pc, nm ->
                            FilterChip(selected = settings.tonicPc == pc, onClick = { onChange(settings.copy(tonicPc = pc)) }, label = { Text(nm) }, colors = chipColors)
                        }
                    }
                }
            }

            Column {
                Text("Octave stretch (piano style)", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Slider(value = settings.stretch, onValueChange = { onChange(settings.copy(stretch = it)) },
                    valueRange = 0f..6f, steps = 11,
                    colors = SliderDefaults.colors(thumbColor = palette.primary, activeTrackColor = palette.primary))
                Text("+${"%.1f".format(settings.stretch)} ¢/octave away from A4 — 0 keeps pure equal divisions", style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            }

            Column {
                Text("Practice rounds", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 5).forEach { r ->
                        FilterChip(selected = settings.practiceRounds == r, onClick = { onChange(settings.copy(practiceRounds = r)) }, label = { Text("×$r") }, colors = chipColors)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pro tools", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Button(onClick = onOpenBeatTrainer, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = palette.chipActive)) { Text("👂 Beat ear trainer") }
                Button(onClick = onOpenOffsets, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = palette.chipActive)) { Text("🎚 Per-string target offsets") }
                Button(onClick = onOpenScaleTrainer, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = palette.chipActive)) { Text("🎼 Scale & maqam trainer") }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Haptic feedback", style = MaterialTheme.typography.bodyLarge)
                    Text("Short vibration when a string settles in tune", style = MaterialTheme.typography.bodySmall, color = palette.textSub)
                }
                Switch(checked = settings.haptics, onCheckedChange = { onChange(settings.copy(haptics = it)) }, colors = SwitchDefaults.colors(checkedTrackColor = palette.primary))
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Auto-advance strings", style = MaterialTheme.typography.bodyLarge)
                    Text("Jump to the next string once the current one settles in tune", style = MaterialTheme.typography.bodySmall, color = palette.textSub)
                }
                Switch(checked = settings.autoAdvance, onCheckedChange = { onChange(settings.copy(autoAdvance = it)) }, colors = SwitchDefaults.colors(checkedTrackColor = palette.primary))
            }

            Column {
                Text("Input sensitivity", style = MaterialTheme.typography.labelLarge, color = palette.textSub)
                Slider(value = settings.sensitivity, onValueChange = { onChange(settings.copy(sensitivity = it)) },
                    valueRange = 1f..8f, steps = 13,
                    colors = SliderDefaults.colors(thumbColor = palette.primary, activeTrackColor = palette.primary))
                Text("Noise gate × ${"%.1f".format(settings.sensitivity)} of ambient floor", style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            }

            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Done") }
        }
    }
}
