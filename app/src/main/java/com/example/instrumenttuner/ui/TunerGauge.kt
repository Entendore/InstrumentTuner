package com.example.instrumenttuner.ui

import android.graphics.Paint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TunerGauge(
    cents: Float,
    hasSignal: Boolean,
    inTuneTolerance: Float = 5f,
    centsRange: Float = 50f,
    modifier: Modifier = Modifier
) {
    val palette = LocalTunerPalette.current
    val degPerCent = 90f / centsRange
    val needle by animateFloatAsState(
        targetValue = if (hasSignal) cents.coerceIn(-centsRange, centsRange) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "needle"
    )
    val labelPaint = remember { Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER } }
    val sidePaint = remember { Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER } }

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height * 0.93f
        val radius = min(size.width / 2.15f, size.height * 0.85f)
        val topLeft = Offset(cx - radius, cy - radius)
        val arcSize = Size(radius * 2f, radius * 2f)
        val band = radius * 0.12f

        val needleColor = when {
            !hasSignal -> palette.dim
            abs(cents) <= inTuneTolerance -> palette.primary
            abs(cents) <= inTuneTolerance * 3f -> palette.warn
            else -> palette.danger
        }

        if (hasSignal && abs(cents) <= inTuneTolerance) {
            drawCircle(palette.primary.copy(alpha = 0.07f), radius * 0.9f, Offset(cx, cy))
        }

        drawArc(palette.chip, 180f, 180f, false, topLeft, arcSize, Stroke(width = band, cap = StrokeCap.Round))
        val inner = inTuneTolerance * degPerCent
        val outer = inTuneTolerance * 3f * degPerCent
        drawArc(palette.warn.copy(alpha = 0.85f), 270f - outer, outer - inner, false, topLeft, arcSize, Stroke(band))
        drawArc(palette.warn.copy(alpha = 0.85f), 270f + inner, outer - inner, false, topLeft, arcSize, Stroke(band))
        drawArc(palette.primary, 270f - inner, inner * 2f, false, topLeft, arcSize, Stroke(band))

        val step = if (centsRange <= 10f) 2f else 10f
        val majorStep = if (centsRange <= 10f) 5 else 25
        labelPaint.textSize = radius * 0.10f
        labelPaint.color = palette.textSub.toArgb()
        var c = -centsRange
        while (c <= centsRange) {
            val ci = c.roundToInt()
            val major = ci % majorStep == 0
            val a = ((270f + c * degPerCent) * PI / 180f).toFloat()
            val dir = Offset(cos(a), sin(a))
            drawLine(
                color = if (major) palette.textPrimary else palette.dim,
                start = Offset(cx + dir.x * radius * 0.90f, cy + dir.y * radius * 0.90f),
                end = Offset(cx + dir.x * radius * (if (major) 0.74f else 0.82f),
                    cy + dir.y * radius * (if (major) 0.74f else 0.82f)),
                strokeWidth = radius * (if (major) 0.014f else 0.008f),
                cap = StrokeCap.Round
            )
            if (major) {
                drawContext.canvas.nativeCanvas.drawText(
                    abs(ci).toString(),
                    cx + dir.x * radius * 0.60f,
                    cy + dir.y * radius * 0.60f + labelPaint.textSize * 0.35f,
                    labelPaint
                )
            }
            c += step
        }

        sidePaint.textSize = radius * 0.09f
        sidePaint.color = palette.dim.toArgb()
        drawContext.canvas.nativeCanvas.drawText("♭ FLAT", cx - radius * 0.88f, cy - radius * 0.10f, sidePaint)
        drawContext.canvas.nativeCanvas.drawText("SHARP ♯", cx + radius * 0.88f, cy - radius * 0.10f, sidePaint)

        val na = ((270f + needle * degPerCent) * PI / 180f).toFloat()
        val nd = Offset(cos(na), sin(na))
        val alpha = if (hasSignal) 1f else 0.5f
        drawLine(
            color = needleColor.copy(alpha = alpha),
            start = Offset(cx - nd.x * radius * 0.12f, cy - nd.y * radius * 0.12f),
            end = Offset(cx + nd.x * radius * 0.96f, cy + nd.y * radius * 0.96f),
            strokeWidth = radius * 0.02f,
            cap = StrokeCap.Round
        )
        drawCircle(palette.needleHub, radius * 0.055f, Offset(cx, cy))
        drawCircle(needleColor.copy(alpha = alpha), radius * 0.03f, Offset(cx, cy))
    }
}
