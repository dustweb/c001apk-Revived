package com.example.c001apk.compose.ui.settings

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import com.example.c001apk.compose.ui.component.design.DialogAction
import com.example.c001apk.compose.ui.component.design.showAppDialog
import com.example.c001apk.compose.ui.main.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlatformDialogTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test
    fun platformDialogRetainsItsActionDuringHotSwitchAndRemovesItsHost() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            lateinit var repository: UserPreferencesRepository
            scenario.onActivity { repository = it.userPreferencesRepository }
            val original = runBlocking { repository.data.first().uiStyle }
            var actions = 0
            try {
                runBlocking { repository.setUiStyle(UiStyle.MIUIX) }
                scenario.onActivity {
                    showAppDialog(it, "窗口主题测试", "保留这条提示", actions = listOf(DialogAction("继续操作", true) { actions++ }))
                }
                compose.waitUntil(5_000) { compose.onAllNodesWithText("保留这条提示").fetchSemanticsNodes().isNotEmpty() }
                runBlocking { repository.setUiStyle(UiStyle.MD3) }
                compose.onNodeWithText("保留这条提示").assertExists()
                compose.onNodeWithText("继续操作").performClick()
                compose.waitUntil(5_000) { actions == 1 }
                scenario.onActivity { assertNull(it.window.decorView.findViewWithTag<View>("themed-platform-dialog")) }
                assertEquals(1, actions)
            } finally {
                runBlocking { repository.setUiStyle(original) }
            }
        }
    }
}
