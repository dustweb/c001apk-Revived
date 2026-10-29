package com.example.c001apk.compose.ui.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Checks view geometry after the window and keyboard have settled. */
class EditorWindowTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    private fun View.children(): Sequence<View> = sequence {
        yield(this@children)
        if (this@children is ViewGroup) for (i in 0 until childCount) yieldAll(getChildAt(i).children())
    }

    private fun capture(name: String) {
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            File(instrumentation.targetContext.cacheDir, "$name.png").outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            bitmap.recycle()
        }
    }

    private fun awaitReady(scenario: ActivityScenario<Activity>, condition: (Activity) -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            var ready = false
            scenario.onActivity { ready = condition(it) }
            if (ready) return
            SystemClock.sleep(50)
        }
        capture("editor-timeout")
        fail("Editor window did not settle")
    }

    @Test fun headerAndEmojiWorkInTheActualWindow() {
        val intent = Intent().setClassName("com.example.c001apk.compose", "com.example.c001apk.compose.ui.feed.reply.ReplyActivity")
            .putExtra("type", "createFeed").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<Activity>(intent).use { scenario ->
            awaitReady(scenario) { it.hasWindowFocus() && ViewCompat.getRootWindowInsets(it.window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) == true }
            SystemClock.sleep(500)
            capture("editor-keyboard-window")
            scenario.onActivity { activity ->
                val views = activity.window.decorView.children().toList()
                val title = views.filterIsInstance<TextView>().first { it.text.toString() == "发布动态" }
                val publish = views.first { it.contentDescription == "发送" && it.isShown }
                val density = activity.resources.displayMetrics.density
                assertTrue("Title needs a start inset", title.left + title.paddingLeft >= 8 * density)
                val visible = Rect()
                assertTrue(publish.getGlobalVisibleRect(visible))
                assertEquals("Publish button is clipped", publish.width, visible.width())
                assertTrue(publish.width >= 48 * density)
                assertTrue(views.any { it.contentDescription == "关闭" && it.isShown })
                views.filterIsInstance<EditText>().first().setText("窗口测试，不会发送")
            }
            val bounds = Rect()
            scenario.onActivity { activity ->
                assertTrue(activity.window.decorView.children().first { it.contentDescription == "表情" }.getGlobalVisibleRect(bounds))
            }
            val time = SystemClock.uptimeMillis()
            listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEachIndexed { index, action ->
                val event = MotionEvent.obtain(time, time + index * 80, action, bounds.centerX().toFloat(), bounds.centerY().toFloat(), 0)
                instrumentation.uiAutomation.injectInputEvent(event, true)
                event.recycle()
            }
            awaitReady(scenario) { activity ->
                ViewCompat.getRootWindowInsets(activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) != true &&
                    activity.window.decorView.children().any { it is TextView && it.text.toString() == "最近" && it.isShown }
            }
            capture("editor-emoji-window")
        }
    }
}
