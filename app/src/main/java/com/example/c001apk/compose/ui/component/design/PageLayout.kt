package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    containerColor: Color = Color.Unspecified,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val miuix = isMiuix()
    CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides contentColor) {
        top.yukonga.miuix.kmp.basic.Scaffold(
            modifier = modifier, topBar = topBar, bottomBar = bottomBar, snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton, contentWindowInsets = contentWindowInsets,
            containerColor = if (containerColor != Color.Unspecified) containerColor
                else if (miuix) MiuixTheme.colorScheme.surface else MaterialTheme.colorScheme.background,
        ) { padding ->
            Box(Modifier.consumeWindowInsets(padding)) { content(padding) }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val currentContent by rememberUpdatedState(content)
    val body = remember { movableContentOf { Box(Modifier.fillMaxSize()) { currentContent() } } }
    if (isMiuix()) top.yukonga.miuix.kmp.basic.PullToRefresh(
        isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier,
        color = MiuixTheme.colorScheme.primary,
        refreshTexts = listOf("下拉刷新", "松开刷新", "正在刷新", "刷新完成"),
        refreshTextStyle = MiuixTheme.textStyles.footnote1.copy(color = MiuixTheme.colorScheme.onSurfaceVariantSummary),
    ) { body() }
    else androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier,
    ) { body() }
}
