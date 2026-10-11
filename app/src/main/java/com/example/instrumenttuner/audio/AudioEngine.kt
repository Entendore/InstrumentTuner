package com.example.instrumenttuner.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

data class PitchSample(
    val frequency: Float = -1f,
    val clarity: Float = 0f,
    val level: Float = 0f,
    val gate: Float = 0.003f
) {
    val hasPitch: Boolean get() = frequency > 0f
}

class AudioEngine(
    private val sampleRate: Int = 44_100,
    private val frameSize: Int = 4_096,
    private val hop: Int = 2_048
) : PitchSource {

    private val _pitch = MutableStateFlow(PitchSample())
    override val pitch: StateFlow<PitchSample> = _pitch.asStateFlow()
    private val _spectrum = MutableStateFlow(FloatArray(0))
    override val spectrum: StateFlow<FloatArray> = _spectrum.asStateFlow()

    private val detector = YinPitchDetector(sampleRate = sampleRate, bufferSize = frameSize)
    private val history = ArrayDeque<Float>()
    private var worker: Thread? = null

    @Volatile private var running = false
    @Volatile override var sensitivity: Float = 4f
    val isRunning: Boolean get() = running

    @SuppressLint("MissingPermission")
    override fun start() {
        if (running) return
        val channel = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val minBytes = AudioRecord.getMinBufferSize(sampleRate, channel, encoding)
        if (minBytes <= 0) return
        val bufferBytes = maxOf(minBytes, frameSize * 4)

        var record = AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channel, encoding, bufferBytes)
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            record = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, encoding, bufferBytes)
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) { record.release(); return }

        running = true
        worker = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            val window = FloatArray(frameSize)
            val hopSamples = ShortArray(hop)
            val prime = ShortArray(frameSize)
            record.startRecording()
            if (record.read(prime, 0, prime.size) == prime.size) {
                for (i in prime.indices) window[i] = prime[i] / 32768f
                var noiseFloor = 0.002f
                while (running) {
                    System.arraycopy(window, hop, window, 0, frameSize - hop)
                    if (record.read(hopSamples, 0, hop) != hop) continue
                    var sumSq = 0f
                    for (i in 0 until hop) {
                        val v = hopSamples[i] / 32768f
                        window[frameSize - hop + i] = v
                        sumSq += v * v
                    }
                    val rms = sqrt(sumSq / hop)
                    val gate = (noiseFloor * sensitivity).coerceIn(MIN_GATE, MAX_GATE)
                    _spectrum.value = Fft.magnitudeDb(window)
                    if (rms < gate) {
                        noiseFloor = noiseFloor * 0.995f + rms * 0.005f
                        history.clear()
                        _pitch.value = PitchSample(level = rms, gate = gate)
                    } else {
                        val result = detector.detect(window)
                        if (result.frequency > 0f && result.clarity >= MIN_CLARITY) {
                            history.addLast(result.frequency)
                            if (history.size > MEDIAN_WINDOW) history.removeFirst()
                            _pitch.value = PitchSample(median(history), result.clarity, rms, gate)
                        } else {
                            _pitch.value = PitchSample(level = rms, gate = gate)
                        }
                    }
                }
            }
            record.stop()
            record.release()
        }, "pitch-engine").also { it.start() }
    }

    override fun stop() {
        running = false
        worker?.join(1_000)
        worker = null
        history.clear()
        _pitch.value = PitchSample()
        _spectrum.value = FloatArray(0)
    }

    private fun median(values: ArrayDeque<Float>): Float {
        val sorted = values.sorted()
        return sorted[sorted.size / 2]
    }

    private companion object {
        const val MIN_GATE = 0.003f
        const val MAX_GATE = 0.03f
        const val MIN_CLARITY = 0.80f
        const val MEDIAN_WINDOW = 5
    }
}
