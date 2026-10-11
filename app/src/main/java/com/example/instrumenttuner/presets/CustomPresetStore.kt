package com.example.instrumenttuner.presets

import android.content.Context
import com.example.instrumenttuner.music.Instrument
import com.example.instrumenttuner.music.InstrumentString
import com.example.instrumenttuner.music.Note
import org.json.JSONArray
import org.json.JSONObject

data class CustomPresetData(val name: String, val midis: List<Int>, val labels: List<String> = emptyList()) {
    fun toInstrument(): Instrument = Instrument(
        name = name,
        strings = midis.mapIndexed { i, midi ->
            InstrumentString(label = labels.getOrNull(i)?.takeIf { it.isNotBlank() } ?: "${i + 1}", note = Note(midi))
        },
        region = CustomPresetStore.CUSTOM_REGION
    )
}

class CustomPresetStore(context: Context) {
    private val prefs = context.getSharedPreferences("custom_presets", Context.MODE_PRIVATE)

    fun loadData(): List<CustomPresetData> {
        val json = prefs.getString(KEY_PRESETS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                val name = obj.optString("name", "").trim()
                val notesArr = obj.optJSONArray("notes") ?: return@mapNotNull null
                val midis = (0 until notesArr.length()).map { notesArr.getInt(it) }
                val labelsArr = obj.optJSONArray("labels")
                val labels = if (labelsArr == null) emptyList()
                else (0 until labelsArr.length()).map { labelsArr.optString(it, "") }
                if (name.isEmpty() || midis.isEmpty()) null else CustomPresetData(name, midis, labels)
            }
        } catch (_: Exception) { emptyList() }
    }

    fun saveData(presets: List<CustomPresetData>) {
        val arr = JSONArray()
        presets.forEach { p ->
            val notes = JSONArray(); p.midis.forEach { notes.put(it) }
            val labels = JSONArray(); p.labels.forEach { labels.put(it) }
            arr.put(JSONObject().put("name", p.name).put("notes", notes).put("labels", labels))
        }
        prefs.edit().putString(KEY_PRESETS, arr.toString()).apply()
    }

    companion object {
        const val CUSTOM_REGION = "My presets"
        private const val KEY_PRESETS = "presets"
    }
}
