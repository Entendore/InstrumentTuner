package com.example.instrumenttuner.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun SpectrumView(
    spectrum: FloatArray,
    sampleRate: Int,
    fundamental: Float,
    modifier: Modifier = Modifier
) {
    val palette = LocalTunerPalette.current
    val labelPaint = remember { Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER } }

    Canvas(modifier) {
        val fMin = 55f
        val fMax = 2200f
        val lnMin = ln(fMin.toDouble())
        val lnMax = ln(fMax.toDouble())
        fun xOf(f: Float): Float = ((ln(f.toDouble()) - lnMin) / (lnMax - lnMin)).toFloat() * size.width

        labelPaint.textSize = size.height * 0.10f
        labelPaint.color = palette.textSub.toArgb()
        for (octave in 2..7) {
            val fC = 32.703f * 2f.pow(octave - 1)
            if (fC < fMin || fC > fMax) continue
            val x = xOf(fC)
            drawLine(palette.bandFaint, Offset(x, 0f), Offset(x, size.height), 1f)
            drawContext.canvas.nativeCanvas.drawText("C$octave", x, size.height - labelPaint.textSize * 0.3f, labelPaint)
        }

        if (spectrum.isNotEmpty()) {
            val binHz = sampleRate / (spectrum.size * 2f)
            val bars = 96
            val plotBottom = size.height
            val plotTop = labelPaint.textSize * 1.4f
            for (b in 0 until bars) {
                val f0 = exp(lnMin + (lnMax - lnMin) * (b.toDouble() / bars)).toFloat()
                val f1 = exp(lnMin + (lnMax - lnMin) * ((b + 1).toDouble() / bars)).toFloat()
                val k0 = (f0 / binHz).toInt().coerceIn(0, spectrum.size - 1)
                val k1 = (f1 / binHz).toInt().coerceIn(k0, spectrum.size - 1)
                var db = -80f
                for (k in k0..k1) if (spectrum[k] > db) db = spectrum[k]
                val h = ((db + 70f) / 70f).coerceIn(0f, 1f) * (plotBottom - plotTop)
                if (h <= 0f) continue
                val x0 = size.width * b / bars
                val x1 = size.width * (b + 1) / bars
                drawRect(
                    color = palette.spectrumBar.copy(alpha = 0.45f + 0.55f * (h / (plotBottom - plotTop))),
                    topLeft = Offset(x0 + 0.5f, plotBottom - h),
                    size = Size((x1 - x0 - 1f).coerceAtLeast(1f), h)
                )
            }
        }

        if (fundamental in fMin..fMax) {
            val x = xOf(fundamental)
            drawLine(palette.primary, Offset(x, 0f), Offset(x, size.height), 2f)
            drawCircle(palette.primary, 4f, Offset(x, 6f))
        }
    }
}
