package com.example.c001apk.compose.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.ThemeMode
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.main.MainActivity
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiuixSettingsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun groupedSettingsCollapseAndThemeChangesApplyWithoutRestart() = withSettings { repository, scenario, activity ->
        compose.onNodeWithTag("preference-group-主题")
            .assert(hasAnyDescendant(hasText("主题设置")))
            .assert(hasAnyDescendant(hasText("应用图标")))
        val expandedHeight = compose.onNodeWithTag("collapsing-preference-header").fetchSemanticsNode().boundsInRoot.height
        capture("settings-expanded")
        compose.onNode(hasScrollAction()).performTouchInput { swipeUp() }
        val collapsedHeight = compose.onNodeWithTag("collapsing-preference-header").fetchSemanticsNode().boundsInRoot.height
        assertTrue("The large title must collapse with scrolling", collapsedHeight < expandedHeight * 0.8f)
        capture("settings-collapsed")
        compose.onNodeWithText("主题设置").performScrollTo().performClick()
        compose.onNodeWithTag("theme-settings-page").assertExists()
        capture("theme-light")
        compose.onNodeWithText("深色主题").performClick()
        clickPopupOption("总是开启")
        compose.waitUntil(5_000) { runBlocking { repository.data.first().themeMode == ThemeMode.ALWAYS_ON } }
        capture("theme-dark")
        scenario.onActivity { assertSame(activity, it) }
        runBlocking { repository.setUiStyle(UiStyle.MD3) }
        compose.onNodeWithTag("theme-settings-page").assertExists()
        compose.onNodeWithText("深色主题").assertExists()
        pressBack()
        compose.onNodeWithTag("ui-style-MD3").performScrollTo().assertIsSelected()
        scenario.onActivity { assertSame(activity, it) }
    }

    @Test
    fun nativeStyleDropdownCanReturnToMd3() = withSettings { repository, scenario, activity ->
        compose.onNodeWithTag("ui-style-MIUIX").performClick()
        clickPopupOption("MD3")
        compose.waitUntil(5_000) { runBlocking { repository.data.first().uiStyle == UiStyle.MD3 } }
        compose.onNodeWithTag("ui-style-MD3").performScrollTo().assertIsSelected()
        scenario.onActivity { assertSame(activity, it) }
    }

    private fun withSettings(block: (UserPreferencesRepository, ActivityScenario<MainActivity>, MainActivity) -> Unit) {
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            lateinit var activity: MainActivity
            scenario.onActivity { repository = it.userPreferencesRepository; activity = it }
            val original = runBlocking { repository.data.first() }
            try {
                runBlocking { repository.setUiStyle(UiStyle.MIUIX); repository.setThemeMode(ThemeMode.ALWAYS_OFF) }
                compose.waitForIdle()
                compose.onAllNodesWithText("设置")[0].performClick()
                block(repository, scenario, activity)
            } finally {
                runBlocking { repository.setUiStyle(original.uiStyle); repository.setThemeMode(original.themeMode) }
            }
        }
    }

    private fun capture(name: String) {
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            File(context.cacheDir, "lsposed-$name.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    private fun clickPopupOption(text: String) {
        // ListPopupColumn also subcomposes an unplaced copy to measure its width.
        compose.onAllNodesWithText(text)
            .filter(SemanticsMatcher("placed") { it.layoutInfo.isPlaced }).onFirst().performClick()
    }
}
