package com.example.c001apk.compose.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.R
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.ThemeType
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.example.c001apk.compose.ui.main.MainActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiStyleSwitchTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun replyThemeUpdatesPaletteAndFontWithoutReplacingTheEditor() {
        val intent = Intent(context, ReplyActivity::class.java).putExtra("type", "feed")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<ReplyActivity>(intent).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            lateinit var editor: TextInputEditText
            scenario.onActivity {
                repository = it.userPreferencesRepository
                editor = it.findViewById(R.id.editText)
                editor.setText("颜色和字号变化后仍保留草稿")
            }
            val original = runBlocking { repository.data.first() }
            fun buttonColor(): Int? {
                var color: Int? = null
                scenario.onActivity {
                    color = (it.findViewById<android.view.View>(R.id.publish).background as? GradientDrawable)?.color?.defaultColor
                }
                return color
            }
            try {
                runBlocking {
                    repository.setUiStyle(UiStyle.MIUIX)
                    repository.setMaterialYou(false)
                    repository.setThemeType(ThemeType.Custom)
                    repository.setSeedColor("D04020")
                    repository.setFontScale(1f)
                }
                compose.waitUntil(5_000) { buttonColor() != null }
                compose.waitForIdle()
                val first = buttonColor()
                var firstSize = 0f
                scenario.onActivity { firstSize = editor.textSize }
                runBlocking { repository.setSeedColor("2060D0"); repository.setFontScale(1.2f) }
                compose.waitUntil(5_000) { buttonColor() != first }
                compose.waitUntil(5_000) {
                    var larger = false
                    scenario.onActivity { larger = editor.textSize > firstSize * 1.1f }
                    larger
                }
                scenario.onActivity {
                    assertSame(editor, it.findViewById(R.id.editText))
                    assertEquals("颜色和字号变化后仍保留草稿", editor.text.toString())
                }
            } finally {
                runBlocking {
                    repository.setUiStyle(original.uiStyle)
                    repository.setMaterialYou(original.materialYou)
                    repository.setThemeType(original.themeType)
                    repository.setSeedColor(original.seedColor)
                    repository.setFontScale(original.fontScale)
                }
            }
        }
    }

    @Test
    fun switchAppliesWithoutRestartAndKeepsSettingsScrollPosition() {
        ActivityScenario.launch<MainActivity>(mainIntent()).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            lateinit var originalActivity: MainActivity
            scenario.onActivity { repository = it.userPreferencesRepository; originalActivity = it }
            val original = runBlocking { repository.data.first().uiStyle }
            try {
                runBlocking { repository.setUiStyle(UiStyle.MD3) }
                compose.onAllNodesWithText("设置")[0].performClick()
                compose.onNodeWithTag("ui-style-MIUIX").performScrollTo()
                compose.onNode(hasScrollAction()).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 96f) }
                val scroll = compose.onNode(hasScrollAction()).fetchSemanticsNode()
                    .config[SemanticsProperties.VerticalScrollAxisRange].value()
                compose.onNodeWithTag("ui-style-MIUIX").performClick()
                waitForStyle(repository, UiStyle.MIUIX)
                compose.onNodeWithTag("ui-style-MIUIX").assertIsSelected()
                val after = compose.onNode(hasScrollAction()).fetchSemanticsNode()
                    .config[SemanticsProperties.VerticalScrollAxisRange].value()
                assertEquals(scroll, after, 2f)
                scenario.onActivity { assertSame(originalActivity, it); assertFalse(it.isFinishing) }

                compose.onNodeWithText("关于", substring = false).performScrollTo().performClick()
                compose.onNodeWithText("开源许可证").assertExists()
                runBlocking { repository.setUiStyle(UiStyle.MD3) }
                waitForStyle(repository, UiStyle.MD3)
                compose.onNodeWithText("开源许可证").assertExists()
                pressBack()
                compose.onNodeWithTag("ui-style-MD3").performScrollTo().assertIsSelected()
                scenario.onActivity { assertSame(originalActivity, it) }
            } finally {
                runBlocking { repository.setUiStyle(original) }
            }
        }
    }

    @Test
    fun selectedStyleSurvivesActivityRecreationAndDialogSwitch() {
        ActivityScenario.launch<MainActivity>(mainIntent()).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            scenario.onActivity { repository = it.userPreferencesRepository }
            val original = runBlocking { repository.data.first().uiStyle }
            try {
                runBlocking { repository.setUiStyle(UiStyle.MIUIX) }
                waitForStyle(repository, UiStyle.MIUIX)
                compose.onAllNodesWithText("设置")[0].performClick()
                compose.onNodeWithTag("ui-style-MIUIX").performScrollTo().assertIsSelected()
                scenario.recreate()
                compose.onNodeWithTag("ui-style-MIUIX").performScrollTo().assertIsSelected()
                compose.onNodeWithText("应用图标").performScrollTo().performClick()
                compose.onNodeWithText("取消").assertExists()
                runBlocking { repository.setUiStyle(UiStyle.MD3) }
                waitForStyle(repository, UiStyle.MD3)
                compose.onNodeWithText("取消").assertExists().performClick()
                compose.onNodeWithTag("ui-style-MD3").performScrollTo().assertIsSelected()
            } finally {
                runBlocking { repository.setUiStyle(original) }
            }
        }
    }

    @Test
    fun nativeReplyDraftSurvivesLiveStyleSwitch() {
        val intent = Intent(context, ReplyActivity::class.java).putExtra("type", "feed")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<ReplyActivity>(intent).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            lateinit var originalActivity: ReplyActivity
            scenario.onActivity {
                repository = it.userPreferencesRepository
                originalActivity = it
                it.findViewById<TextInputEditText>(R.id.editText).setText("切换界面时保留这份草稿")
            }
            val original = runBlocking { repository.data.first().uiStyle }
            try {
                for (style in listOf(UiStyle.MIUIX, UiStyle.MD3, UiStyle.MIUIX)) {
                    runBlocking { repository.setUiStyle(style) }
                    compose.waitUntil(5_000) {
                        var applied = false
                        scenario.onActivity {
                            val background = it.findViewById<android.view.View>(R.id.inputLayout).background as GradientDrawable
                            val radius = (if (style == UiStyle.MIUIX) 28 else 16) * it.resources.displayMetrics.density
                            applied = kotlin.math.abs((background.cornerRadii?.firstOrNull() ?: 0f) - radius) < 2f
                        }
                        applied
                    }
                    scenario.onActivity {
                        assertSame(originalActivity, it)
                        assertEquals(style == UiStyle.MIUIX, it.findViewById<android.view.View>(R.id.replySend).isShown)
                        assertEquals(style == UiStyle.MIUIX, it.findViewById<android.view.View>(R.id.replyCancel).isShown)
                        assertEquals(style == UiStyle.MIUIX, it.findViewById<android.view.View>(R.id.replyDragHandle).isShown)
                        assertEquals(style == UiStyle.MD3, it.findViewById<android.view.View>(R.id.publish).isShown)
                        if (style == UiStyle.MD3) {
                            listOf(R.id.emojiBtn, R.id.imageBtn, R.id.otherImageBtn, R.id.atBtn, R.id.tagBtn).forEach { id ->
                                val toolbarIcon = it.findViewById<android.widget.ImageView>(id)
                                org.junit.Assert.assertNotNull("MD3 toolbar icon must retain its theme tint", toolbarIcon.imageTintList)
                            }
                        }
                        val icon = it.findViewById<android.widget.ImageView>(R.id.imageBtn).drawable
                        assertEquals(style == UiStyle.MIUIX,
                            icon is com.example.c001apk.compose.view.VectorGlyphDrawable)
                        assertEquals(style == UiStyle.MIUIX, it.onBackPressedDispatcher.hasEnabledCallbacks())
                        assertEquals("切换界面时保留这份草稿", it.findViewById<TextInputEditText>(R.id.editText).text.toString())
                    }
                }
            } finally {
                runBlocking { repository.setUiStyle(original) }
            }
        }
    }

    @Test
    fun replyPredictiveBackCancelsWithoutLosingDraftAndCommitsOnce() {
        val intent = Intent(context, ReplyActivity::class.java).putExtra("type", "feed")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<ReplyActivity>(intent).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            scenario.onActivity { repository = it.userPreferencesRepository }
            val original = runBlocking { repository.data.first().uiStyle }
            try {
                runBlocking { repository.setUiStyle(UiStyle.MIUIX) }
                compose.waitUntil(5_000) {
                    var ready = false
                    scenario.onActivity {
                        ready = it.findViewById<android.widget.ImageView>(R.id.imageBtn).drawable is
                            com.example.c001apk.compose.view.VectorGlyphDrawable
                    }
                    ready
                }
                scenario.onActivity {
                    it.findViewById<TextInputEditText>(R.id.editText).setText("手势取消后保留")
                    it.onBackPressedDispatcher.dispatchOnBackStarted(
                        androidx.activity.BackEventCompat(0f, 500f, 0f, androidx.activity.BackEventCompat.EDGE_LEFT))
                    it.onBackPressedDispatcher.dispatchOnBackProgressed(
                        androidx.activity.BackEventCompat(200f, 500f, .5f, androidx.activity.BackEventCompat.EDGE_LEFT))
                    assertTrue(it.findViewById<android.view.View>(R.id.main).translationY > 0f)
                    it.onBackPressedDispatcher.dispatchOnBackCancelled()
                }
                compose.waitUntil(5_000) {
                    var restored = false
                    scenario.onActivity { restored = it.findViewById<android.view.View>(R.id.main).translationY == 0f }
                    restored
                }
                scenario.onActivity {
                    assertFalse(it.isFinishing)
                    assertEquals("手势取消后保留", it.findViewById<TextInputEditText>(R.id.editText).text.toString())
                    it.onBackPressedDispatcher.dispatchOnBackStarted(
                        androidx.activity.BackEventCompat(0f, 500f, 0f, androidx.activity.BackEventCompat.EDGE_LEFT))
                    it.onBackPressedDispatcher.dispatchOnBackProgressed(
                        androidx.activity.BackEventCompat(200f, 500f, .6f, androidx.activity.BackEventCompat.EDGE_LEFT))
                    it.onBackPressedDispatcher.onBackPressed()
                }
                compose.waitUntil(5_000) { scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED }
            } finally {
                runBlocking { repository.setUiStyle(original) }
            }
        }
    }

    private fun mainIntent() = Intent(context, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    private fun waitForStyle(repository: UserPreferencesRepository, style: UiStyle) {
        compose.waitUntil(5_000) { runBlocking { repository.data.first().uiStyle == style } }
        compose.waitForIdle()
    }
}
