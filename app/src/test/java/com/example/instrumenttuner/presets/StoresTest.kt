package com.example.instrumenttuner.presets

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.instrumenttuner.session.LineupTemplate
import com.example.instrumenttuner.session.LineupTemplateStore
import com.example.instrumenttuner.session.PracticeRound
import com.example.instrumenttuner.session.PracticeSession
import com.example.instrumenttuner.session.PracticeStore
import com.example.instrumenttuner.ui.TuningEntry
import com.example.instrumenttuner.ui.TuningLogStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StoresTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun customPresetStore_roundtrips_names_notes_and_labels() {
        val store = CustomPresetStore(context)
        store.saveData(listOf(
            CustomPresetData("DADGAD", listOf(38, 45, 50, 55, 57, 62), listOf("low", "", "", "", "", "")),
            CustomPresetData("Drone", listOf(57), emptyList())
        ))
        val loaded = CustomPresetStore(context).loadData()
        assertEquals(2, loaded.size)
        assertEquals("DADGAD", loaded[0].name)
        assertEquals(listOf(38, 45, 50, 55, 57, 62), loaded[0].midis)
        assertEquals("low", loaded[0].labels[0])
    }

    @Test
    fun toInstrument_numbers_blank_labels_and_sets_custom_region() {
        val inst = CustomPresetData("X", listOf(60, 62), listOf("", "melody")).toInstrument()
        assertEquals("1", inst.strings[0].label)
        assertEquals("melody", inst.strings[1].label)
        assertEquals(CustomPresetStore.CUSTOM_REGION, inst.region)
    }

    @Test
    fun practiceStore_caps_history_at_100_sessions() {
        val store = PracticeStore(context)
        val many = (1..120).map { PracticeSession("Guitar", it * 1000L, listOf(PracticeRound(it.toLong(), 1.0))) }
        val saved = store.save(many)
        assertEquals(100, saved.size)
        assertEquals("oldest 20 must be dropped", 21L, saved.first().rounds.first().seconds)
    }

    @Test
    fun tuningLogStore_roundtrips_and_caps_at_500() {
        val store = TuningLogStore(context)
        val entries = (1..510).map { TuningEntry("Guitar", "$it", "E2", 0.5f, it * 1000L) }
        val saved = store.save(entries)
        assertEquals(500, saved.size)
        val loaded = TuningLogStore(context).load()
        assertEquals(500, loaded.size)
        assertEquals("510", loaded.last().stringLabel)
    }

    @Test
    fun lineupTemplateStore_roundtrips_queues() {
        val store = LineupTemplateStore(context)
        store.save(listOf(LineupTemplate("Stage", listOf("Guitar", "Bass"))))
        val loaded = LineupTemplateStore(context).load()
        assertEquals(1, loaded.size)
        assertEquals(listOf("Guitar", "Bass"), loaded[0].instruments)
    }

    @Test
    fun stringOffsetStore_roundtrips_per_instrument() {
        val store = StringOffsetStore(context)
        store.save(mapOf("Guitar" to mapOf(40 to -1.5f, 45 to 0.5f)))
        val loaded = StringOffsetStore(context).load()
        assertEquals(-1.5f, loaded["Guitar"]!![40]!!, 0.001f)
        assertEquals(0.5f, loaded["Guitar"]!![45]!!, 0.001f)
    }

    @Test
    fun stores_recover_gracefully_from_corrupt_data() {
        context.getSharedPreferences("custom_presets", Context.MODE_PRIVATE)
            .edit().putString("presets", "{corrupt").apply()
        assertTrue(CustomPresetStore(context).loadData().isEmpty())

        context.getSharedPreferences("practice_sessions", Context.MODE_PRIVATE)
            .edit().putString("sessions", "not json").apply()
        assertTrue(PracticeStore(context).load().isEmpty())

        context.getSharedPreferences("tuning_log", Context.MODE_PRIVATE)
            .edit().putString("log", "[}").apply()
        assertTrue(TuningLogStore(context).load().isEmpty())
    }
}
