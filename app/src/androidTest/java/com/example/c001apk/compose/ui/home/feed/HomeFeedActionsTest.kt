package com.example.c001apk.compose.ui.home.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class HomeFeedActionsTest {
    @get:Rule val compose = createComposeRule()
    @Test fun shortScrollDoesNotShowBackToTopAndReturningHidesIt() = checkThreshold(tallFirstPost = false)
    @Test fun aLongFirstPostUsesPixelDistanceInsteadOfItemIndex() = checkThreshold(tallFirstPost = true)

    private fun checkThreshold(tallFirstPost: Boolean) {
        lateinit var state: LazyListState
        var visible = false
        compose.setContent {
            state = rememberLazyListState()
            visible = rememberBackToTopVisible(state)
            Box(Modifier.height(400.dp).fillMaxWidth()) {
                LazyColumn(state = state) {
                    items(30) { index ->
                        Text("动态 $index", Modifier.height(if (tallFirstPost && index == 0) 1800.dp else 120.dp))
                    }
                }
            }
        }
        compose.waitForIdle()
        val viewport = state.layoutInfo.viewportEndOffset - state.layoutInfo.viewportStartOffset
        runBlocking(Dispatchers.Main) { state.scrollBy(viewport * .3f) }
        compose.waitForIdle()
        compose.runOnIdle { assertFalse(visible) }
        runBlocking(Dispatchers.Main) { state.scrollBy(viewport * 2f) }
        compose.waitForIdle()
        compose.runOnIdle { assertTrue(visible) }
        runBlocking(Dispatchers.Main) { state.scrollToItem(0) }
        compose.waitForIdle()
        compose.runOnIdle { assertFalse(visible) }
    }
}
