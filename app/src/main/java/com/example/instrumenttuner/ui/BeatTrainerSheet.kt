package com.example.instrumenttuner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.instrumenttuner.audio.TonePlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

private const val TOTAL_ROUNDS = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeatTrainerSheet(
    tones: TonePlayer,
    a4: Float,
    bestScore: Int,
    onFinish: (score: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalTunerPalette.current
    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf("idle") }
    var round by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var currentOffset by remember { mutableStateOf(0f) }
    var lastCorrect by remember { mutableStateOf<Boolean?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose { job?.cancel(); tones.stop() }
    }

    fun startRound(r: Int) {
        val same = Random.nextFloat() < 0.2f
        val maxOff = 5f + r * 1.2f
        val cents = if (same) 0f
        else (3f + Random.nextFloat() * (maxOff - 3f)) * if (Random.nextBoolean()) 1f else -1f
        currentOffset = cents
        phase = "playing"
        tones.play(listOf(a4, a4 * 2f.pow(cents / 1200f)), null, 0.25f)
    }

    fun answer(choice: Int) {
        if (phase != "playing") return
        tones.stop()
        val actual = when {
            currentOffset < -0.5f -> -1
            currentOffset > 0.5f -> 1
            else -> 0
        }
        val correct = choice == actual
        if (correct) score++
        lastCorrect = correct
        phase = "feedback"
        job = scope.launch {
            delay(1000)
            val next = round + 1
            if (next >= TOTAL_ROUNDS) { phase = "done"; onFinish(score) }
            else { round = next; startRound(next) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.sheet) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
            Text("Beat ear trainer", style = MaterialTheme.typography.titleLarge)
            Text("Two tones play together — listen for the beating and decide whether the second tone is flat, identical, or sharp. Offsets grow each round (≈0.8–5 Hz of beats).",
                style = MaterialTheme.typography.bodySmall, color = palette.textSub)
            Spacer(Modifier.height(12.dp))

            Text(
                when {
                    phase == "idle" -> "Best: $bestScore/$TOTAL_ROUNDS — press Start"
                    phase == "done" -> "Final: $score/$TOTAL_ROUNDS"
                    else -> "Round ${round + 1}/$TOTAL_ROUNDS · Score $score"
                },
                fontSize = 14.sp, color = palette.textSub
            )
            Spacer(Modifier.height(8.dp))

            Text(
                when (phase) {
                    "playing" -> "Listening…"
                    "feedback" -> {
                        val described = when {
                            abs(currentOffset) <= 0.5f -> "identical"
                            currentOffset < 0 -> "♭ ${"%.1f".format(-currentOffset)}¢ flat"
                            else -> "♯ ${"%.1f".format(currentOffset)}¢ sharp"
                        }
                        (if (lastCorrect == true) "✓ Correct — " else "✗ It was ") + described
                    }
                    "done" -> if (score >= bestScore && score > 0) "New personal best 🎉" else "Session complete"
                    else -> "Hearing slow beats (1–5 Hz) is the pro tuning skill."
                },
                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = when {
                    phase == "feedback" && lastCorrect == true -> palette.primary
                    phase == "feedback" -> palette.warn
                    else -> palette.textPrimary
                }
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { answer(-1) }, enabled = phase == "playing", modifier = Modifier.weight(1f)) { Text("♭ Flat") }
                Button(onClick = { answer(0) }, enabled = phase == "playing", modifier = Modifier.weight(1f)) { Text("Same") }
                Button(onClick = { answer(1) }, enabled = phase == "playing", modifier = Modifier.weight(1f)) { Text("♯ Sharp") }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (phase == "idle" || phase == "done") {
                    Button(onClick = { round = 0; score = 0; startRound(0) }) {
                        Text(if (phase == "done") "Play again" else "Start")
                    }
                    Spacer(Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    }
}
