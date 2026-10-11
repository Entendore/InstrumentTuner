package com.example.instrumenttuner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.instrumenttuner.audio.AudioEngine
import com.example.instrumenttuner.audio.Fft
import com.example.instrumenttuner.audio.PitchSource
import com.example.instrumenttuner.audio.TonePlayer
import com.example.instrumenttuner.music.INSTRUMENTS
import com.example.instrumenttuner.music.Instrument
import com.example.instrumenttuner.music.InstrumentString
import com.example.instrumenttuner.music.Note
import com.example.instrumenttuner.music.Temperament
import com.example.instrumenttuner.presets.CustomPresetData
import com.example.instrumenttuner.presets.CustomPresetStore
import com.example.instrumenttuner.presets.PresetTransfer
import com.example.instrumenttuner.presets.StringOffsetStore
import com.example.instrumenttuner.session.LineupTemplateStore
import com.example.instrumenttuner.session.PracticeRound
import com.example.instrumenttuner.session.PracticeSession
import com.example.instrumenttuner.session.PracticeStore
import com.example.instrumenttuner.ui.BeatTrainerSheet
import com.example.instrumenttuner.ui.CalibrateSheet
import com.example.instrumenttuner.ui.DRONE_STACKS
import com.example.instrumenttuner.ui.DetectSheet
import com.example.instrumenttuner.ui.DroneSheet
import com.example.instrumenttuner.ui.InstrumentPickerSheet
import com.example.instrumenttuner.ui.IntonationSheet
import com.example.instrumenttuner.ui.LineupSheet
import com.example.instrumenttuner.ui.OffsetsSheet
import com.example.instrumenttuner.ui.PresetEditorSheet
import com.example.instrumenttuner.ui.PracticeHistorySheet
import com.example.instrumenttuner.ui.ProgressSheet
import com.example.instrumenttuner.ui.PitchSparkline
import com.example.instrumenttuner.ui.ScaleTrainerSheet
import com.example.instrumenttuner.ui.SettingsSheet
import com.example.instrumenttuner.ui.SpectrumView
import com.example.instrumenttuner.ui.StrobeView
import com.example.instrumenttuner.ui.TunerGauge
import com.example.instrumenttuner.ui.TunerSettings
import com.example.instrumenttuner.ui.TunerTheme
import com.example.instrumenttuner.ui.TuningEntry
import com.example.instrumenttuner.ui.TuningLogSheet
import com.example.instrumenttuner.ui.rankCandidates
import com.example.instrumenttuner.ui.themeByName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private val audioEngine: PitchSource = testPitchSource ?: AudioEngine()
    private val tonePlayer by lazy { TonePlayer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TunerApp(audioEngine, tonePlayer) }
    }

    companion object {
        /** Test hook: inject a deterministic source before the activity launches. */
        var testPitchSource: PitchSource? = null
    }
}

private const val PREFS = "tuner_prefs"
private const val KEY_A4 = "a4"
private const val KEY_INSTRUMENT = "instrument"
private const val KEY_CAPO = "capo"
private const val KEY_USE_FLATS = "use_flats"
private const val KEY_TOLERANCE = "tolerance"
private const val KEY_HAPTICS = "haptics"
private const val KEY_AUTO_ADVANCE = "auto_advance"
private const val KEY_SENSITIVITY = "sensitivity"
private const val KEY_TEMPERAMENT = "temperament"
private const val KEY_TONIC = "tonic_pc"
private const val KEY_PRACTICE_ROUNDS = "practice_rounds"
private const val KEY_THEME = "theme"
private const val KEY_STRETCH = "stretch"
private const val KEY_TRANSPOSITION = "transposition"
private const val KEY_TONE_TIMBRE = "tone_timbre"
private const val KEY_DISPLAY_MODE = "display_mode"
private const val KEY_FINE = "fine_zoom"
private const val KEY_STAGE = "stage_mode"
private const val KEY_DRONE_ROOT = "drone_root"
private const val KEY_DRONE_STACK = "drone_stack"
private const val KEY_LINEUP = "lineup"
private const val KEY_BEAT_BEST = "beat_best"
private const val KEY_PRACTICE_PREFIX = "practice_best_"
private const val KEY_A4_PREFIX = "a4_by_"
private const val KEY_A4_LOG = "a4_log"
private const val SIGNAL_HOLD_MS = 1_200L
private const val NOTE_HOLD_CENTS = 58f
private const val AUTO_ADVANCE_FRAMES = 32
private const val SPARK_WINDOW_MS = 4_000L

private data class Reading(val displayNote: Note, val cents: Float, val heardNote: Note, val frequency: Float)

private sealed interface EditorMode {
    data object Closed : EditorMode
    data object New : EditorMode
    data class Edit(val preset: CustomPresetData) : EditorMode
}

private fun fmtDuration(s: Long): String = if (s >= 60) "${s / 60}m ${s % 60}s" else "${s}s"

private fun tick(v: Vibrator) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(30, 100))
    else @Suppress("DEPRECATION") v.vibrate(30)
}

@Composable
private fun TunerApp(engine: PitchSource, tones: TonePlayer) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    var settings by remember {
        mutableStateOf(TunerSettings(
            useFlats = prefs.getBoolean(KEY_USE_FLATS, false),
            tolerance = prefs.getFloat(KEY_TOLERANCE, 5f),
            haptics = prefs.getBoolean(KEY_HAPTICS, true),
            autoAdvance = prefs.getBoolean(KEY_AUTO_ADVANCE, true),
            sensitivity = prefs.getFloat(KEY_SENSITIVITY, 4f),
            temperament = prefs.getString(KEY_TEMPERAMENT, Temperament.EQUAL.label) ?: Temperament.EQUAL.label,
            tonicPc = prefs.getInt(KEY_TONIC, 0),
            practiceRounds = prefs.getInt(KEY_PRACTICE_ROUNDS, 3),
            theme = prefs.getString(KEY_THEME, "Midnight") ?: "Midnight",
            stretch = prefs.getFloat(KEY_STRETCH, 0f),
            transposition = prefs.getInt(KEY_TRANSPOSITION, 0),
            toneTimbre = prefs.getString(KEY_TONE_TIMBRE, "Pure") ?: "Pure"
        ))
    }
    fun saveSettings(s: TunerSettings) {
        settings = s
        prefs.edit()
            .putBoolean(KEY_USE_FLATS, s.useFlats).putFloat(KEY_TOLERANCE, s.tolerance)
            .putBoolean(KEY_HAPTICS, s.haptics).putBoolean(KEY_AUTO_ADVANCE, s.autoAdvance)
            .putFloat(KEY_SENSITIVITY, s.sensitivity).putString(KEY_TEMPERAMENT, s.temperament)
            .putInt(KEY_TONIC, s.tonicPc).putInt(KEY_PRACTICE_ROUNDS, s.practiceRounds)
            .putString(KEY_THEME, s.theme).putFloat(KEY_STRETCH, s.stretch)
            .putInt(KEY_TRANSPOSITION, s.transposition).putString(KEY_TONE_TIMBRE, s.toneTimbre)
            .apply()
    }
    val palette = themeByName(settings.theme)

    // ── Permission ──────────────────────────────────────────────────
    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var permissionDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
        permissionDenied = !granted
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) { if (!hasMicPermission) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }

    if (!hasMicPermission) {
        TunerTheme(palette) {
            PermissionPrompt(
                wasDenied = permissionDenied,
                onRetry = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    })
                }
            )
        }
        return
    }

    // ── State ───────────────────────────────────────────────────────
    val view = LocalView.current
    val sample by engine.pitch.collectAsState()
    val spectrum by engine.spectrum.collectAsState()
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val presetStore = remember { CustomPresetStore(context) }
    var customData by remember { mutableStateOf(presetStore.loadData()) }
    val customPresets = remember(customData) { customData.map { it.toInstrument() } }
    val allInstruments = remember(customPresets) { INSTRUMENTS + customPresets }
    val logStore = remember { com.example.instrumenttuner.ui.TuningLogStore(context) }
    var tuningLog by remember { mutableStateOf(logStore.load()) }
    val practiceStore = remember { PracticeStore(context) }
    var sessionHistory by remember { mutableStateOf(practiceStore.load()) }
    val lineupTemplateStore = remember { LineupTemplateStore(context) }
    var lineupTemplates by remember { mutableStateOf(lineupTemplateStore.load()) }
    val offsetStore = remember { StringOffsetStore(context) }
    var stringOffsets by remember { mutableStateOf(offsetStore.load()) }

    var instrument by remember {
        val savedName = prefs.getString(KEY_INSTRUMENT, null)
        mutableStateOf(allInstruments.firstOrNull { it.name == savedName } ?: INSTRUMENTS[0])
    }
    var a4 by remember { mutableStateOf(prefs.getFloat(KEY_A4, 440f)) }
    var capo by remember { mutableStateOf(prefs.getInt(KEY_CAPO, 0).coerceIn(0, 7)) }
    var locked by remember { mutableStateOf<InstrumentString?>(null) }
    var toneNote by remember { mutableStateOf<Note?>(null) }
    var displayMode by remember { mutableStateOf(prefs.getInt(KEY_DISPLAY_MODE, 0)) }
    var fineZoom by remember { mutableStateOf(prefs.getBoolean(KEY_FINE, false)) }
    var stageMode by remember { mutableStateOf(prefs.getBoolean(KEY_STAGE, false)) }
    var showSettings by remember { mutableStateOf(false) }
    var showLog by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var editorMode by remember { mutableStateOf<EditorMode>(EditorMode.Closed) }
    var pendingDelete by remember { mutableStateOf<Instrument?>(null) }
    var tunedMidis by remember { mutableStateOf(setOf<Int>()) }
    var droneRoot by remember { mutableStateOf(prefs.getInt(KEY_DRONE_ROOT, 48)) }
    var droneStack by remember { mutableStateOf(prefs.getInt(KEY_DRONE_STACK, 2)) }
    var droneActive by remember { mutableStateOf(false) }
    var showDrone by remember { mutableStateOf(false) }
    var transferMessage by remember { mutableStateOf<String?>(null) }
    var detectMode by remember { mutableStateOf(false) }
    var detectedNotes by remember { mutableStateOf(listOf<Note>()) }
    var practiceActive by remember { mutableStateOf(false) }
    var practiceStart by remember { mutableStateOf(0L) }
    var practiceResult by remember { mutableStateOf<String?>(null) }
    var practiceRound by remember { mutableStateOf(1) }
    var practiceRoundResults by remember { mutableStateOf(listOf<Pair<Long, Double>>()) }
    var showPracticeHistory by remember { mutableStateOf(false) }
    var lineup by remember {
        mutableStateOf(try {
            JSONArray(prefs.getString(KEY_LINEUP, "[]")!!).let { arr -> (0 until arr.length()).map { arr.getString(it) } }
        } catch (_: Exception) { emptyList() })
    }
    var lineupActive by remember { mutableStateOf(false) }
    var lineupStart by remember { mutableStateOf(0L) }
    var lineupLegStart by remember { mutableStateOf(0L) }
    var lineupTimes by remember { mutableStateOf(listOf<Pair<String, Long>>()) }
    var lineupResult by remember { mutableStateOf<String?>(null) }
    var showLineup by remember { mutableStateOf(false) }
    var templateName by remember { mutableStateOf("") }
    var showCalibrate by remember { mutableStateOf(false) }
    var showIntonation by remember { mutableStateOf(false) }
    var showBeatTrainer by remember { mutableStateOf(false) }
    var showOffsets by remember { mutableStateOf(false) }
    var bestBeat by remember { mutableStateOf(prefs.getInt(KEY_BEAT_BEST, 0)) }
    var showScaleTrainer by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    var stringHealth by remember { mutableStateOf(mapOf<String, Float>()) }
    val scope = rememberCoroutineScope()
    var playAllJob by remember { mutableStateOf<Job?>(null) }

    val temperament = remember(settings.temperament) {
        Temperament.entries.firstOrNull { it.label == settings.temperament } ?: Temperament.EQUAL
    }

    val holder = remember {
        object {
            var lastReading: Reading? = null
            var freshAt = 0L
            var heldNote: Note? = null
            var inTuneFrames = 0
            var hapticFired = false
            var tuneHoldFrames = 0
            var advanceFrames = 0
            val spark = ArrayDeque<Pair<Long, Float>>()
            val centsWindow = ArrayDeque<Float>()
            var stabilityNote: Note? = null
            var detectNote: Note? = null
            var detectFrames = 0
            var roundDone = false
            var roundGap = 0
            var lineupWait = false
            var lineupGap = 0
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────
    fun setStageBrightness(on: Boolean) {
        val w = (context as? android.app.Activity)?.window ?: return
        val lp = w.attributes
        lp.screenBrightness = if (on) 1f else -1f
        w.attributes = lp
    }
    fun saveLineup(l: List<String>) {
        prefs.edit().putString(KEY_LINEUP, JSONArray(l).toString()).apply()
    }
    fun logA4(value: Float, source: String) {
        val arr = try { JSONArray(prefs.getString(KEY_A4_LOG, "[]")!!) } catch (_: Exception) { JSONArray() }
        arr.put(JSONObject().put("time", System.currentTimeMillis()).put("a4", value.toDouble()).put("source", source))
        while (arr.length() > 100) arr.remove(0)
        prefs.edit().putString(KEY_A4_LOG, arr.toString()).apply()
    }
    fun loadA4Log(): List<Triple<Long, Float, String>> = try {
        val arr = JSONArray(prefs.getString(KEY_A4_LOG, "[]")!!)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Triple(o.getLong("time"), o.getDouble("a4").toFloat(), o.optString("source", ""))
        }
    } catch (_: Exception) { emptyList() }

    fun selectInstrument(inst: Instrument) {
        if (inst.name == instrument.name) return
        instrument = inst
        locked = null
        tones.stop()
        toneNote = null
        tunedMidis = setOf()
        practiceActive = false
        practiceResult = null
        stringHealth = mapOf()
        holder.tuneHoldFrames = 0
        holder.advanceFrames = 0
        val mem = prefs.getFloat(KEY_A4_PREFIX + inst.name, -1f)
        if (mem > 0f) a4 = mem
        prefs.edit().putString(KEY_INSTRUMENT, inst.name).apply()
        TunerWidgetProvider.refresh(context)
    }
    fun selectInstrumentByName(name: String) {
        allInstruments.firstOrNull { it.name == name }?.let { selectInstrument(it) }
    }
    fun setA4(value: Float, source: String) {
        tones.stop(); toneNote = null; droneActive = false
        a4 = value
        prefs.edit().putFloat(KEY_A4, a4).putFloat(KEY_A4_PREFIX + instrument.name, a4).apply()
        logA4(a4, source)
    }

    fun stretchOffset(note: Note): Float = settings.stretch * (note.midi - 69f) / 12f
    fun stringOffset(note: Note): Float = stringOffsets[instrument.name]?.get(note.midi) ?: 0f
    fun temperedFreq(note: Note): Float {
        val off = temperament.offsetOf(note.midi, settings.tonicPc) + stretchOffset(note) + stringOffset(note)
        return note.frequency(a4) * 2f.pow(off / 1200f)
    }
    fun centsTo(note: Note, freq: Float): Float =
        Note.centsBetween(freq, note, a4) - temperament.offsetOf(note.midi, settings.tonicPc) -
            stretchOffset(note) - stringOffset(note)

    fun playFreq(f: Float) {
        val (freqs, amps) = when (settings.toneTimbre) {
            "Hollow" -> listOf(f, f * 2f) to listOf(1f, 0.5f)
            "Rich" -> listOf(f, f * 2f, f * 3f) to listOf(1f, 0.45f, 0.25f)
            else -> listOf(f) to listOf(1f)
        }
        tones.play(freqs, amps)
    }
    fun playTone(note: Note) = playFreq(temperedFreq(note))
    fun startTone(target: Note) {
        droneActive = false
        playAllJob?.cancel(); playAllJob = null
        toneNote = target
        playTone(target)
    }
    fun droneFrequencies(): List<Float> {
        val root = temperedFreq(Note(droneRoot))
        return DRONE_STACKS[droneStack.coerceIn(0, DRONE_STACKS.size - 1)].ratios.map { root * it }
    }

    fun csv(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
    fun buildCsv(): String = buildString {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        appendLine("TUNING LOG")
        appendLine("time,instrument,string,note,cents")
        tuningLog.forEach { e ->
            appendLine("${fmt.format(e.timeMs)},${csv(e.instrumentName)},${csv(e.stringLabel)},${csv(e.noteLabel)},${"%.1f".format(e.cents)}")
        }
        appendLine()
        appendLine("PRACTICE SESSIONS")
        appendLine("time,instrument,rounds,best_seconds,avg_cents,round_times")
        sessionHistory.forEach { s ->
            val best = s.rounds.minOf { it.seconds }
            val avg = s.rounds.map { it.avgCents }.average()
            appendLine("${fmt.format(s.timeMs)},${csv(s.instrument)},${s.rounds.size},$best,${"%.1f".format(avg)},\"${s.rounds.joinToString("|") { it.seconds.toString() }}\"")
        }
        appendLine()
        appendLine("A4 CALIBRATIONS")
        appendLine("time,source,a4_hz")
        loadA4Log().forEach { (t, v, src) -> appendLine("${fmt.format(t)},$src,${"%.1f".format(v)}") }
        appendLine()
        appendLine("STRING HEALTH (current session)")
        appendLine("instrument,string,octave_error_cents")
        stringHealth.forEach { (s, v) -> appendLine("${csv(instrument.name)},${csv(s)},${"%.1f".format(v)}") }
    }
    fun shareText(text: String, chooserTitle: String) {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
        }, chooserTitle))
    }
    fun shareLog() {
        val fmt = SimpleDateFormat("MMM d · HH:mm", Locale.getDefault())
        val text = buildString {
            appendLine("Tuning log · A4=${a4.roundToInt()} · ${temperament.label}")
            tuningLog.forEach { e ->
                appendLine("${fmt.format(e.timeMs)}  ${e.instrumentName}${if (e.stringLabel.isNotEmpty()) " · ${e.stringLabel}" else ""} · ${e.noteLabel}: ${"%+.1f".format(e.cents)}¢")
            }
        }
        shareText(text, "Share tuning log")
    }
    fun buildCertificate(title: String, lines: List<String>): String = buildString {
        val fmt = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
        appendLine("🎵 TUNING CERTIFICATE")
        appendLine("════════════════════════")
        appendLine(title)
        appendLine(fmt.format(java.util.Date()))
        appendLine("Instrument: ${instrument.name}${if (capo > 0) " · capo $capo" else ""}")
        appendLine("A4 = ${a4.roundToInt()} Hz · ${temperament.label}")
        lines.forEach { appendLine("  • $it") }
        appendLine("— verified with Instrument Tuner")
    }

    fun startPractice() {
        lineupActive = false
        lineupResult = null
        practiceRound = 1
        practiceRoundResults = listOf()
        holder.roundDone = false
        holder.roundGap = 0
        tunedMidis = setOf()
        holder.tuneHoldFrames = 0
        holder.advanceFrames = 0
        practiceResult = "Round 1/${settings.practiceRounds} — go!"
        practiceStart = System.currentTimeMillis()
        practiceActive = true
    }
    fun startLineup() {
        if (lineup.isEmpty()) return
        practiceActive = false
        practiceResult = null
        lineupTimes = listOf()
        lineupResult = null
        holder.lineupWait = false
        holder.lineupGap = 0
        tunedMidis = setOf()
        holder.tuneHoldFrames = 0
        holder.advanceFrames = 0
        selectInstrumentByName(lineup.first())
        lineupStart = System.currentTimeMillis()
        lineupLegStart = System.currentTimeMillis()
        lineupActive = true
    }

    // ── File launchers ──────────────────────────────────────────────
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(PresetTransfer.encode(customData).toByteArray()) } != null
            }.isSuccess
            transferMessage = if (ok) "Exported ${customData.size} presets ✓" else "Export failed"
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val json = runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } }.getOrNull()
            val parsed = json?.let { PresetTransfer.decode(it) }
            if (parsed.isNullOrEmpty()) transferMessage = "Couldn't read presets from that file."
            else {
                val taken = allInstruments.map { it.name.lowercase() }.toSet()
                val merged = parsed.map { p ->
                    var name = p.name; var n = 2
                    while (name.lowercase() in taken) { name = "${p.name} ($n)"; n++ }
                    if (name == p.name) p else p.copy(name = name)
                }
                customData = customData + merged
                presetStore.saveData(customData)
                transferMessage = "Imported ${merged.size} presets ✓"
            }
        }
    }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            val ok = runCatching { context.contentResolver.openOutputStream(uri)?.use { it.write(buildCsv().toByteArray()) } != null }.isSuccess
            transferMessage = if (ok) "CSV exported ✓" else "CSV export failed"
        }
    }

    // ── Lifecycle effects ───────────────────────────────────────────
    DisposableEffect(lifecycleOwner) {
        view.keepScreenOn = true
        engine.start()
        if (stageMode) setStageBrightness(true)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> engine.start()
                Lifecycle.Event.ON_PAUSE -> { tones.stop(); droneActive = false; setStageBrightness(false); engine.stop() }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            view.keepScreenOn = false
            tones.stop()
            engine.stop()
        }
    }
    LaunchedEffect(settings.sensitivity) { engine.sensitivity = settings.sensitivity }
    LaunchedEffect(Unit) { if (stageMode) setStageBrightness(true) }

    // ── Per-frame computations ───────────────────────────────────────
    val now = System.currentTimeMillis()
    val lockedTarget: Note? = locked?.let { Note(it.note.midi + capo) }
    val fresh: Reading? = if (sample.hasPitch) {
        if (lockedTarget != null) {
            Reading(displayNote = lockedTarget, cents = centsTo(lockedTarget, sample.frequency),
                heardNote = Note.nearest(sample.frequency, a4).first, frequency = sample.frequency)
        } else {
            val nearest = Note.nearest(sample.frequency, a4)
            val held = holder.heldNote
            val display = if (held != null && abs(Note.centsBetween(sample.frequency, held, a4)) < NOTE_HOLD_CENTS) held else nearest.first
            holder.heldNote = display
            Reading(display, centsTo(display, sample.frequency), display, sample.frequency)
        }
    } else null

    if (fresh != null) { holder.lastReading = fresh; holder.freshAt = now }
    val reading = fresh ?: holder.lastReading.takeIf { now - holder.freshAt < SIGNAL_HOLD_MS }
    val isStale = fresh == null && reading != null
    val inTune = fresh != null && abs(fresh.cents) <= settings.tolerance

    if (fresh != null) {
        holder.spark.addLast(now to fresh.cents)
        while (holder.spark.isNotEmpty() && now - holder.spark.first().first > SPARK_WINDOW_MS) holder.spark.removeFirst()
        if (holder.stabilityNote != fresh.displayNote) { holder.centsWindow.clear(); holder.stabilityNote = fresh.displayNote }
        holder.centsWindow.addLast(fresh.cents)
        if (holder.centsWindow.size > 21) holder.centsWindow.removeFirst()
    } else if (reading == null) holder.spark.clear()

    val stability: Float? = if (holder.centsWindow.size >= 8) {
        val mean = holder.centsWindow.average()
        sqrt(holder.centsWindow.map { val d = it - mean; d * d }.average()).toFloat()
    } else null

    if (settings.haptics && fresh != null && abs(fresh.cents) <= settings.tolerance) {
        if (!holder.hapticFired) {
            holder.inTuneFrames++
            if (holder.inTuneFrames >= 4) { holder.hapticFired = true; vibrator?.let { if (it.hasVibrator()) tick(it) } }
        }
    } else { holder.inTuneFrames = 0; holder.hapticFired = false }

    if (detectMode && fresh != null) {
        if (holder.detectNote == fresh.heardNote) {
            holder.detectFrames++
            if (holder.detectFrames == 8 && detectedNotes.none { it.midi == fresh.heardNote.midi }) {
                detectedNotes = detectedNotes + fresh.heardNote
                if (settings.haptics) vibrator?.let { if (it.hasVibrator()) tick(it) }
            }
        } else { holder.detectNote = fresh.heardNote; holder.detectFrames = 1 }
    } else { holder.detectNote = null; holder.detectFrames = 0 }

    if (fresh != null && locked != null && abs(fresh.cents) <= settings.tolerance) {
        holder.tuneHoldFrames++
        if (holder.tuneHoldFrames == 5) {
            val shifted = Note(locked.note.midi + capo)
            tunedMidis = tunedMidis + shifted.midi
            tuningLog = logStore.save(tuningLog + TuningEntry(instrument.name, locked.label, shifted.label(settings.useFlats), fresh.cents, now))
            Fft.octaveDeviation(spectrum, 44_100, fresh.frequency)?.let { hd ->
                stringHealth = stringHealth + (locked.label to hd)
            }
        }
    } else holder.tuneHoldFrames = 0

    val allTuned = instrument.strings.isNotEmpty() &&
        instrument.strings.all { Note(it.note.midi + capo).midi in tunedMidis }

    if (settings.autoAdvance && !allTuned && fresh != null && locked != null && abs(fresh.cents) <= settings.tolerance) {
        holder.advanceFrames++
        if (holder.advanceFrames >= AUTO_ADVANCE_FRAMES) {
            holder.advanceFrames = 0
            val idx = instrument.strings.indexOf(locked)
            if (idx >= 0) {
                val size = instrument.strings.size
                var ni = (idx + 1) % size
                var hops = 0
                while (Note(instrument.strings[ni].note.midi + capo).midi in tunedMidis && hops < size) { ni = (ni + 1) % size; hops++ }
                if (hops < size) {
                    val next = instrument.strings[ni]
                    locked = next
                    startTone(Note(next.note.midi + capo))
                }
            }
        }
    } else holder.advanceFrames = 0
    val advancing = settings.autoAdvance && locked != null && fresh != null &&
        abs(fresh.cents) <= settings.tolerance && holder.advanceFrames in 8 until AUTO_ADVANCE_FRAMES

    if (practiceActive && allTuned && !holder.roundDone) {
        holder.roundDone = true
        val secs = (now - practiceStart) / 1000
        val runEntries = tuningLog.filter { it.timeMs >= practiceStart }
        val avg = if (runEntries.isEmpty()) 0.0 else runEntries.map { abs(it.cents) }.average()
        practiceRoundResults = practiceRoundResults + (secs to avg)
        if (practiceRound < settings.practiceRounds) {
            practiceResult = "Round $practiceRound: ${fmtDuration(secs)} · avg ±${"%.1f".format(avg)}¢ — next…"
            holder.roundGap = 35
        } else {
            practiceActive = false
            val times = practiceRoundResults.map { it.first }
            val best = times.min()
            sessionHistory = practiceStore.save(sessionHistory + PracticeSession(instrument.name, now, practiceRoundResults.map { (s, a) -> PracticeRound(s, a) }))
            val bestKey = KEY_PRACTICE_PREFIX + instrument.name
            val prevBest = prefs.getFloat(bestKey, 0f)
            if (prevBest <= 0f || best < prevBest) {
                prefs.edit().putFloat(bestKey, best.toFloat()).apply()
                TunerWidgetProvider.refresh(context)
            }
            val trend = if (times.size >= 2 && times.last() < times.first()) " · improved ${times.first() - times.last()}s 🎉" else ""
            practiceResult = "Session done · best ${fmtDuration(best)}$trend"
        }
    }
    if (practiceActive && holder.roundGap > 0) {
        holder.roundGap--
        if (holder.roundGap == 0) {
            practiceRound++
            tunedMidis = setOf()
            holder.tuneHoldFrames = 0; holder.advanceFrames = 0; holder.roundDone = false
            practiceStart = System.currentTimeMillis()
            practiceResult = "Round $practiceRound/${settings.practiceRounds} — go!"
        }
    }

    val lineupPos = if (lineupActive) lineup.indexOf(instrument.name) else -1
    val lineupNext = if (lineupPos >= 0) lineup.getOrNull(lineupPos + 1) else null
    if (lineupActive && lineupPos >= 0 && allTuned && !holder.lineupWait) {
        holder.lineupWait = true
        holder.lineupGap = 40
        lineupTimes = lineupTimes + (instrument.name to ((now - lineupLegStart) / 1000))
        if (settings.haptics) vibrator?.let { if (it.hasVibrator()) tick(it) }
    }
    if (lineupActive && holder.lineupWait && holder.lineupGap > 0) {
        holder.lineupGap--
        if (holder.lineupGap == 0) {
            holder.lineupWait = false
            val nextName = lineup.getOrNull(lineupPos + 1)
            if (nextName != null) {
                selectInstrumentByName(nextName)
                lineupLegStart = System.currentTimeMillis()
            } else {
                lineupActive = false
                val total = (System.currentTimeMillis() - lineupStart) / 1000
                lineupResult = "Lineup complete: ${lineupTimes.size} instruments in ${fmtDuration(total)}"
            }
        }
    }

    val nearestString: InstrumentString? = if (locked == null && fresh != null) {
        instrument.strings.map { it to abs(centsTo(Note(it.note.midi + capo), fresh.frequency)) }
            .minByOrNull { it.second }?.takeIf { it.second <= 50f }?.first
    } else null

    val lastByString = remember(tuningLog, instrument) {
        tuningLog.filter { it.instrumentName == instrument.name && it.stringLabel.isNotEmpty() }
            .groupBy { it.stringLabel }.mapValues { (_, list) -> list.maxByOrNull { it.timeMs } }
    }
    val detectCandidates = remember(detectedNotes, customPresets) { rankCandidates(detectedNotes, allInstruments) }
    val a4HistoryLines = remember {
        val fmt = SimpleDateFormat("MMM d · HH:mm", Locale.getDefault())
        loadA4Log().map { (t, v, src) -> "${fmt.format(t)} — $src → ${"%.1f".format(v)} Hz" }
    }

    // ── Stage mode (full-screen, returns early) ─────────────────────
    if (stageMode) {
        TunerTheme(palette) {
            Surface(
                Modifier.fillMaxSize().clickable {
                    stageMode = false
                    prefs.edit().putBoolean(KEY_STAGE, false).apply()
                    setStageBrightness(false)
                },
                color = palette.background
            ) {
                Column(Modifier.fillMaxSize().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val stageNote = reading?.let {
                        val n = if (settings.transposition != 0) Note(it.displayNote.midi + settings.transposition) else it.displayNote
                        n.label(settings.useFlats)
                    } ?: "--"
                    Text(stageNote, fontSize = 110.sp, fontWeight = FontWeight.Bold,
                        color = if (inTune) palette.primary else palette.textPrimary)
                    Text(
                        when {
                            reading == null -> "Play a note…"
                            inTune -> "In tune ✓"
                            reading.cents < 0 -> "♭ ${"%.1f".format(abs(reading.cents))}¢"
                            else -> "♯ ${"%.1f".format(abs(reading.cents))}¢"
                        },
                        fontSize = 34.sp, color = if (inTune) palette.primary else palette.warn
                    )
                    Spacer(Modifier.height(12.dp))
                    StrobeView(cents = fresh?.cents ?: 0f, hasSignal = fresh != null,
                        inTuneTolerance = settings.tolerance,
                        modifier = Modifier.weight(1f).fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("tap anywhere to exit", fontSize = 12.sp, color = palette.dim)
                }
            }
        }
        return
    }

    // ── Main UI ─────────────────────────────────────────────────────
    TunerTheme(palette) {
        Surface(Modifier.fillMaxSize(), color = palette.background) {
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                // Header
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Instrument Tuner", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = palette.textPrimary)
                        Text("Chromatic · YIN detection", fontSize = 12.sp, color = palette.textSub)
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { setA4((a4 - 1f).coerceIn(415f, 466f), "manual") }) { Text("−", fontSize = 24.sp) }
                        Text("A4 = ${a4.roundToInt()} Hz", fontSize = 13.sp, color = palette.textSub)
                        TextButton(onClick = { setA4((a4 + 1f).coerceIn(415f, 466f), "manual") }) { Text("+", fontSize = 24.sp) }
                        TextButton(onClick = { showIntonation = true }) { Text("📐", fontSize = 16.sp) }
                        TextButton(onClick = { showProgress = true }) { Text("📈", fontSize = 16.sp) }
                        TextButton(onClick = { showLog = true }, modifier = Modifier.testTag("btn_log")) { Text("🗒", fontSize = 16.sp) }
                        TextButton(onClick = { showSettings = true }, modifier = Modifier.testTag("btn_settings")) { Text("⚙", fontSize = 16.sp) }
                        TextButton(onClick = { showLineup = true }) { Text("🧭", fontSize = 16.sp) }
                        TextButton(onClick = { showPracticeHistory = true }) { Text("🏆", fontSize = 16.sp) }
                    }
                }

                // Display area
                when (displayMode) {
                    1 -> {
                        Spacer(Modifier.height(12.dp))
                        StrobeView(cents = fresh?.cents ?: 0f, hasSignal = fresh != null,
                            inTuneTolerance = settings.tolerance,
                            modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(18.dp)).background(palette.panel))
                        Spacer(Modifier.height(6.dp))
                    }
                    2 -> {
                        Spacer(Modifier.height(12.dp))
                        SpectrumView(spectrum = spectrum, sampleRate = 44_100,
                            fundamental = fresh?.frequency ?: -1f,
                            modifier = Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(18.dp)).background(palette.panel))
                        Spacer(Modifier.height(6.dp))
                    }
                    3 -> {
                        TunerGauge(cents = fresh?.cents ?: 0f, hasSignal = fresh != null,
                            inTuneTolerance = settings.tolerance,
                            centsRange = if (fineZoom) 10f else 50f,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.65f))
                        Spacer(Modifier.height(8.dp))
                        StrobeView(cents = fresh?.cents ?: 0f, hasSignal = fresh != null,
                            inTuneTolerance = settings.tolerance,
                            modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(18.dp)).background(palette.panel))
                    }
                    else -> TunerGauge(cents = fresh?.cents ?: 0f, hasSignal = fresh != null,
                        inTuneTolerance = settings.tolerance,
                        centsRange = if (fineZoom) 10f else 50f,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1.65f))
                }

                // Reading
                val shownNote = reading?.let {
                    if (settings.transposition != 0) Note(it.displayNote.midi + settings.transposition) else it.displayNote
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (reading == null) {
                        Text("--", fontSize = 68.sp, fontWeight = FontWeight.Bold, color = palette.dim)
                        Text("Play a note…", fontSize = 14.sp, color = palette.textSub)
                    } else {
                        val dim = if (isStale) 0.45f else 1f
                        Text(
                            shownNote!!.label(settings.useFlats) + if (settings.transposition != 0) " (written)" else "",
                            fontSize = 68.sp, fontWeight = FontWeight.Bold,
                            color = (if (inTune) palette.primary else palette.textPrimary).copy(alpha = dim)
                        )
                        Text(
                            when {
                                inTune -> "In tune ✓"
                                reading.cents < 0 -> "♭ ${"%.1f".format(abs(reading.cents))} ¢ flat"
                                else -> "♯ ${"%.1f".format(abs(reading.cents))} ¢ sharp"
                            },
                            fontSize = 17.sp, color = (if (inTune) palette.primary else palette.warn).copy(alpha = dim)
                        )
                        Text(buildString {
                            append("%.1f Hz".format(reading.frequency))
                            if (!isStale && stability != null) append(" · ±${"%.1f".format(stability)}¢")
                            if (locked != null && reading.heardNote != reading.displayNote) {
                                val heardShown = if (settings.transposition != 0) Note(reading.heardNote.midi + settings.transposition) else reading.heardNote
                                append(" · hearing ${heardShown.label(settings.useFlats)}")
                            }
                            if (!isStale) append(" · ${"%.2f".format(abs(reading.frequency - temperedFreq(reading.displayNote)))} Hz beat")
                        }, fontSize = 13.sp, color = palette.textSub.copy(alpha = dim))
                        if (advancing) { Spacer(Modifier.height(4.dp)); Text("steady… next string soon", fontSize = 11.sp, color = palette.warn) }
                        if (allTuned) { Spacer(Modifier.height(4.dp)); Text("All strings tuned 🎉", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = palette.primary) }
                        if (practiceActive) {
                            Spacer(Modifier.height(4.dp))
                            Text("⏱ Round $practiceRound/${settings.practiceRounds} · ${(now - practiceStart) / 1000}s", fontSize = 13.sp, color = palette.warn)
                        } else practiceResult?.let { r ->
                            Spacer(Modifier.height(4.dp))
                            Text(r, fontSize = 13.sp, color = palette.primary, fontWeight = FontWeight.Bold)
                            if (practiceRoundResults.isNotEmpty()) {
                                TextButton(onClick = {
                                    shareText(buildCertificate("Practice session complete",
                                        practiceRoundResults.mapIndexed { i, (secs, avg) -> "Round ${i + 1} — ${fmtDuration(secs)} · avg ±${"%.1f".format(avg)}¢" }),
                                        "Share certificate")
                                }) { Text("📜 Share certificate") }
                            }
                        }
                        if (lineupActive) {
                            Spacer(Modifier.height(4.dp))
                            val msg = when {
                                holder.lineupWait && lineupNext != null -> "✓ done — moving to $lineupNext…"
                                holder.lineupWait -> "✓ done — finishing lineup…"
                                else -> "🧭 Lineup ${lineupPos + 1}/${lineup.size}"
                            }
                            Text(msg, fontSize = 13.sp, color = palette.warn)
                        } else lineupResult?.let { r ->
                            Spacer(Modifier.height(4.dp))
                            Text(r, fontSize = 13.sp, color = palette.primary, fontWeight = FontWeight.Bold)
                            if (lineupTimes.isNotEmpty()) {
                                TextButton(onClick = {
                                    shareText(buildCertificate("Multi-instrument lineup complete",
                                        lineupTimes.map { (name, secs) -> "$name — ${fmtDuration(secs)}" } + "Total — ${fmtDuration(lineupTimes.sumOf { it.second })}"),
                                        "Share certificate")
                                }) { Text("📜 Share certificate") }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                PitchSparkline(points = holder.spark.toList(), tolerance = settings.tolerance,
                    modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(10.dp)).background(palette.panel))
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("mic", fontSize = 11.sp, color = palette.textSub)
                    Spacer(Modifier.width(8.dp))
                    BoxWithConstraints(Modifier.weight(1f).height(10.dp)) {
                        LinearProgressIndicator(
                            progress = { min(1f, sample.level / 0.12f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).align(Alignment.Center),
                            color = palette.primary, trackColor = palette.track)
                        Spacer(Modifier.offset(x = maxWidth * (sample.gate / 0.12f).coerceIn(0f, 1f))
                            .width(2.dp).height(10.dp).background(palette.warn))
                    }
                }

                Spacer(Modifier.weight(1f))

                // Instrument selector
                val bestSecs = prefs.getFloat(KEY_PRACTICE_PREFIX + instrument.name, 0f)
                val a4Memorized = prefs.contains(KEY_A4_PREFIX + instrument.name)
                Row(
                    Modifier.fillMaxWidth().testTag("btn_instrument_selector")
                        .clip(RoundedCornerShape(12.dp)).background(palette.card)
                        .clickable { showPicker = true }.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Instrument", fontSize = 10.sp, color = palette.textSub)
                        Text(instrument.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, color = palette.textPrimary)
                    }
                    Text(buildString {
                        append("${instrument.strings.size} strings")
                        if (bestSecs > 0f) append(" · best ${bestSecs.toInt()}s")
                        if (a4Memorized) append(" · A4 memorized")
                    }, fontSize = 12.sp, color = palette.textSub)
                    Spacer(Modifier.width(8.dp))
                    Text("▾", fontSize = 14.sp, color = palette.primary)
                }
                Spacer(Modifier.height(8.dp))

                // Capo
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Capo", fontSize = 12.sp, color = palette.textSub)
                    Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0..7) {
                            FilterChip(selected = capo == i,
                                onClick = { if (capo != i) { capo = i; tones.stop(); toneNote = null; holder.tuneHoldFrames = 0; holder.advanceFrames = 0; prefs.edit().putInt(KEY_CAPO, i).apply() } },
                                label = { Text("$i", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Mode + tool chips
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = locked == null, onClick = { locked = null; tones.stop(); toneNote = null },
                        label = { Text("Auto") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary))
                    FilterChip(selected = displayMode == 1, onClick = { displayMode = if (displayMode == 1) 0 else 1; prefs.edit().putInt(KEY_DISPLAY_MODE, displayMode).apply() },
                        label = { Text("Strobe") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.warn.copy(alpha = 0.18f), selectedLabelColor = palette.warn))
                    FilterChip(selected = displayMode == 2, onClick = { displayMode = if (displayMode == 2) 0 else 2; prefs.edit().putInt(KEY_DISPLAY_MODE, displayMode).apply() },
                        label = { Text("Spectrum") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.warn.copy(alpha = 0.18f), selectedLabelColor = palette.warn))
                    FilterChip(selected = displayMode == 3, onClick = { displayMode = if (displayMode == 3) 0 else 3; prefs.edit().putInt(KEY_DISPLAY_MODE, displayMode).apply() },
                        label = { Text("Combo") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.warn.copy(alpha = 0.18f), selectedLabelColor = palette.warn))
                    FilterChip(selected = fineZoom, onClick = { fineZoom = !fineZoom; prefs.edit().putBoolean(KEY_FINE, fineZoom).apply() },
                        label = { Text(if (fineZoom) "Fine ±10¢" else "Fine") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary))
                    FilterChip(selected = playAllJob?.isActive == true,
                        onClick = {
                            if (playAllJob?.isActive == true) { playAllJob?.cancel(); playAllJob = null; tones.stop(); toneNote = null }
                            else {
                                locked = null
                                playAllJob = scope.launch {
                                    droneActive = false
                                    instrument.strings.forEach { s ->
                                        val target = Note(s.note.midi + capo)
                                        toneNote = target
                                        playTone(target)
                                        delay(1400)
                                    }
                                    toneNote = null
                                    playAllJob = null
                                }
                            }
                        },
                        label = { Text(if (playAllJob?.isActive == true) "■ Stop" else "▶ Play all") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary))
                    FilterChip(selected = droneActive && tones.isPlaying, onClick = { showDrone = true },
                        label = { Text(if (droneActive && tones.isPlaying) "Drone ●" else "Drone") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.warn.copy(alpha = 0.18f), selectedLabelColor = palette.warn))
                    FilterChip(selected = stageMode,
                        onClick = { stageMode = !stageMode; prefs.edit().putBoolean(KEY_STAGE, stageMode).apply(); setStageBrightness(stageMode) },
                        label = { Text("🎤 Stage") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.warn.copy(alpha = 0.18f), selectedLabelColor = palette.warn))
                    FilterChip(selected = practiceActive,
                        onClick = { if (practiceActive) practiceActive = false else startPractice() },
                        label = { Text(if (practiceActive) "⏱ Stop" else "⏱ Practice") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = palette.chipActive, selectedLabelColor = palette.primary))
                }
                Spacer(Modifier.height(10.dp))

                // String buttons
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    instrument.strings.forEach { s ->
                        val target = Note(s.note.midi + capo)
                        val isSelected = locked == s
                        val toneOn = toneNote == target
                        val wasTuned = target.midi in tunedMidis
                        val isNearest = s == nearestString
                        val lastEntry = lastByString[s.label]
                        Button(
                            onClick = {
                                playAllJob?.cancel(); playAllJob = null
                                locked = s
                                if (toneOn) { tones.stop(); toneNote = null } else startTone(target)
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = when {
                                isSelected -> BorderStroke(1.5.dp, palette.primary)
                                isNearest -> BorderStroke(1.dp, palette.textSub)
                                wasTuned -> BorderStroke(1.dp, palette.primary.copy(alpha = 0.4f))
                                else -> null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isSelected && inTune -> palette.primary
                                    isSelected -> palette.chipActive
                                    wasTuned -> palette.chipActive
                                    else -> palette.card
                                },
                                contentColor = if (isSelected && inTune) palette.background else palette.textPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(target.label(settings.useFlats), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                if (s.label.isNotEmpty()) Text(s.label, fontSize = 10.sp,
                                    color = (if (isSelected && inTune) palette.background else palette.textPrimary).copy(alpha = 0.65f))
                                val statusColor = when {
                                    isSelected && inTune -> palette.background
                                    toneOn -> palette.warn
                                    wasTuned -> palette.primary
                                    else -> palette.textSub
                                }
                                Text(when {
                                    toneOn -> "● playing"
                                    wasTuned -> "✓ tuned"
                                    lastEntry != null -> "last ${"%+.1f".format(lastEntry.cents)}¢"
                                    else -> "%.1f Hz".format(temperedFreq(target))
                                }, fontSize = 9.sp, color = statusColor)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }

        // ── Sheets & dialogs ────────────────────────────────────────
        if (showPicker) {
            InstrumentPickerSheet(
                instruments = allInstruments, selected = instrument,
                canExport = customData.isNotEmpty(),
                onSelect = { inst -> selectInstrument(inst); showPicker = false; lineupActive = false; lineupResult = null },
                onCreateNew = { showPicker = false; editorMode = EditorMode.New },
                onDetect = { showPicker = false; detectedNotes = listOf(); detectMode = true },
                onExport = { exportLauncher.launch("instrument-tuner-presets.json") },
                onImport = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                onEdit = { inst -> customData.firstOrNull { it.name == inst.name }?.let { showPicker = false; editorMode = EditorMode.Edit(it) } },
                onDelete = { inst -> pendingDelete = inst },
                onDismiss = { showPicker = false }
            )
        }

        when (val mode = editorMode) {
            EditorMode.Closed -> {}
            EditorMode.New -> PresetEditorSheet(
                initial = null, takenNames = allInstruments.map { it.name }.toSet(),
                onSave = { newName, midis, labels ->
                    customData = customData + CustomPresetData(newName, midis, labels)
                    presetStore.saveData(customData)
                    editorMode = EditorMode.Closed
                    selectInstrument(customData.last().toInstrument())
                },
                onDismiss = { editorMode = EditorMode.Closed }
            )
            is EditorMode.Edit -> PresetEditorSheet(
                initial = mode.preset, takenNames = allInstruments.map { it.name }.toSet(),
                onSave = { newName, midis, labels ->
                    val updated = CustomPresetData(newName, midis, labels)
                    customData = customData.map { if (it.name == mode.preset.name) updated else it }
                    presetStore.saveData(customData)
                    editorMode = EditorMode.Closed
                    if (instrument.name == mode.preset.name) selectInstrument(updated.toInstrument())
                },
                onDismiss = { editorMode = EditorMode.Closed }
            )
        }

        pendingDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Delete “${target.name}”?") },
                text = { Text("This custom preset will be removed permanently.") },
                confirmButton = {
                    TextButton(onClick = {
                        customData = customData.filterNot { it.name == target.name }
                        presetStore.saveData(customData)
                        if (instrument.name == target.name) selectInstrument(INSTRUMENTS[0])
                        pendingDelete = null
                    }) { Text("Delete", color = palette.danger) }
                },
                dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
            )
        }

        if (showDrone) {
            DroneSheet(
                rootMidi = droneRoot, stackIndex = droneStack,
                isPlaying = droneActive && tones.isPlaying,
                subtitle = droneFrequencies().joinToString(" · ") { "%.1f Hz".format(it) },
                onRoot = { midi ->
                    droneRoot = midi
                    prefs.edit().putInt(KEY_DRONE_ROOT, midi).apply()
                    if (droneActive && tones.isPlaying) {
                        val root = temperedFreq(Note(midi))
                        tones.play(DRONE_STACKS[droneStack].ratios.map { root * it }, null, 0.22f)
                    }
                },
                onStack = { i ->
                    droneStack = i
                    prefs.edit().putInt(KEY_DRONE_STACK, i).apply()
                    if (droneActive && tones.isPlaying) tones.play(droneFrequencies(), null, 0.22f)
                },
                onToggle = {
                    if (droneActive && tones.isPlaying) { tones.stop(); droneActive = false }
                    else {
                        playAllJob?.cancel(); playAllJob = null
                        toneNote = null
                        tones.play(droneFrequencies(), null, 0.22f)
                        droneActive = true
                    }
                },
                onDismiss = { showDrone = false }
            )
        }

        if (detectMode) {
            DetectSheet(
                collected = detectedNotes, candidates = detectCandidates,
                useFlats = settings.useFlats,
                onUse = { inst -> detectMode = false; selectInstrument(inst); lineupActive = false },
                onClear = { detectedNotes = listOf(); holder.detectNote = null; holder.detectFrames = 0 },
                onDismiss = { detectMode = false }
            )
        }

        if (showLineup) {
            LineupSheet(
                lineup = lineup, all = allInstruments, active = lineupActive, currentName = instrument.name,
                templates = lineupTemplates, templateName = templateName,
                onTemplateName = { templateName = it },
                onSaveTemplate = { name ->
                    lineupTemplates = lineupTemplateStore.save(lineupTemplates.filterNot { it.name.equals(name, ignoreCase = true) } + com.example.instrumenttuner.session.LineupTemplate(name, lineup))
                    templateName = ""
                    transferMessage = "Template “$name” saved ✓"
                },
                onDeleteTemplate = { name -> lineupTemplates = lineupTemplateStore.save(lineupTemplates.filterNot { it.name == name }) },
                onUseTemplate = { t, start ->
                    val valid = t.instruments.filter { n -> allInstruments.any { it.name == n } }
                    if (valid.isEmpty()) transferMessage = "That template has no available instruments."
                    else { lineup = valid; saveLineup(lineup); if (start) { startLineup(); showLineup = false } }
                },
                onStart = { startLineup(); showLineup = false },
                onStop = { lineupActive = false; lineupResult = null },
                onAdd = { name -> lineup = lineup + name; saveLineup(lineup) },
                onRemove = { name ->
                    lineup = lineup - name; saveLineup(lineup)
                    if (lineupActive && instrument.name == name) lineupActive = false
                },
                onDismiss = { showLineup = false }
            )
        }

        if (showPracticeHistory) {
            PracticeHistorySheet(
                subtitle = instrument.name,
                sessions = sessionHistory.filter { it.instrument == instrument.name },
                onClear = { sessionHistory = practiceStore.save(sessionHistory.filterNot { it.instrument == instrument.name }) },
                onExportCsv = { csvLauncher.launch("tuner-data.csv") },
                onDismiss = { showPracticeHistory = false }
            )
        }

        if (showCalibrate) {
            CalibrateSheet(
                a4 = a4, liveFrequency = sample.frequency, hasPitch = sample.hasPitch,
                history = a4HistoryLines,
                onApply = { newA4 ->
                    setA4(newA4.coerceIn(400f, 480f), "ear")
                    showCalibrate = false
                    transferMessage = "Calibrated: A4 = ${"%.1f".format(a4)} Hz ✓"
                },
                onDismiss = { showCalibrate = false }
            )
        }

        if (showIntonation) {
            IntonationSheet(liveFrequency = sample.frequency, hasPitch = sample.hasPitch,
                spectrum = spectrum, sampleRate = 44_100,
                onDismiss = { showIntonation = false })
        }

        if (showBeatTrainer) {
            BeatTrainerSheet(tones = tones, a4 = a4, bestScore = bestBeat,
                onFinish = { s ->
                    if (s > bestBeat) { bestBeat = s; prefs.edit().putInt(KEY_BEAT_BEST, s).apply(); transferMessage = "New ear-training best: $s/10 🎉" }
                },
                onDismiss = { showBeatTrainer = false })
        }

        if (showOffsets) {
            OffsetsSheet(
                instrument = instrument, useFlats = settings.useFlats, capo = capo,
                offsets = stringOffsets[instrument.name] ?: emptyMap(),
                onChange = { midi, value ->
                    val forInst = (stringOffsets[instrument.name] ?: emptyMap()).toMutableMap()
                    if (value == 0f) forInst.remove(midi) else forInst[midi] = value
                    stringOffsets = stringOffsets + (instrument.name to forInst)
                    offsetStore.save(stringOffsets)
                    tones.stop(); toneNote = null
                },
                onReset = { stringOffsets = stringOffsets + (instrument.name to emptyMap()); offsetStore.save(stringOffsets); tones.stop(); toneNote = null },
                onDismiss = { showOffsets = false })
        }

        if (showScaleTrainer) {
            ScaleTrainerSheet(a4 = a4, onPlayFreq = { f -> playFreq(f) }, onDismiss = { showScaleTrainer = false })
        }

        if (showProgress) {
            ProgressSheet(instrumentName = instrument.name,
                sessions = sessionHistory.filter { it.instrument == instrument.name },
                onDismiss = { showProgress = false })
        }

        if (showLog) {
            TuningLogSheet(
                subtitle = "A4=${a4.roundToInt()} · ${temperament.label}",
                entries = tuningLog,
                onClear = { tuningLog = logStore.save(emptyList()) },
                onShare = { shareLog() },
                onExportCsv = { csvLauncher.launch("tuner-data.csv") },
                onDismiss = { showLog = false })
        }

        if (showSettings) {
            SettingsSheet(
                settings = settings, a4 = a4,
                onA4 = { setA4(it.coerceIn(415f, 466f), "quick") },
                onOpenCalibrate = { showSettings = false; showCalibrate = true },
                onOpenBeatTrainer = { showSettings = false; showBeatTrainer = true },
                onOpenOffsets = { showSettings = false; showOffsets = true },
                onOpenScaleTrainer = { showSettings = false; showScaleTrainer = true },
                onChange = { new ->
                    if (new.temperament != settings.temperament || new.tonicPc != settings.tonicPc ||
                        new.stretch != settings.stretch || new.toneTimbre != settings.toneTimbre) {
                        tones.stop(); toneNote = null
                    }
                    saveSettings(new)
                },
                onDismiss = { showSettings = false })
        }

        transferMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { transferMessage = null },
                text = { Text(msg) },
                confirmButton = { TextButton(onClick = { transferMessage = null }) { Text("OK") } }
            )
        }
    }
}

@Composable
private fun PermissionPrompt(wasDenied: Boolean, onRetry: () -> Unit, onOpenSettings: () -> Unit) {
    val palette = com.example.instrumenttuner.ui.LocalTunerPalette.current
    Surface(Modifier.fillMaxSize(), color = palette.background) {
        Column(Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎤", fontSize = 54.sp)
            Spacer(Modifier.height(16.dp))
            Text("Microphone access needed", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, color = palette.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text("The tuner listens through your microphone. Audio is analyzed on-device only — nothing is recorded or uploaded.",
                textAlign = TextAlign.Center, color = palette.textSub, fontSize = 14.sp)
            if (wasDenied) {
                Spacer(Modifier.height(8.dp))
                Text("If you chose “Don't allow”, enable the microphone in the app's system settings.",
                    textAlign = TextAlign.Center, color = palette.warn, fontSize = 13.sp)
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Allow microphone access") }
            if (wasDenied) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onOpenSettings) { Text("Open app settings") }
            }
        }
    }
}
