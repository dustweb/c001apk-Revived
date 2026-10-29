package com.example.c001apk.compose.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.c001apk.compose.R
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.feed.reply.ReplyActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ReplyEditorLayoutTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun largeEditorTextAndToolIconsFitWithoutLosingDraft() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, ReplyActivity::class.java).putExtra("type", "createFeed")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<ReplyActivity>(intent).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            scenario.onActivity { repository = it.userPreferencesRepository }
            val original = runBlocking { repository.data.first() }
            try {
                runBlocking { repository.setUiStyle(UiStyle.MIUIX); repository.setFontScale(1.3f); repository.setContentScale(1.15f) }
                compose.waitUntil(5_000) {
                    var ready = false
                    scenario.onActivity {
                        val title = it.findViewById<TextView>(R.id.title)
                        val publish = it.findViewById<TextView>(R.id.publish)
                        ready = it.findViewById<TextInputLayout>(R.id.textInputLayout).boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_NONE &&
                            title.width > title.paint.measureText(title.text.toString()) &&
                            publish.width - publish.paddingLeft - publish.paddingRight >= publish.paint.measureText(publish.text.toString())
                    }
                    ready
                }
                scenario.onActivity {
                    val title = it.findViewById<TextView>(R.id.title)
                    val publish = it.findViewById<TextView>(R.id.publish)
                    val editor = it.findViewById<TextInputEditText>(R.id.editText)
                    val image = it.findViewById<ImageView>(R.id.imageBtn)
                    val density = it.resources.displayMetrics.density
                    assertTrue(title.right < publish.left)
                    assertTrue(image.drawable.intrinsicHeight >= density * 20)
                    assertTrue(image.height >= density * 40)
                    assertEquals(ImageView.ScaleType.CENTER_INSIDE, image.scaleType)
                    assertFalse(publish.isEnabled)
                    editor.setText("发布面板布局预览，不会发送")
                    assertTrue(publish.isEnabled)
                }
                compose.waitUntil(5_000) {
                    var fits = false
                    scenario.onActivity {
                        val counter = it.findViewById<TextView>(com.google.android.material.R.id.textinput_counter)
                        fits = counter.width - counter.paddingLeft - counter.paddingRight >= counter.paint.measureText(counter.text.toString())
                    }
                    fits
                }
                scenario.onActivity {
                    val panel = it.findViewById<android.view.View>(R.id.inputLayout)
                    val bitmap = Bitmap.createBitmap(panel.width, panel.height, Bitmap.Config.ARGB_8888)
                    panel.draw(Canvas(bitmap))
                    File(it.cacheDir, "miuix-pages-editor.png").outputStream().use { output -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, output) }
                }
                scenario.onActivity { it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
                compose.waitUntil(10_000) {
                    var ready = false
                    runCatching { scenario.onActivity {
                        val title = it.findViewById<TextView>(R.id.title)
                        val publish = it.findViewById<TextView>(R.id.publish)
                        ready = it.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE &&
                            title.width > 0 && title.right < publish.left &&
                            it.findViewById<TextInputLayout>(R.id.textInputLayout).boxBackgroundMode == TextInputLayout.BOX_BACKGROUND_NONE
                    } }
                    ready
                }
                scenario.onActivity {
                    assertEquals("发布面板布局预览，不会发送", it.findViewById<TextInputEditText>(R.id.editText).text.toString())
                    val title = it.findViewById<TextView>(R.id.title)
                    val publish = it.findViewById<TextView>(R.id.publish)
                    assertTrue(title.width >= title.paint.measureText(title.text.toString()))
                    assertTrue(publish.right <= (publish.parent as android.view.View).width)
                }
                runBlocking { repository.setUiStyle(UiStyle.MD3) }
                compose.waitUntil(5_000) {
                    var restored = false
                    scenario.onActivity { restored = it.findViewById<TextInputLayout>(R.id.textInputLayout).boxBackgroundMode != TextInputLayout.BOX_BACKGROUND_NONE }
                    restored
                }
                scenario.onActivity {
                    assertEquals("发布面板布局预览，不会发送", it.findViewById<TextInputEditText>(R.id.editText).text.toString())
                    assertTrue(it.findViewById<TextView>(R.id.publish).isEnabled)
                }
            } finally {
                runBlocking { repository.setUiStyle(original.uiStyle); repository.setFontScale(original.fontScale); repository.setContentScale(original.contentScale) }
            }
        }
    }
}
