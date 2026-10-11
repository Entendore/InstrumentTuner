package com.example.instrumenttuner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun StrobeView(
    cents: Float,
    hasSignal: Boolean,
    inTuneTolerance: Float = 5f,
    modifier: Modifier = Modifier
) {
    val palette = LocalTunerPalette.current
    val centsRef by rememberUpdatedState(cents)
    val signalRef by rememberUpdatedState(hasSignal)
    val density = LocalDensity.current
    val bandSpacing = with(density) { 56.dp.toPx() }
    val speedPerCent = with(density) { 3.2.dp.toPx() }
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var last = System.nanoTime()
        while (true) {
            val now = withFrameNanos { it }
            val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.1f)
            last = now
            if (signalRef) {
                phase += centsRef.coerceIn(-50f, 50f) * speedPerCent * dt
                phase %= bandSpacing
            }
        }
    }

    val inTune = hasSignal && abs(cents) <= inTuneTolerance

    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val s = bandSpacing
            val barW = s * 0.42f
            val shift = ((phase % s) + s) % s
            val color = when {
                !hasSignal -> palette.bandFaint
                inTune -> palette.primary
                else -> palette.bandDim
            }
            var x = shift - s * 2f
            while (x < size.width + s) {
                drawRoundRect(color = color, topLeft = Offset(x, size.height * 0.08f),
                    size = Size(barW, size.height * 0.84f), cornerRadius = CornerRadius(barW * 0.3f))
                x += s
            }
            val markerColor = if (inTune) palette.primary else palette.warn
            drawLine(markerColor, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height * 0.14f), strokeWidth = 3f)
            drawLine(markerColor, Offset(size.width / 2f, size.height * 0.86f), Offset(size.width / 2f, size.height), strokeWidth = 3f)
        }
        Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("♭ flat", fontSize = 11.sp, color = palette.textSub)
            Spacer(Modifier.weight(1f))
            Text("sharp ♯", fontSize = 11.sp, color = palette.textSub)
        }
    }
}
