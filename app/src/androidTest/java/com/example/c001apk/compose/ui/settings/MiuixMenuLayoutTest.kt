package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.ui.component.design.DropdownMenu
import com.example.c001apk.compose.ui.component.design.DropdownMenuItem
import com.example.c001apk.compose.ui.component.design.IconButton
import com.example.c001apk.compose.ui.component.design.MenuAction
import com.example.c001apk.compose.ui.component.design.MiuixActionMenuButton
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.More

@RunWith(AndroidJUnit4::class)
class MiuixMenuLayoutTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun nativeMenuTracksItsAnchorAndUpdatesDarkIconsAtLargeScale() {
        var dark by mutableStateOf(false)
        var bottom by mutableStateOf(false)
        var clicks = 0
        compose.setContent {
            C001apkComposeTheme(uiStyle = UiStyle.MIUIX, darkTheme = dark, materialYou = false,
                fontScale = 1.3f, contentScale = 1.15f) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    MiuixActionMenuButton(listOf(MenuAction("复制链接", MiuixIcons.Copy) { clicks++ }),
                        Modifier.align(if (bottom) Alignment.BottomEnd else Alignment.TopEnd).testTag("anchor"))
                }
            }
        }
        compose.onNodeWithTag("anchor").performClick()
        assertAnchored()
        compose.runOnIdle { dark = true }
        assertBrightIcon("action-menu-icon-复制链接")
        compose.runOnIdle { bottom = true }
        assertAnchored()
        val anchor = compose.onNodeWithTag("anchor").fetchSemanticsNode().boundsInRoot
        val menu = compose.onNodeWithText("复制链接").fetchSemanticsNode().boundsInRoot
        assertTrue("The menu must open above a bottom-edge anchor", menu.bottom <= anchor.top + anchor.height)
        compose.onNodeWithText("复制链接").performClick()
        assertEquals(1, clicks)
        compose.onNodeWithText("复制链接").assertDoesNotExist()
    }

    @Test
    fun compatibilityMenuKeepsCompactWidthAndReadableDarkIcons() {
        var expanded by mutableStateOf(false)
        compose.setContent {
            C001apkComposeTheme(uiStyle = UiStyle.MIUIX, darkTheme = true, materialYou = false) {
                Box(Modifier.fillMaxSize().padding(16.dp)) {
                    Box(Modifier.align(Alignment.TopEnd)) {
                        IconButton(onClick = { expanded = true }, modifier = Modifier.testTag("anchor")) {
                            Icon(MiuixIcons.More, "更多")
                        }
                        DropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem(text = { Text("复制链接") }, onClick = { expanded = false },
                                leadingIcon = { Icon(MiuixIcons.Copy, null, Modifier.testTag("legacy-icon")) })
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag("anchor").performClick()
        assertAnchored()
        assertBrightIcon("legacy-icon")
        compose.onNodeWithText("复制链接").performClick()
        compose.onNodeWithText("复制链接").assertDoesNotExist()
    }

    private fun assertAnchored() {
        val root = compose.onRoot().fetchSemanticsNode().boundsInRoot
        val anchor = compose.onNodeWithTag("anchor").fetchSemanticsNode().boundsInRoot
        val menu = compose.onNodeWithText("复制链接").fetchSemanticsNode().boundsInRoot
        assertTrue("The popup must not stretch across the screen: $menu", menu.width < root.width * 0.85f)
        assertTrue("The popup must stay beside its trigger: $menu / $anchor", menu.right >= anchor.left)
        assertTrue("The popup must stay inside the window", menu.left >= root.left && menu.right <= root.right &&
            menu.top >= root.top && menu.bottom <= root.bottom)
    }

    private fun assertBrightIcon(tag: String) {
        val pixels = compose.onNodeWithTag(tag, useUnmergedTree = true).captureToImage().toPixelMap()
        val brightest = (0 until pixels.height).maxOf { y -> (0 until pixels.width).maxOf { x -> pixels[x, y].luminance() } }
        assertTrue("The dark-theme icon must be clearly visible: $brightest", brightest > 0.6f)
    }
}
