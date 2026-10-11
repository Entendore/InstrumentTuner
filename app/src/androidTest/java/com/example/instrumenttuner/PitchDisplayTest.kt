package com.example.instrumenttuner

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.rule.GrantPermissionRule
import com.example.instrumenttuner.audio.FakePitchSource
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PitchDisplayTest {

    // Injected BEFORE the activity launches (property init order matters)
    private val fakePitch = FakePitchSource().also { MainActivity.testPitchSource = it }

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun emits_440Hz_shows_A4_in_tune_and_engine_started() {
        fakePitch.emit(440f)
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("In tune ✓").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("A4").assertExists()
        assertTrue("engine should have been started by lifecycle", fakePitch.started)
    }

    @Test
    fun emits_450Hz_shows_sharp_deviation() {
        fakePitch.emit(450f)
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("¢ sharp", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("A4").assertExists()
    }

    @Test
    fun emits_260Hz_shows_C4_flat() {
        fakePitch.emit(260f)
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("¢ flat", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("C4").assertExists()
    }

    @Test
    fun silence_shows_listening_state() {
        fakePitch.emitSilence()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Play a note…").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
