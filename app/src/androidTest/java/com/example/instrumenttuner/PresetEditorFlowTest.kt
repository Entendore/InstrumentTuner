package com.example.instrumenttuner

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import com.example.instrumenttuner.presets.CustomPresetStore
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PresetEditorFlowTest {

    // Clean leftovers from previous runs BEFORE the activity reads the store
    private val cleanup = run {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val store = CustomPresetStore(ctx)
        store.saveData(store.loadData().filterNot { it.name == "QA Test Trio" })
    }

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun create_custom_preset_it_becomes_selected_and_persists() {
        composeTestRule.onNodeWithTag("btn_instrument_selector").performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Choose instrument").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("＋ Create custom preset").performClick()

        composeTestRule.onNodeWithTag("input_preset_name").performTextInput("QA Test Trio")
        composeTestRule.onAllNodesWithText("D").onFirst().performClick()
        composeTestRule.onAllNodesWithText("G").onFirst().performClick()
        composeTestRule.onAllNodesWithText("A").onFirst().performClick()

        composeTestRule.onNodeWithText("Save preset").performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("QA Test Trio").fetchSemanticsNodes().isNotEmpty()
        }

        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val saved = CustomPresetStore(ctx).loadData()
        assertTrue(saved.any { it.name == "QA Test Trio" && it.midis.size == 3 })
    }
}
