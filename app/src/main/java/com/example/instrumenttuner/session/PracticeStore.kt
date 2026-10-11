package com.example.instrumenttuner.session

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PracticeRound(val seconds: Long, val avgCents: Double)
data class PracticeSession(val instrument: String, val timeMs: Long, val rounds: List<PracticeRound>)

class PracticeStore(context: Context) {
    private val prefs = context.getSharedPreferences("practice_sessions", Context.MODE_PRIVATE)

    fun load(): List<PracticeSession> {
        val json = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.getJSONObject(i)
                val roundsArr = o.optJSONArray("rounds") ?: return@mapNotNull null
                val rounds = (0 until roundsArr.length()).map { k ->
                    val r = roundsArr.getJSONArray(k)
                    PracticeRound(r.getLong(0), r.getDouble(1))
                }
                PracticeSession(o.optString("instrument", ""), o.optLong("time"), rounds)
            }.filter { it.instrument.isNotEmpty() && it.rounds.isNotEmpty() }
        } catch (_: Exception) { emptyList() }
    }

    fun save(sessions: List<PracticeSession>): List<PracticeSession> {
        val capped = if (sessions.size > MAX_SESSIONS) sessions.takeLast(MAX_SESSIONS) else sessions
        val arr = JSONArray()
        capped.forEach { s ->
            val rounds = JSONArray()
            s.rounds.forEach { r -> rounds.put(JSONArray().put(r.seconds).put(r.avgCents)) }
            arr.put(JSONObject().put("instrument", s.instrument).put("time", s.timeMs).put("rounds", rounds))
        }
        prefs.edit().putString(KEY_SESSIONS, arr.toString()).apply()
        return capped
    }

    private companion object {
        const val KEY_SESSIONS = "sessions"
        const val MAX_SESSIONS = 100
    }
}
