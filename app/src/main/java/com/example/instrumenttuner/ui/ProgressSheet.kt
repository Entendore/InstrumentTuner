package com.example.instrumenttuner.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.session.PracticeSession
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressSheet(
    instrumentName: String,
    sessions: List<PracticeSession>,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val bests = remember(sessions) { sessions.map { s -> s.rounds.minOf { it.seconds }.toFloat() } }
    val overallBest = bests.minOrNull()
    val avgDev = remember(sessions) {
        val all = sessions.flatMap { it.rounds }.map { it.avgCents }
        if (all.isEmpty()) null else all.average()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp).verticalScroll(rememberScrollState())) {
            Text("Progress — $instrumentName", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (sessions.isEmpty()) {
                Text("No practice sessions yet for this instrument.\nRun ⏱ Practice rounds to start building your history.",
                    color = palette.textSub, fontSize = 14.sp)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox("Sessions", "${sessions.size}", palette)
                    StatBox("Best", "${overallBest?.toInt()}s", palette)
                    StatBox("Avg dev", "±${"%.1f".format(avgDev ?: 0.0)}¢", palette)
                }
                if (bests.size >= 2 && bests.last() < bests.first()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Improved ${bests.first().toInt() - bests.last().toInt()}s since your first session 🎉",
                        fontSize = 13.sp, color = palette.primary)
                }

                Spacer(Modifier.height(14.dp))
                Text("Best-round time trend (chronological)", fontSize = 13.sp, color = palette.textSub)
                Spacer(Modifier.height(6.dp))
                if (bests.size >= 2) {
                    TrendGraph(bests, Modifier.fillMaxWidth().height(130.dp))
                } else {
                    Text("Complete at least two sessions to see the trend.", fontSize = 12.sp, color = palette.dim)
                }

                Spacer(Modifier.height(14.dp))
                Text("Recent sessions", fontSize = 13.sp, color = palette.textSub)
                Spacer(Modifier.height(6.dp))
                sessions.takeLast(5).reversed().forEach { s ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(dateFormat.format(s.timeMs), fontSize = 12.sp,
                            color = palette.textSub, modifier = Modifier.width(64.dp))
                        Text("${s.rounds.size} rounds · best ${s.rounds.minOf { it.seconds }}s · avg ±${"%.1f".format(s.rounds.map { it.avgCents }.average())}¢",
                            fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onDismiss) { Text("Done") }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, palette: TunerPalette) {
    Column(Modifier.clip(RoundedCornerShape(12.dp)).background(palette.chip)
        .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = palette.primary)
        Text(label, fontSize = 11.sp, color = palette.textSub)
    }
}

@Composable
private fun TrendGraph(values: List<Float>, modifier: Modifier = Modifier) {
    val palette = LocalTunerPalette.current
    val labelPaint = remember { Paint().apply { isAntiAlias = true; textSize = 26f } }
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).coerceAtLeast(1f)
        val padL = labelPaint.textSize * 1.7f
        val w = size.width - padL
        val stepX = w / (values.size - 1)
        fun y(v: Float) = size.height * 0.85f - ((v - min) / range) * size.height * 0.7f

        drawLine(palette.bandFaint, Offset(padL, size.height * 0.85f), Offset(size.width, size.height * 0.85f), 1f)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = padL + i * stepX
            if (i == 0) path.moveTo(x, y(v)) else path.lineTo(x, y(v))
        }
        drawPath(path, palette.primary, style = Stroke(3f))
        values.forEachIndexed { i, v ->
            drawCircle(palette.primary, 4f, Offset(padL + i * stepX, y(v)))
        }
        labelPaint.color = palette.textSub.toArgb()
        drawContext.canvas.nativeCanvas.drawText("${max.toInt()}s", 0f, y(max) + 8f, labelPaint)
        drawContext.canvas.nativeCanvas.drawText("${min.toInt()}s", 0f, y(min) + 8f, labelPaint)
    }
}
