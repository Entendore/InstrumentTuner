package com.example.instrumenttuner.presets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PresetTransferTest {

    @Test
    fun encode_and_decode_roundtrip_preserves_data() {
        val original = listOf(
            CustomPresetData("My Lute", listOf(40, 45, 50), listOf("Low", "Mid", "High")),
            CustomPresetData("Drone", listOf(60), listOf("Root"))
        )
        val decoded = PresetTransfer.decode(PresetTransfer.encode(original))
        assertNotNull(decoded)
        assertEquals(2, decoded!!.size)
        assertEquals("My Lute", decoded[0].name)
        assertEquals(listOf(40, 45, 50), decoded[0].midis)
        assertEquals(listOf("Low", "Mid", "High"), decoded[0].labels)
    }

    @Test
    fun decode_handles_v1_format_without_labels() {
        val v1 = """{"format":"instrument-tuner-presets","version":1,"presets":[{"name":"Old","notes":[48,55]}]}"""
        val decoded = PresetTransfer.decode(v1)
        assertNotNull(decoded)
        assertEquals(1, decoded!!.size)
        assertEquals("Old", decoded[0].name)
        assertEquals(0, decoded[0].labels.size)
    }

    @Test
    fun decode_returns_null_for_invalid_json() {
        assertNull(PresetTransfer.decode("not json at all"))
        assertNull(PresetTransfer.decode("""{"presets":"not an array"}"""))
    }
}
