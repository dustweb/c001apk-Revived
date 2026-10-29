package com.example.c001apk.compose.ui.settings

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.ui.blacklist.MiuixBlacklist
import com.example.c001apk.compose.ui.component.cards.FeedCard
import com.example.c001apk.compose.ui.component.cards.FeedReplyCard
import com.example.c001apk.compose.ui.component.cards.miuix.*
import com.example.c001apk.compose.ui.home.HomeTabEditorDialog
import com.example.c001apk.compose.ui.home.TabType
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.example.c001apk.compose.util.CookieUtil
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.io.File

class MiuixPageCoverageTest {
    @get:Rule val compose = createComposeRule()
    private var oldLogin = false
    @Before fun before() { oldLogin = CookieUtil.isLogin; CookieUtil.isLogin = true }
    @After fun after() { CookieUtil.isLogin = oldLogin }

    private fun feed() = Gson().fromJson("""{"id":"motion","uid":"42","username":"预览用户",
        "message":"按压正文检查轻微反馈，不会打开真实动态。","likenum":"9","replynum":"12",
        "userAction":{"like":1},"dateline":1700000000}""", HomeFeedResponse.Data::class.java)

    @Composable private fun Theme(neutral: Boolean = false, content: @Composable () -> Unit) {
        C001apkComposeTheme(uiStyle = UiStyle.MIUIX, darkTheme = true, materialYou = false) {
            Box(Modifier.fillMaxSize().background(if (neutral) top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface else Color.Magenta)) { content() }
        }
    }

    @Composable private fun FeedPreview() {
        FeedCard(data = feed(), isFeedContent = false, modifier = Modifier.padding(16.dp),
            onViewUser = {}, onViewFeed = { _, _ -> }, onOpenLink = { _, _ -> },
            onCopyText = {}, onBlockUser = {})
    }

    @Test fun likedActionOnlyChangesIconAndCountColor() {
        compose.setContent { Theme(neutral = true) { FeedPreview() } }
        compose.onNodeWithTag("feed-like-motion").assertIsSelected()
        val card = compose.onNodeWithTag("miuix-feed-motion").captureToImage().toPixelMap()
        val button = compose.onNodeWithTag("feed-like-motion").captureToImage().toPixelMap()
        val background = card[card.width / 2, card.height - 4]
        val buttonBackground = button[2, button.height / 2]
        assertEquals(background.red, buttonBackground.red, .02f)
        assertEquals(background.green, buttonBackground.green, .02f)
        assertEquals(background.blue, buttonBackground.blue, .02f)
        capture("liked-feed", nodeTag = "miuix-feed-motion")
    }

    @Test fun richTextPressIsSubtleAndRestoresAfterCancel() {
        compose.setContent { Theme { FeedPreview() } }
        checkPress("miuix-feed-motion", "feed-body-motion")
    }

    @Test fun receivedLikeCardHasTheSamePressFeedback() {
        val notice = Gson().fromJson("""{"id":"notice","uid":"42","message":"通知中的回复",
            "fetchType":"feed_reply","likeUserInfo":{"uid":"43","username":"点赞用户"},
            "likeTime":1700000000}""", HomeFeedResponse.Data::class.java)
        compose.setContent { Theme { FeedReplyCard(data = notice, onViewUser = {}, onOpenLink = { _, _ -> },
            onCopyText = {}, onBlockUser = { _, _ -> }) } }
        checkPress("miuix-comment-notice", "miuix-comment-notice")
    }

    private fun checkPress(cardTag: String, targetTag: String) {
        compose.waitForIdle()
        val row = compose.onNodeWithTag(cardTag).fetchSemanticsNode().boundsInRoot.center.y.toInt()
        fun pixels(): Int {
            val bitmap = compose.onRoot().captureToImage().toPixelMap()
            return (0 until bitmap.width).count { x ->
                val c = bitmap[x, row]
                !(c.red > .98f && c.blue > .98f && c.green < .02f)
            }
        }
        val normal = pixels()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag(targetTag).performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(250)
        val pressed = pixels()
        val ratio = pressed.toFloat() / normal
        assertTrue("Expected subtle press, ratio=$ratio ($normal -> $pressed)", ratio in .97f.. .997f)
        compose.onNodeWithTag(targetTag).performTouchInput { cancel() }
        compose.mainClock.advanceTimeBy(500)
        assertEquals(normal.toFloat(), pixels().toFloat(), 2f)
        compose.mainClock.autoAdvance = true
    }

    @Test fun blacklistEntriesUseRowsAndKeepAddOpenRemoveActions() {
        val entries = mutableStateListOf("100001", "100002")
        var input by mutableStateOf(TextFieldValue())
        val opened = mutableListOf<String>()
        compose.setContent { Theme {
            MiuixBlacklist("USER", entries, input, { input = it },
                { entries.add(input.text); input = TextFieldValue() }, opened::add, { entries.remove(it) },
                {}, {}, {}, {})
        } }
        compose.onNodeWithContentDescription("移除 100001").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("100002"), entries.toList()); assertTrue(opened.isEmpty()) }
        compose.onNodeWithText("UID 100002").performClick()
        compose.runOnIdle { assertEquals(listOf("100002"), opened) }
        compose.onNode(hasSetTextAction()).performScrollTo().performTextInput("100003")
        compose.onNodeWithText("添加").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf("100002", "100003"), entries.toList()) }
        val back = compose.onNodeWithContentDescription("返回").captureToImage().toPixelMap()
        assertTrue("Back icon should remain visible on dark backgrounds", (0 until back.width).any { x ->
            (0 until back.height).any { y -> val c = back[x, y]; c.red > .7f && c.green > .7f && c.blue > .7f && c.alpha > .9f }
        })
        capture("blacklist")
    }

    @Test fun nativeConversationChatAndProfileKeepTheirActions() {
        var profile by mutableStateOf(false)
        val user = Gson().fromJson("""{"id":"message","uid":"42","username":"预览用户","level":3,
            "ukey":"conversation","messageUid":"42","messageUsername":"预览用户","message":"消息预览，含表情 🙂",
            "bio":"这是个人页布局预览。","unreadNum":3,"dateline":1700000000,"follow":17,"fans":4}""",
            HomeFeedResponse.Data::class.java)
        var chat = 0; var follow = 0; var pm = 0
        compose.setContent { Theme {
            Column(Modifier.fillMaxSize().background(top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())) {
                if (profile) MiuixUserInfoCard(user, { _, _ -> follow++ }, { _, _ -> pm++ }, { _, _ -> })
                else {
                    MiuixConversationCard(Modifier.padding(16.dp), user, { _, _ -> }, {}, null, { _, _, _ -> chat++ })
                    MiuixChatBubble(Modifier, user, false, {}, { _, _, _ -> }, {}, { _, _ -> }, {})
                    MiuixChatBubble(Modifier, user, true, {}, { _, _, _ -> }, {}, { _, _ -> }, {})
                }
            }
        } }
        compose.onNodeWithTag("miuix-conversation-conversation").performClick()
        compose.runOnIdle { assertEquals(1, chat) }
        capture("conversation-chat")
        compose.runOnIdle { profile = true }
        compose.onNodeWithText("私信").performScrollTo().performClick()
        compose.onNode(hasText("关注") and SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Role, androidx.compose.ui.semantics.Role.Button)).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, pm); assertEquals(1, follow) }
        capture("profile")
    }

    @Test fun homeEditorRetainsLastTabAndAllowsReordering() {
        val original = listOf(TabType.FOLLOW, TabType.FEED, TabType.APP)
        var result: List<TabType>? = null
        var enabled: Set<TabType>? = null
        compose.setContent { Theme {
            HomeTabEditorDialog(original, setOf(TabType.FEED), {}, { order, visible -> result = order; enabled = visible })
        } }
        compose.onNode(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.ToggleableState, androidx.compose.ui.state.ToggleableState.On)).assertIsNotEnabled()
        compose.onNodeWithText("关注").performClick()
        compose.onAllNodes(isToggleable()).assertAll(isEnabled())
        val grips = compose.onAllNodesWithContentDescription("长按拖动排序", useUnmergedTree = true)
        val first = grips[0].fetchSemanticsNode().boundsInRoot
        val last = grips[2].fetchSemanticsNode().boundsInRoot
        grips[0].performTouchInput { down(center) }
        android.os.SystemClock.sleep(700)
        compose.waitForIdle()
        grips[0].performTouchInput {
            advanceEventTime(700)
            moveBy(androidx.compose.ui.geometry.Offset(0f, last.center.y - first.center.y))
            advanceEventTime(32)
            up()
        }
        capture("home-editor", dialog = true)
        compose.onNodeWithText("完成").performClick()
        compose.runOnIdle {
            assertEquals(original.toSet(), result?.toSet())
            assertNotEquals(original, result)
            assertEquals(setOf(TabType.FEED, TabType.FOLLOW), enabled)
        }
    }

    private fun capture(name: String, dialog: Boolean = false, nodeTag: String? = null) {
        val bitmap = (if (nodeTag != null) compose.onNodeWithTag(nodeTag) else if (dialog) compose.onNode(isDialog()) else compose.onRoot()).captureToImage().asAndroidBitmap()
        File(ApplicationProvider.getApplicationContext<Context>().cacheDir, "miuix-pages-$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
