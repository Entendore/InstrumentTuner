package com.example.instrumenttuner.presets

import android.content.Context
import org.json.JSONObject

class StringOffsetStore(context: Context) {
    private val prefs = context.getSharedPreferences("string_offsets", Context.MODE_PRIVATE)

    fun load(): Map<String, Map<Int, Float>> {
        val json = prefs.getString(KEY, null) ?: return emptyMap()
        return try {
            val root = JSONObject(json)
            val out = mutableMapOf<String, Map<Int, Float>>()
            root.keys().forEach { inst ->
                val obj = root.getJSONObject(inst)
                val m = mutableMapOf<Int, Float>()
                obj.keys().forEach { k -> m[k.toInt()] = obj.getDouble(k).toFloat() }
                out[inst] = m
            }
            out
        } catch (_: Exception) { emptyMap() }
    }

    fun save(map: Map<String, Map<Int, Float>>) {
        val root = JSONObject()
        map.forEach { (inst, m) ->
            val obj = JSONObject()
            m.forEach { (midi, v) -> obj.put(midi.toString(), v.toDouble()) }
            root.put(inst, obj)
        }
        prefs.edit().putString(KEY, root.toString()).apply()
    }

    private companion object { const val KEY = "offsets" }
}
