package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.ui.component.design.*
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class MiuixSharedComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun nativeScaffoldAndRefreshKeepBodyStateAcrossStyleChanges() {
        var style by mutableStateOf(UiStyle.MD3)
        var scrollPosition = 0
        compose.setContent {
            C001apkComposeTheme(uiStyle = style, materialYou = false) {
                Scaffold(topBar = { TopAppBar(title = "状态保留") }) { padding ->
                    Surface(Modifier.padding(padding)) {
                        RefreshContainer(false, {}) {
                            var draft by rememberSaveable { mutableStateOf("") }
                            val scroll = rememberScrollState()
                            val position = scroll.value
                            SideEffect { scrollPosition = position }
                            Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
                                TextField(draft, { draft = it }, labelText = "草稿", modifier = Modifier.testTag("draft"))
                                repeat(30) { Text("项目 $it", Modifier.height(48.dp).fillMaxWidth()) }
                            }
                        }
                    }
                }
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput("保留输入和滚动")
        compose.onNodeWithText("项目 25").performScrollTo()
        compose.waitForIdle()
        val before = scrollPosition
        assertTrue(before > 0)
        compose.runOnIdle { style = UiStyle.MIUIX }
        compose.waitForIdle()
        assertEquals(before, scrollPosition)
        compose.onNodeWithTag("draft").performScrollTo()
        compose.onNode(hasSetTextAction()).assertTextContains("保留输入和滚动")
        compose.runOnIdle { style = UiStyle.MD3 }
        compose.onNode(hasSetTextAction()).assertTextContains("保留输入和滚动")
    }

    @Test
    fun nativeRefreshHasOneStateOwnerAndSurvivesAStyleChange() {
        var style by mutableStateOf(UiStyle.MIUIX)
        var refreshing by mutableStateOf(false)
        var requests = 0
        compose.setContent {
            C001apkComposeTheme(uiStyle = style, materialYou = false) {
                RefreshContainer(refreshing, { requests++; refreshing = true }, Modifier.fillMaxSize()) {
                    LazyColumn(Modifier.fillMaxSize().testTag("refresh-list")) {
                        items(30) { Text("列表 $it", Modifier.fillMaxWidth().height(64.dp)) }
                    }
                }
            }
        }
        compose.onNodeWithTag("refresh-list").performTouchInput { swipeDown(durationMillis = 700) }
        compose.waitUntil(5_000) { requests == 1 }
        assertTrue(refreshing)
        compose.onNodeWithText("正在刷新").assertExists()
        compose.runOnIdle { style = UiStyle.MD3 }
        assertTrue(refreshing)
        assertEquals(1, requests)
        compose.runOnIdle { refreshing = false; style = UiStyle.MIUIX }
        compose.onNodeWithText("列表 0").assertExists()
        assertEquals(1, requests)
    }
}
