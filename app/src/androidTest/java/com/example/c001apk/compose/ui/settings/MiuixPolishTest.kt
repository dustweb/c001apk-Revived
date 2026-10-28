package com.example.c001apk.compose.ui.settings

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.ui.component.design.*
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class MiuixPolishTest {
    @get:Rule val compose = createComposeRule()

    @Test fun dialogActionsShareWidthAndChoiceRowsSelectOnlyOnce() {
        var selected by mutableIntStateOf(-1)
        var clicks = 0
        var committed = -1
        compose.setContent {
            C001apkComposeTheme(uiStyle = UiStyle.MIUIX, darkTheme = true, materialYou = false,
                fontScale = 1.3f, contentScale = 1.1f) {
                AlertDialog(modifier = Modifier.testTag("dialog"), onDismissRequest = {},
                    title = { Text("选择选项") },
                    text = {
                        Column {
                            listOf("选项一", "选项二").forEachIndexed { index, label ->
                                ListItemDialogItem(label, selected == index) { selected = index; clicks++ }
                            }
                        }
                    },
                    dismissButton = { TextButton(onClick = {}) { Text("取消") } },
                    confirmButton = { TextButton(onClick = { committed = selected }) { Text("确定") } },
                )
            }
        }
        val left = compose.onNodeWithText("取消").fetchSemanticsNode().boundsInRoot
        val right = compose.onNodeWithText("确定").fetchSemanticsNode().boundsInRoot
        assertEquals(left.width, right.width, 1f)
        assertTrue("Actions should span the dialog", left.width > 150f)
        assertTrue(left.right < right.left)
        assertEquals(left.top, right.top, 1f)
        capture("choices-off", "dialog")
        compose.onNodeWithText("选项一").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(1, clicks); assertEquals(-1, committed) }
        capture("choices-on", "dialog")
        compose.onNodeWithText("确定").performClick()
        compose.runOnIdle { assertEquals(0, committed) }
    }

    @Test fun unselectedTabsHaveNoOutlineAndSelectionStillChanges() {
        var selected by mutableIntStateOf(0)
        compose.setContent {
            C001apkComposeTheme(uiStyle = UiStyle.MIUIX, darkTheme = true, materialYou = false) {
                Column(Modifier.fillMaxSize().background(top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface)) {
                    FilledTabs(listOf("隐私", "安全"), selected, { selected = it }, Modifier.testTag("tabs"))
                    FilledTabs(listOf("窄一", "窄二"), selected, {}, Modifier.width(40.dp))
                }
            }
        }
        val unselected = compose.onNodeWithText("安全").fetchSemanticsNode().boundsInRoot
        val frame = compose.onRoot().captureToImage().toPixelMap()
        val edge = frame[(unselected.left + 1).toInt(), unselected.center.y.toInt()]
        val background = frame[frame.width / 2, frame.height - 4]
        assertEquals(background.red, edge.red, .025f)
        assertEquals(background.green, edge.green, .025f)
        assertEquals(background.blue, edge.blue, .025f)
        capture("filled-tabs", "tabs")
        compose.onNodeWithText("安全").performClick().assertIsSelected()
        compose.runOnIdle { assertEquals(1, selected) }
    }

    @Test fun refreshSpacingDoesNotMoveIdleContent() {
        var inset by mutableStateOf(0.dp)
        var refreshing by mutableStateOf(false)
        compose.setContent {
            C001apkComposeTheme(uiStyle = UiStyle.MIUIX, darkTheme = true, materialYou = false) {
                Column(Modifier.fillMaxSize().background(top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface)) {
                    top.yukonga.miuix.kmp.basic.Card(Modifier.fillMaxWidth().padding(16.dp)) {
                        top.yukonga.miuix.kmp.basic.BasicComponent(title = "账号卡片", summary = "刷新间距预览")
                    }
                    RefreshContainer(refreshing, {}, indicatorTopInset = inset) {
                        LazyColumn(Modifier.fillMaxSize()) {
                            item { Text("信息流", Modifier.fillMaxWidth().padding(16.dp).testTag("body"), color = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.onSurface) }
                        }
                    }
                }
            }
        }
        val idleTop = compose.onNodeWithTag("body").fetchSemanticsNode().boundsInRoot.top
        compose.runOnIdle { inset = 12.dp }
        compose.waitForIdle()
        assertEquals(idleTop, compose.onNodeWithTag("body").fetchSemanticsNode().boundsInRoot.top, 1f)
        compose.runOnIdle { refreshing = true }
        compose.waitForIdle()
        val label = compose.onNodeWithText("正在刷新").fetchSemanticsNode().boundsInRoot
        val content = compose.onNodeWithTag("body").fetchSemanticsNode().boundsInRoot
        assertTrue("Refresh text must not overlap content", label.bottom < content.top)
        capture("refresh-spacing")
        compose.runOnIdle { refreshing = false }
        compose.waitUntil(5_000) {
            kotlin.math.abs(idleTop - compose.onNodeWithTag("body").fetchSemanticsNode().boundsInRoot.top) < 1f
        }
    }

    private fun capture(name: String, tag: String? = null) {
        val bitmap = (if (tag == null) compose.onRoot() else compose.onNodeWithTag(tag))
            .captureToImage().asAndroidBitmap()
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.cacheDir, "miuix-polish-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
