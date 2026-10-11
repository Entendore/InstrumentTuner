package com.example.instrumenttuner.session

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class LineupTemplate(val name: String, val instruments: List<String>)

class LineupTemplateStore(context: Context) {
    private val prefs = context.getSharedPreferences("lineup_templates", Context.MODE_PRIVATE)

    fun load(): List<LineupTemplate> {
        val json = prefs.getString(KEY_TEMPLATES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val name = o.optString("name", "").trim()
                val instArr = o.optJSONArray("instruments") ?: return@mapNotNull null
                val instruments = (0 until instArr.length()).map { instArr.getString(it) }
                if (name.isEmpty() || instruments.isEmpty()) null else LineupTemplate(name, instruments)
            }
        } catch (_: Exception) { emptyList() }
    }

    fun save(templates: List<LineupTemplate>): List<LineupTemplate> {
        val arr = JSONArray()
        templates.forEach { t ->
            val inst = JSONArray(); t.instruments.forEach { inst.put(it) }
            arr.put(JSONObject().put("name", t.name).put("instruments", inst))
        }
        prefs.edit().putString(KEY_TEMPLATES, arr.toString()).apply()
        return templates
    }

    private companion object { const val KEY_TEMPLATES = "templates" }
}
