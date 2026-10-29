package com.example.c001apk.compose.ui.settings

import android.graphics.Bitmap
import android.widget.EditText
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.ui.chat.ChatBottom
import com.example.c001apk.compose.ui.chat.ChatViewModel
import com.example.c001apk.compose.ui.main.AppNavHost
import com.example.c001apk.compose.ui.main.MainActivity
import com.example.c001apk.compose.ui.main.rememberAppNavController
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class ChatComposerStateTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun changingNavigationStylePreservesTheNativeEditorDraft() {
        var style by mutableStateOf(UiStyle.MIUIX)
        var sent: String? = null
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent {
                val controller = rememberAppNavController()
                C001apkComposeTheme(uiStyle = style, darkTheme = true, materialYou = false) {
                    AppNavHost(controller, "composer") {
                        appComposable("composer") {
                            val model = hiltViewModel<ChatViewModel, ChatViewModel.ViewModelFactory> { it.create("composer-test") }
                            var clearFocus by remember { mutableStateOf(false) }
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                                ChatBottom(viewModel = model, onPickImage = {}, onPickOtherImage = {},
                                    onSendMessage = { sent = it }, clearText = false, resetClear = {},
                                    clearFocus = clearFocus, resetFocus = { clearFocus = false }, onClearFocus = { clearFocus = true })
                            }
                        }
                    }
                }
            } }
            compose.waitForIdle()
            onView(isAssignableFrom(EditText::class.java)).perform(replaceText("切换风格也保留私信草稿"))
            listOf(UiStyle.MD3, UiStyle.MIUIX).forEach { target ->
                compose.runOnIdle { style = target }
                compose.waitForIdle()
                onView(isAssignableFrom(EditText::class.java)).check(matches(withText("切换风格也保留私信草稿")))
            }
            compose.onNodeWithContentDescription("表情").performClick()
            compose.waitForIdle()
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
                File(instrumentation.targetContext.cacheDir, "miuix-chat-composer.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            compose.onNodeWithText("发送").performClick()
            assertEquals("切换风格也保留私信草稿", sent)
        }
    }
}
