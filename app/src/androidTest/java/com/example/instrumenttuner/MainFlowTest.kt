package com.example.instrumenttuner

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test

class MainFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

    @Test
    fun appLaunches_andShowsDefaultInstrument() {
        composeTestRule.onNodeWithText("Guitar").assertExists()
        composeTestRule.onNodeWithText("Instrument", substring = true).assertExists()
    }

    @Test
    fun clickingSettings_opensSettingsSheet() {
        composeTestRule.onNodeWithTag("btn_settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("Theme").assertExists()
        composeTestRule.onNodeWithText("Temperament").assertExists()
    }

    @Test
    fun clickingInstrumentSelector_opensPickerSheet() {
        composeTestRule.onNodeWithTag("btn_instrument_selector").performClick()
        composeTestRule.onNodeWithText("Choose instrument").assertExists()
        composeTestRule.onNodeWithText("WESTERN").assertExists()
    }
}
