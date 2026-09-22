package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CompanionScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testShowPairingQrCodeFlow() {
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        CompanionScreen()
      }
    }

    // Button should be displayed initially
    composeTestRule
      .onNodeWithTag("show_pairing_qr_button")
      .assertIsDisplayed()

    // Tap "Show Pairing QR Code"
    composeTestRule
      .onNodeWithTag("show_pairing_qr_button")
      .performClick()

    // Now QR code card and "Waiting for Parent Connection..." should be displayed
    composeTestRule
      .onNodeWithTag("qr_code_card")
      .assertIsDisplayed()

    composeTestRule
      .onNodeWithText("Waiting for Parent Connection...")
      .assertIsDisplayed()
  }
}
