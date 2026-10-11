package com.example.instrumenttuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.music.Instrument
import com.example.instrumenttuner.music.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffsetsSheet(
    instrument: Instrument,
    useFlats: Boolean,
    capo: Int,
    offsets: Map<Int, Float>,
    onChange: (midi: Int, value: Float) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp).verticalScroll(rememberScrollState())) {
            Text("Per-string offsets", style = MaterialTheme.typography.titleLarge)
            Text("Compensation targets for ${instrument.name}${if (capo > 0) " (capo $capo)" else ""}. Applied to the needle, reference tones, checklist and log — real strings rarely land exactly on theory.",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(14.dp))

            instrument.strings.forEach { s ->
                val note = Note(s.note.midi + capo)
                val off = offsets[note.midi] ?: 0f
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(note.label(useFlats), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (s.label.isNotEmpty()) Text(s.label, fontSize = 11.sp, color = palette.textSub)
                    }
                    TextButton(onClick = { onChange(note.midi, (off - 0.5f).coerceIn(-25f, 25f)) }) { Text("−", fontSize = 20.sp) }
                    Text("${"%+.1f".format(off)}¢", fontSize = 15.sp,
                        color = if (off == 0f) palette.textSub else palette.primary)
                    TextButton(onClick = { onChange(note.midi, (off + 0.5f).coerceIn(-25f, 25f)) }) { Text("+", fontSize = 20.sp) }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onReset) { Text("Reset all") }
                Spacer(Modifier.weight(1f))
                Button(onClick = onDismiss) { Text("Done") }
            }
        }
    }
}
