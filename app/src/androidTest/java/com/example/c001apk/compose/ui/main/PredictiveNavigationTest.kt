package com.example.c001apk.compose.ui.main

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.ui.component.design.TextField
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PredictiveNavigationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var controller: NavHostController
    private lateinit var activity: ComponentActivity
    private var style by mutableStateOf(UiStyle.MIUIX)
    private var localHistory by mutableIntStateOf(0)
    private lateinit var detailModel: DraftModel

    class DraftModel : ViewModel()

    private fun launch() {
        compose.setContent {
            activity = LocalActivity.current as ComponentActivity
            controller = rememberAppNavController()
            C001apkComposeTheme(uiStyle = style, darkTheme = false, materialYou = false) {
                AppNavHost(controller, "home", Modifier.fillMaxSize()) {
                    appComposable("home") { Box(Modifier.fillMaxSize().background(Color.Red)) }
                    appComposable("detail") {
                        com.example.c001apk.compose.ui.component.design.PageBackAction(localHistory > 0) { localHistory-- }
                        detailModel = viewModel<DraftModel>()
                        var draft by rememberSaveable { mutableStateOf("") }
                        Box(Modifier.fillMaxSize().background(Color.Blue)) {
                            TextField(draft, { draft = it }, Modifier.testTag("draft"))
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { controller.navigate("detail") }
        compose.waitForIdle()
        compose.waitUntilExactlyOneExists(hasTestTag("draft"), timeoutMillis = 5_000)
        compose.onNode(hasSetTextAction()).performTextInput("未发送的草稿")
        compose.runOnIdle { activity.currentFocus?.clearFocus() }
        compose.waitForIdle()
    }

    private fun progress(value: Float) = compose.runOnIdle {
        activity.onBackPressedDispatcher.dispatchOnBackProgressed(
            BackEventCompat(200f, 500f, value, BackEventCompat.EDGE_LEFT))
    }

    @Test fun cancelledGestureRevealsPreviousPageAndKeepsDraftAndViewModel() {
        launch()
        val original = detailModel
        compose.runOnIdle {
            activity.onBackPressedDispatcher.dispatchOnBackStarted(
                BackEventCompat(0f, 500f, 0f, BackEventCompat.EDGE_LEFT))
        }
        progress(0.55f)
        compose.waitForIdle()
        val frame = compose.onRoot().captureToImage().toPixelMap()
        val revealed = frame[frame.width / 8, frame.height / 2]
        assertTrue("Back progress must reveal the previous page: $revealed", revealed.red > .2f && revealed.blue < .1f)
        compose.runOnIdle { activity.onBackPressedDispatcher.dispatchOnBackCancelled() }
        compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assertTextEquals("未发送的草稿")
        compose.runOnIdle {
            assertEquals("detail", controller.currentDestination?.route)
            assertSame(original, detailModel)
            assertEquals(Lifecycle.State.RESUMED, controller.currentBackStackEntry?.lifecycle?.currentState)
        }
    }

    @Test fun committedGesturePopsExactlyOneEntry() {
        launch()
        compose.runOnIdle {
            activity.onBackPressedDispatcher.dispatchOnBackStarted(
                BackEventCompat(0f, 500f, 0f, BackEventCompat.EDGE_LEFT))
        }
        progress(.65f)
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals("home", controller.currentDestination?.route)
            assertFalse(controller.popBackStack())
        }
    }

    @Test fun styleSwitchKeepsTheCurrentEntryAndItsDraft() {
        launch()
        val original = detailModel
        val entryId = controller.currentBackStackEntry!!.id
        compose.runOnIdle { style = UiStyle.MD3 }
        compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assertTextEquals("未发送的草稿")
        compose.runOnIdle { style = UiStyle.MIUIX }
        compose.waitForIdle()
        compose.onNode(hasSetTextAction()).assertTextEquals("未发送的草稿")
        compose.runOnIdle {
            assertSame(original, detailModel)
            assertEquals(entryId, controller.currentBackStackEntry!!.id)
        }
    }
    @Test fun pageLocalBackActionRunsBeforePoppingItsRoute() {
        launch()
        compose.runOnIdle { localHistory = 1 }
        compose.waitForIdle()
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        compose.runOnIdle {
            assertEquals(0, localHistory)
            assertEquals("detail", controller.currentDestination?.route)
        }
        compose.onNode(hasSetTextAction()).assertTextEquals("未发送的草稿")
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals("home", controller.currentDestination?.route) }
    }

    @Test fun paneBackTakesPriorityOverThePageAndCanBeCancelled() = verifyPaneBack(commit = false)
    @Test fun committedPaneBackClosesOnlyThePane() = verifyPaneBack(commit = true)

    private fun verifyPaneBack(commit: Boolean) {
        var open by mutableStateOf(true)
        compose.setContent {
            activity = LocalActivity.current as ComponentActivity
            controller = rememberAppNavController()
            C001apkComposeTheme(uiStyle = UiStyle.MIUIX, materialYou = false) {
                FeedSplitLayout(showDetail = open, canHandleBack = true, onClose = { open = false },
                    detailContent = { Box(Modifier.fillMaxSize().background(Color.Blue).testTag("pane")) }) {
                    AppNavHost(controller, "home", Modifier.fillMaxSize(), isPane = true, backEnabled = !open) {
                        appComposable("home") { Box(Modifier.fillMaxSize().background(Color.Red)) }
                        appComposable("next") { Box(Modifier.fillMaxSize().background(Color.Green).testTag("next")) }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { controller.navigate("next") }
        compose.waitUntilExactlyOneExists(hasTestTag("next"), 5_000)
        compose.waitForIdle()
        val initialLeft = compose.onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.left
        compose.runOnIdle {
            activity.onBackPressedDispatcher.dispatchOnBackStarted(
                BackEventCompat(0f, 500f, 0f, BackEventCompat.EDGE_LEFT))
        }
        progress(.5f)
        compose.waitForIdle()
        val movingLeft = compose.onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.left
        assertTrue("The pane must follow the gesture", movingLeft > initialLeft * 1.2f)
        compose.runOnIdle {
            if (commit) activity.onBackPressedDispatcher.onBackPressed()
            else activity.onBackPressedDispatcher.dispatchOnBackCancelled()
        }
        compose.waitForIdle()
        compose.runOnIdle { assertEquals("next", controller.currentDestination?.route) }
        if (commit) {
            compose.onNodeWithTag("pane").assertDoesNotExist()
        } else {
            val restored = compose.onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.left
            assertEquals(initialLeft, restored, 1f)
        }
    }

}
