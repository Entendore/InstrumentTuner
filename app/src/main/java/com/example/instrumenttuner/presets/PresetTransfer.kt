package com.example.instrumenttuner.presets

import org.json.JSONArray
import org.json.JSONObject

object PresetTransfer {
    fun encode(presets: List<CustomPresetData>): String {
        val arr = JSONArray()
        presets.forEach { p ->
            val notes = JSONArray(); p.midis.forEach { notes.put(it) }
            val labels = JSONArray(); p.labels.forEach { labels.put(it) }
            arr.put(JSONObject().put("name", p.name).put("notes", notes).put("labels", labels))
        }
        return JSONObject().put("format", "instrument-tuner-presets").put("version", 2)
            .put("presets", arr).toString(2)
    }

    fun decode(json: String): List<CustomPresetData>? {
        return try {
            val root = JSONObject(json)
            val arr = root.optJSONArray("presets") ?: JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val name = o.optString("name", "").trim()
                val notesArr = o.optJSONArray("notes") ?: return@mapNotNull null
                val midis = (0 until notesArr.length()).map { notesArr.getInt(it) }.filter { it in 0..127 }
                val labelsArr = o.optJSONArray("labels")
                val labels = if (labelsArr == null) emptyList()
                else (0 until labelsArr.length()).map { labelsArr.optString(it, "") }
                if (name.isEmpty() || midis.isEmpty()) null else CustomPresetData(name, midis, labels)
            }
        } catch (_: Exception) { null }
    }
}
