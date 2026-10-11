package com.example.instrumenttuner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.music.Instrument
import com.example.instrumenttuner.music.Note
import com.example.instrumenttuner.presets.CustomPresetStore

private val INSTRUMENT_REGIONS = mapOf(
    "Guitar" to "Western", "Guitar (7-string)" to "Western",
    "Bass" to "Western", "Bass (5-string)" to "Western",
    "Ukulele" to "Western", "Ukulele (baritone)" to "Western",
    "Violin" to "Western", "Viola" to "Western", "Cello" to "Western",
    "Mandolin" to "Western",
    "Irish bouzouki" to "European folk", "Mandola" to "European folk",
    "Banjo (5-string, open G)" to "European folk",
    "Mountain dulcimer (DAd)" to "European folk",
    "Balalaika (prima)" to "European folk",
    "Bouzouki (Greek, 4-course)" to "European folk",
    "Charango (Andean)" to "Latin America", "Cuatro venezolano" to "Latin America",
    "Cavaquinho (Brazil)" to "Latin America", "Guitarrón mexicano" to "Latin America",
    "Tiple colombiano (nominal)" to "Latin America",
    "Ronroco (nominal)" to "Latin America", "Tres cubano (G–C–E)" to "Latin America",
    "Oud (Arabic)" to "Middle East & Persia", "Bağlama / saz (D–G–A)" to "Middle East & Persia",
    "Qanun (diatonic base)" to "Middle East & Persia",
    "Santur (Persian, D)" to "Middle East & Persia",
    "Buzuq" to "Middle East & Persia", "Tar (Persian)" to "Middle East & Persia",
    "Setar (Persian)" to "Middle East & Persia",
    "Guembri / sintir" to "North & West Africa",
    "Kalimba (17-key C)" to "North & West Africa",
    "Kora (silaba tuning)" to "North & West Africa",
    "Tanpura (drone, Sa = C)" to "South Asia", "Sitar (Sa = C)" to "South Asia",
    "Veena (Saraswati)" to "South Asia", "Sarangi (Sa–Pa–Sa)" to "South Asia",
    "Ektara (drone)" to "South Asia",
    "Dombra (Kazakh)" to "Central Asia", "Dutar (quint tuning)" to "Central Asia",
    "Pipa (Chinese)" to "East Asia", "Guzheng (D pentatonic)" to "East Asia",
    "Koto (hirajōshi)" to "East Asia", "Shamisen (honchōshi)" to "East Asia",
    "Gayageum (Korean)" to "East Asia",
    "Đàn tranh (C pentatonic)" to "Southeast Asia",
    "Lap steel (C6)" to "Other folk & historical",
    "Lyre (D major diatonic)" to "Other folk & historical",
    "Celtic harp (C major)" to "Other folk & historical"
)

private val REGION_ORDER = listOf(
    "Western", "European folk", "Latin America", "Middle East & Persia",
    "North & West Africa", "South Asia", "Central Asia", "East Asia",
    "Southeast Asia", "Other folk & historical", CustomPresetStore.CUSTOM_REGION
)

fun Instrument.regionName(): String = INSTRUMENT_REGIONS[name] ?: region

private fun Instrument.summary(): String {
    if (strings.isEmpty()) return "no strings"
    val midis = strings.map { it.note.midi }
    return "${strings.size} strings · ${Note(midis.min()).label()} – ${Note(midis.max()).label()}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentPickerSheet(
    instruments: List<Instrument>,
    selected: Instrument,
    canExport: Boolean,
    onSelect: (Instrument) -> Unit,
    onCreateNew: () -> Unit,
    onDetect: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onEdit: (Instrument) -> Unit,
    onDelete: (Instrument) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    var query by remember { mutableStateOf("") }
    val grouped = remember(instruments, query) {
        val filtered = if (query.isBlank()) instruments
        else instruments.filter { it.name.contains(query, ignoreCase = true) }
        filtered.groupBy { it.regionName() }
            .toList()
            .sortedBy { (region, _) ->
                REGION_ORDER.indexOf(region).let { if (it < 0) Int.MAX_VALUE else it }
            }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("Choose instrument", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Button(onClick = onDetect, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = palette.chipActive)) {
                Text("🔍 Auto-detect by sound")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Search ${instruments.size} instruments…") },
                singleLine = true, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onCreateNew, modifier = Modifier.fillMaxWidth()) {
                Text("＋ Create custom preset")
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onImport, modifier = Modifier.weight(1f)) { Text("⬇ Import presets") }
                TextButton(onClick = onExport, enabled = canExport, modifier = Modifier.weight(1f)) { Text("⬆ Export presets") }
            }
            Spacer(Modifier.height(6.dp))

            if (grouped.isEmpty()) {
                Text("No matches for “$query”.", color = palette.textSub,
                    modifier = Modifier.padding(vertical = 24.dp))
            } else {
                LazyColumn(Modifier.fillMaxWidth().height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    grouped.forEach { (region, list) ->
                        item(key = "header-$region") {
                            Text(region.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                color = if (region == CustomPresetStore.CUSTOM_REGION) palette.primary else palette.textSub,
                                modifier = Modifier.padding(top = 14.dp, bottom = 4.dp))
                        }
                        items(list, key = { "${region}:${it.name}" }) { inst ->
                            val isSelected = inst.name == selected.name
                            val isCustom = inst.regionName() == CustomPresetStore.CUSTOM_REGION
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) palette.chipActive else Color.Transparent)
                                    .clickable { onSelect(inst) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(inst.name, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    Text(inst.summary(), fontSize = 11.sp, color = palette.textSub)
                                }
                                if (isSelected) {
                                    Text("✓", color = palette.primary, fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                }
                                if (isCustom) {
                                    TextButton(onClick = { onEdit(inst) }) { Text("Edit", fontSize = 12.sp) }
                                    TextButton(onClick = { onDelete(inst) }) { Text("Delete", fontSize = 12.sp, color = palette.danger) }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
