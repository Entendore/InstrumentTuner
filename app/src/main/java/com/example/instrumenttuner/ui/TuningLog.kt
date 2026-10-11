package com.example.instrumenttuner.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class TuningEntry(
    val instrumentName: String,
    val stringLabel: String,
    val noteLabel: String,
    val cents: Float,
    val timeMs: Long
)

class TuningLogStore(context: Context) {
    private val prefs = context.getSharedPreferences("tuning_log", Context.MODE_PRIVATE)

    fun load(): List<TuningEntry> {
        val json = prefs.getString(KEY_LOG, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                TuningEntry(
                    instrumentName = o.optString("instrument", ""),
                    stringLabel = o.optString("string", ""),
                    noteLabel = o.optString("note", ""),
                    cents = o.optDouble("cents", 0.0).toFloat(),
                    timeMs = o.optLong("time", 0L)
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    fun save(entries: List<TuningEntry>): List<TuningEntry> {
        val capped = if (entries.size > MAX_ENTRIES) entries.takeLast(MAX_ENTRIES) else entries
        val arr = JSONArray()
        capped.forEach { e ->
            arr.put(JSONObject()
                .put("instrument", e.instrumentName)
                .put("string", e.stringLabel)
                .put("note", e.noteLabel)
                .put("cents", e.cents.toDouble())
                .put("time", e.timeMs))
        }
        prefs.edit().putString(KEY_LOG, arr.toString()).apply()
        return capped
    }

    private companion object {
        const val KEY_LOG = "log"
        const val MAX_ENTRIES = 500
    }
}
