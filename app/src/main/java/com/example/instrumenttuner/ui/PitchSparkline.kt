package com.example.instrumenttuner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.abs

@Composable
fun PitchSparkline(
    points: List<Pair<Long, Float>>,
    tolerance: Float,
    windowMs: Long = 4_000L,
    modifier: Modifier = Modifier
) {
    val palette = LocalTunerPalette.current
    Canvas(modifier) {
        val mid = size.height / 2f
        val half = size.height / 2f - 3f
        val bandH = (tolerance / 50f) * half
        drawRect(palette.primary.copy(alpha = 0.10f), Offset(0f, mid - bandH), Size(size.width, bandH * 2f))
        drawLine(palette.bandFaint, Offset(0f, mid), Offset(size.width, mid), 1f)
        if (points.size >= 2) {
            val now = points.last().first
            val path = Path()
            points.forEachIndexed { i, (t, c) ->
                val x = size.width - ((now - t).toFloat() / windowMs) * size.width
                val y = mid - c.coerceIn(-50f, 50f) / 50f * half
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val inTune = abs(points.last().second) <= tolerance
            val lineColor = if (inTune) palette.primary else palette.bandDim
            drawPath(path, lineColor, style = Stroke(3f))
            val lastY = mid - points.last().second.coerceIn(-50f, 50f) / 50f * half
            drawCircle(lineColor, 4f, Offset(size.width, lastY))
        }
    }
}
