package com.example.c001apk.compose.ui.feed

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.content.Context
import android.graphics.Bitmap
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.model.HomeFeedResponse
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.base.LikeType
import com.example.c001apk.compose.ui.component.cards.FeedCard
import com.example.c001apk.compose.ui.component.cards.FeedReplyCard
import com.example.c001apk.compose.ui.component.cards.FeedReplySortCard
import com.example.c001apk.compose.ui.theme.C001apkComposeTheme
import com.example.c001apk.compose.ui.component.design.ModalBottomSheet
import com.example.c001apk.compose.util.CookieUtil
import com.google.gson.Gson
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import top.yukonga.miuix.kmp.theme.MiuixTheme

@RunWith(AndroidJUnit4::class)
class MiuixFeedContentTest {
    @get:Rule val compose = createComposeRule()
    private var previousLogin = false
    private var previousUid = ""

    @Before
    fun useLocalTestIdentity() {
        previousLogin = CookieUtil.isLogin
        previousUid = CookieUtil.uid
        CookieUtil.isLogin = true
        CookieUtil.uid = "child-user"
    }

    @After
    fun restoreIdentity() {
        CookieUtil.isLogin = previousLogin
        CookieUtil.uid = previousUid
    }

    @Test
    fun nativeFeedActionsDoNotTriggerTheParentCard() {
        val feed = feed()
        val opened = mutableListOf<Pair<String, Boolean>>()
        val authors = mutableListOf<String>()
        val likes = mutableListOf<Pair<String, LikeType>>()
        var copied: String? = null
        compose.setContent {
            TestTheme {
                FeedCard(isFeedContent = false, data = feed,
                    onViewUser = { authors.add(it) }, onViewFeed = { id, replies -> opened.add(id to replies) },
                    onOpenLink = { _, _ -> }, onCopyText = { copied = it },
                    onLike = { id, _, type -> likes.add(id to type) }, onBlockUser = {})
            }
        }
        compose.onNodeWithTag("feed-avatar-feed-1").performTouchInput { click() }
        assertEquals(listOf("author"), authors)
        assertTrue(opened.isEmpty())
        compose.onNodeWithTag("feed-like-feed-1").performTouchInput { click() }
        assertEquals(listOf("feed-1" to LikeType.FEED), likes)
        assertTrue(opened.isEmpty())
        compose.onNodeWithTag("feed-replies-feed-1").performTouchInput { click() }
        assertEquals(listOf("feed-1" to true), opened)
        compose.onNodeWithTag("miuix-feed-feed-1").performTouchInput { longClick(Offset(width / 2f, 4f)) }
        assertEquals("正文颜色检查", copied)
    }

    @Test
    fun nestedRepliesKeepParentIdsAndOwnerActions() {
        val comment = comment()
        val replies = mutableListOf<List<String?>>()
        val threads = mutableListOf<List<String?>>()
        val deletions = mutableListOf<Triple<String, LikeType, String?>>()
        compose.setContent {
            TestTheme {
                FeedReplyCard(data = comment, onViewUser = {},
                    onShowTotalReply = { id, uid, parent -> threads.add(listOf(id, uid, parent)) },
                    onOpenLink = { _, _ -> }, onCopyText = {}, onBlockUser = { _, _ -> },
                    onDelete = { id, type, parent -> deletions.add(Triple(id, type, parent)) },
                    onReply = { id, uid, name, parent, avatar -> replies.add(listOf(id, uid, name, parent, avatar)) })
            }
        }
        compose.onNodeWithTag("comment-reply-comment-1").performClick()
        assertEquals(listOf("comment-1", "parent-user", "主评论作者", null, null), replies.single())
        compose.onNodeWithTag("nested-reply-child-1").performTouchInput { click(Offset(width / 2f, 4f)) }
        assertEquals(listOf("child-1", "parent-user", "子回复作者", "comment-1", null), replies.last())

        compose.onNodeWithTag("nested-reply-child-1").performTouchInput { longClick(Offset(width / 2f, 4f)) }
        compose.onNodeWithText("显示回复").performClick()
        assertEquals(listOf("child-1", "parent-user", "comment-1"), threads.last())
        compose.onNodeWithTag("view-all-replies").performClick()
        assertEquals(listOf("comment-1", "parent-user", null), threads.last())

        compose.onNodeWithTag("nested-reply-child-1").performTouchInput { longClick(Offset(width / 2f, 4f)) }
        compose.onNodeWithText("删除").performClick()
        assertEquals(Triple("child-1", LikeType.REPLY, "comment-1"), deletions.single())
    }

    @Test
    fun switchingPreservesLikeStateAndUpdatesExistingRichTextView() {
        val original = feed()
        var style by mutableStateOf(UiStyle.MIUIX)
        var dark by mutableStateOf(false)
        var like by mutableIntStateOf(0)
        lateinit var root: View
        var expectedColor = 0
        compose.setContent {
            TestTheme(style, dark) {
                root = LocalView.current.rootView
                expectedColor = MiuixTheme.colorScheme.onBackground.toArgb()
                FeedCard(isFeedContent = false,
                    data = original.copy(likenum = if (like == 1) "13" else "12", userAction = original.userAction!!.copy(like = like)),
                    onViewUser = {}, onViewFeed = { _, _ -> }, onOpenLink = { _, _ -> }, onCopyText = {},
                    onBlockUser = {}, onLike = { _, _, _ -> like = 1 - like })
            }
        }
        lateinit var originalText: TextView
        compose.runOnIdle {
            originalText = findText(root, "正文颜色检查")!!
            assertEquals(expectedColor, originalText.currentTextColor)
            dark = true
        }
        compose.runOnIdle {
            val current = findText(root, "正文颜色检查")!!
            assertSame(originalText, current)
            assertEquals(expectedColor, current.currentTextColor)
        }
        compose.onNodeWithTag("feed-like-feed-1").performClick().assertIsSelected()
        compose.runOnIdle { style = UiStyle.MD3 }
        compose.onNodeWithTag("miuix-feed-feed-1").assertDoesNotExist()
        compose.runOnIdle { style = UiStyle.MIUIX }
        compose.onNodeWithTag("feed-like-feed-1").assertIsSelected()
        assertEquals(1, like)
    }

    @Test
    fun commentFiltersAndSingleReplyPreviewKeepTheirBehavior() {
        var selected by mutableIntStateOf(0)
        compose.setContent {
            TestTheme {
                Column {
                    FeedReplySortCard(replyCount = "1", selected = selected, updateSortReply = { selected = it })
                    FeedReplyCard(data = comment().copy(replyRowsMore = 0, replynum = "1"), onViewUser = {},
                        onOpenLink = { _, _ -> }, onCopyText = {}, onBlockUser = { _, _ -> })
                }
            }
        }
        compose.onNodeWithTag("miuix-comment-sort").assertExists()
        compose.onNodeWithText("热门").performClick()
        assertEquals(2, selected)
        compose.onNodeWithTag("view-all-replies").assertDoesNotExist()
    }

    @Composable
    private fun TestTheme(style: UiStyle = UiStyle.MIUIX, dark: Boolean = false, content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalUserPreferences provides UserPreferencesCompat.default().copy(uiStyle = style)) {
            C001apkComposeTheme(uiStyle = style, darkTheme = dark, materialYou = false, content = content)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun fullReplySheetUsesMiuixAndSupportsScrollingAndClosing() {
        var visible by mutableStateOf(true)
        compose.setContent {
            TestTheme {
                if (visible) ModalBottomSheet(onDismissRequest = { visible = false }, title = "全部回复") {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(12) { index ->
                            FeedReplyCard(data = comment().copy(id = "sheet-$index"), isTotalReply = true,
                                onViewUser = {}, onOpenLink = { _, _ -> }, onCopyText = {}, onBlockUser = { _, _ -> })
                        }
                    }
                }
            }
        }
        compose.onNodeWithTag("miuix-sheet").assertExists()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(8)
        compose.onNodeWithTag("miuix-comment-sheet-8").assertExists()
        compose.onNodeWithContentDescription("关闭").performClick()
        compose.onNodeWithTag("miuix-sheet").assertDoesNotExist()
        assertFalse(visible)
        compose.runOnIdle { visible = true }
        compose.onNodeWithTag("miuix-sheet").assertExists()
        androidx.test.espresso.Espresso.pressBack()
        compose.onNodeWithTag("miuix-sheet").assertDoesNotExist()
        assertFalse(visible)
    }

    @Test
    fun forwardedPostWithEmptyTitleStillShowsItsBody() {
        lateinit var root: View
        val forwarded = Gson().fromJson("""{
            "id":"forward-1", "uid":"source", "username":"转发作者", "message_title":"",
            "message":"需要保留的转发正文", "url":"coolmarket://feed/forward-1"
        }""", HomeFeedResponse.ForwardSourceFeed::class.java)
        compose.setContent {
            TestTheme {
                root = LocalView.current.rootView
                FeedCard(isFeedContent = false, data = feed().copy(forwardSourceType = "feed", forwardSourceFeed = forwarded),
                    onViewUser = {}, onViewFeed = { _, _ -> }, onOpenLink = { _, _ -> }, onCopyText = {}, onBlockUser = {})
            }
        }
        compose.runOnIdle { assertNotNull(findText(root, "@转发作者：需要保留的转发正文")) }
    }

    @Test
    fun captureContentPreviewInBothThemes() {
        var dark by mutableStateOf(false)
        compose.setContent {
            TestTheme(dark = dark) {
                LazyColumn(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)
                    .statusBarsPadding().navigationBarsPadding(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        top.yukonga.miuix.kmp.basic.Text("miuix 动态与评论预览", Modifier.padding(horizontal = 20.dp),
                            fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface)
                    }
                    item {
                        FeedCard(modifier = Modifier.padding(horizontal = 12.dp), isFeedContent = false,
                            data = feed().copy(messageTitle = "原生 miuix 内容卡片", message = "动态、评论和楼中楼使用独立布局，保留原有操作与内容。"),
                            onViewUser = {}, onViewFeed = { _, _ -> }, onOpenLink = { _, _ -> }, onCopyText = {}, onBlockUser = {})
                    }
                    item { FeedReplySortCard(replyCount = "12", selected = 0, updateSortReply = {}) }
                    item {
                        FeedReplyCard(data = comment(), onViewUser = {}, onOpenLink = { _, _ -> },
                            onCopyText = {}, onBlockUser = { _, _ -> })
                    }
                }
            }
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (theme in listOf("light", "dark")) {
            compose.runOnIdle { dark = theme == "dark" }
            val image = compose.onRoot().captureToImage().asAndroidBitmap()
            File(context.cacheDir, "miuix-content-$theme.png").outputStream().use {
                image.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }

    private fun feed() = Gson().fromJson("""{
        "id":"feed-1", "entityType":"feed", "uid":"author", "username":"动态作者",
        "message":"正文颜色检查", "replynum":"3", "likenum":"12", "dateline":1700000000,
        "userAction":{"like":0}
    }""", HomeFeedResponse.Data::class.java)

    private fun comment() = Gson().fromJson("""{
        "id":"comment-1", "entityType":"feed_reply", "fetchType":"feed_reply",
        "uid":"parent-user", "username":"主评论作者", "message":"这是一条主评论",
        "dateline":1700000000, "replynum":"2", "likenum":"5", "replyRowsMore":1,
        "userAction":{"like":0}, "replyRows":[{
            "id":"child-1", "uid":"child-user", "username":"子回复作者",
            "message":"<a class=\"feed-link-uname\" href=\"/u/child-user\">子回复作者</a>回复: 子回复正文",
            "ruid":"parent-user", "rusername":"主评论作者", "dateline":1700000000
        }]
    }""", HomeFeedResponse.Data::class.java)

    private fun findText(view: View, text: String): TextView? {
        if (view is TextView && view.text.toString() == text) return view
        if (view is ViewGroup) for (index in 0 until view.childCount) {
            findText(view.getChildAt(index), text)?.let { return it }
        }
        return null
    }
}
