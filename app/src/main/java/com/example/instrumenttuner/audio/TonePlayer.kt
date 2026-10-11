package com.example.instrumenttuner.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin

class TonePlayer(private val context: Context, private val sampleRate: Int = 44_100) {

    private val playing = AtomicBoolean(false)
    private var worker: Thread? = null
    private var focusRequest: AudioFocusRequest? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop()
    }

    val isPlaying: Boolean get() = playing.get()

    fun play(frequency: Float, volume: Float = 0.30f) = play(listOf(frequency), null, volume)

    fun play(frequencies: List<Float>, amplitudes: List<Float>? = null, volume: Float = 0.30f) {
        stop()
        val freqs = frequencies.filter { it > 0f }
        if (freqs.isEmpty()) return
        val amps = amplitudes?.takeIf { it.size == freqs.size } ?: List(freqs.size) { 1f }
        requestFocus()
        playing.set(true)
        worker = Thread({
            val chunk = 2_048
            val buffer = ShortArray(chunk)
            val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val track = AudioTrack(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                AudioFormat.Builder().setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT).build(),
                maxOf(minBuf, chunk * 2), AudioTrack.MODE_STREAM, 0)
            if (track.state == AudioTrack.STATE_INITIALIZED) {
                track.play()
                val steps = DoubleArray(freqs.size) { 2.0 * PI * freqs[it] / sampleRate }
                val phases = DoubleArray(freqs.size)
                val norm = volume / amps.sum()
                val fadeIn = (sampleRate * 0.05).toInt()
                val fadeOut = (sampleRate * 0.15).toInt()
                var written = 0
                var fadeRemaining = -1
                while (true) {
                    if (fadeRemaining < 0 && !playing.get()) fadeRemaining = fadeOut
                    val fading = fadeRemaining >= 0
                    for (i in buffer.indices) {
                        val envelope = when {
                            fading -> (fadeRemaining.toFloat() / fadeOut).coerceAtLeast(0f)
                            written < fadeIn -> written.toFloat() / fadeIn
                            else -> 1f
                        }
                        var v = 0.0
                        for (k in steps.indices) {
                            v += amps[k] * sin(phases[k])
                            phases[k] += steps[k]
                            if (phases[k] >= 2 * PI) phases[k] -= 2 * PI
                        }
                        val s = (v * envelope * norm * Short.MAX_VALUE).toInt()
                        buffer[i] = s.coerceIn(-32768, 32767).toShort()
                        written++
                        if (fading) fadeRemaining--
                    }
                    track.write(buffer, 0, buffer.size)
                    if (fading && fadeRemaining <= 0) break
                }
                track.stop()
            }
            track.release()
            playing.set(false)
            abandonFocus()
        }, "tone-player").also { it.start() }
    }

    fun stop() {
        playing.set(false)
        worker?.join(800)
        worker = null
        abandonFocus()
    }

    private fun requestFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setOnAudioFocusChangeListener(focusListener).build()
            focusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusListener)
        }
    }
}
