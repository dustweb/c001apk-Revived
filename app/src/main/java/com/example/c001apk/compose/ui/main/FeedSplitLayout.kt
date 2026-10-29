package com.example.c001apk.compose.ui.main

import androidx.compose.animation.core.spring
import top.yukonga.miuix.kmp.nav.gesture.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import kotlin.math.roundToInt
import androidx.activity.compose.BackHandler
import com.example.c001apk.compose.ui.theme.isMiuix

@Composable
internal fun FeedSplitLayout(
    showDetail: Boolean,
    detailContent: @Composable () -> Unit,
    mainBackground: @Composable () -> Unit = {},
    canHandleBack: Boolean = false,
    onClose: () -> Unit = {},
    mainContent: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(if (showDetail) 1f else 0f) }
    LaunchedEffect(showDetail) {
        progress.animateTo(if (showDetail) 1f else 0f,
            spring(dampingRatio = 1f, stiffness = 350f))
    }
    val keepDetail = showDetail || progress.value > 0f

    Layout(
        modifier = Modifier.fillMaxSize().clipToBounds(),
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                mainBackground()
                mainContent()
            }
            if (keepDetail) {
                Box(modifier = Modifier.fillMaxSize()) { detailContent() }
            }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val detailWidth = width / 2
        val mainWidth = width - (detailWidth * progress.value.coerceIn(0f, 1f)).roundToInt()
        val main = measurables[0].measure(
            constraints.copy(minWidth = mainWidth, maxWidth = mainWidth)
        )
        // 详情保持半屏宽度滑入，避免动画中反复压缩文本与图片。
        val detail = measurables.getOrNull(1)?.measure(
            constraints.copy(minWidth = detailWidth, maxWidth = detailWidth)
        )
        layout(width, constraints.maxHeight) {
            main.placeRelative(0, 0)
            detail?.placeRelative(mainWidth, 0)
        }
    }
    if (isMiuix()) PredictiveBackHandler(
        enabled = showDetail && canHandleBack,
        onProgress = { events -> events.collect { progress.snapTo(1f - it.progress) } },
        onCommit = { scope.launch { progress.animateTo(0f); onClose() } },
        onCancel = { scope.launch { progress.animateTo(1f) } },
    ) else BackHandler(showDetail && canHandleBack, onClose)
}
