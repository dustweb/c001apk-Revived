package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    if (isMiuix()) {
        SideEffect {
            scrollBehavior?.state?.let {
                it.heightOffsetLimit = 0f
                it.heightOffset = 0f
            }
        }
        top.yukonga.miuix.kmp.basic.Surface(
            modifier = modifier, color = colors?.containerColor ?: MiuixTheme.colorScheme.surface,
        ) {
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(windowInsets).height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navigationIcon()
                Box(Modifier.weight(1f).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                    CompositionLocalProvider(
                        LocalContentColor provides MiuixTheme.colorScheme.onSurface,
                        LocalTextStyle provides MiuixTheme.textStyles.title3.copy(color = MiuixTheme.colorScheme.onSurface),
                        content = title,
                    )
                }
                actions()
            }
        }
    } else {
        androidx.compose.material3.TopAppBar(title, modifier, navigationIcon, actions,
            windowInsets = windowInsets, colors = colors ?: TopAppBarDefaults.topAppBarColors(), scrollBehavior = scrollBehavior)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    if (isMiuix()) {
        SideEffect { scrollBehavior?.state?.let { it.heightOffsetLimit = 0f; it.heightOffset = 0f } }
        top.yukonga.miuix.kmp.basic.SmallTopAppBar(title = title,
            modifier = modifier.windowInsetsPadding(windowInsets),
            defaultWindowInsetsPadding = false, navigationIcon = navigationIcon, actions = actions,
            color = colors?.containerColor ?: MiuixTheme.colorScheme.surface)
    } else androidx.compose.material3.TopAppBar(title = { androidx.compose.material3.Text(title) },
        modifier = modifier, navigationIcon = navigationIcon, actions = actions,
        windowInsets = windowInsets, colors = colors ?: TopAppBarDefaults.topAppBarColors(), scrollBehavior = scrollBehavior)
}

@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.elevatedShape,
    colors: CardColors? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val miuix = isMiuix()
    val resolved = colors ?: if (miuix) null else CardDefaults.elevatedCardColors()
    val nativeColors = resolved?.let {
        top.yukonga.miuix.kmp.basic.CardColors(it.containerColor, it.contentColor)
    } ?: top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors()
    top.yukonga.miuix.kmp.basic.Card(
        modifier = if (miuix) modifier else modifier.shadow(1.dp, shape).clip(shape),
        cornerRadius = if (miuix) top.yukonga.miuix.kmp.basic.CardDefaults.CornerRadius else 0.dp,
        colors = nativeColors,
    ) {
        CompositionLocalProvider(LocalContentColor provides nativeColors.contentColor) { content() }
    }
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
) {
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.CircularProgressIndicator(modifier = modifier, strokeWidth = strokeWidth)
    } else {
        androidx.compose.material3.CircularProgressIndicator(modifier = modifier,
            strokeWidth = strokeWidth, strokeCap = strokeCap)
    }
}

@Composable
fun LinearProgressIndicator(progress: () -> Float, modifier: Modifier = Modifier) {
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.LinearProgressIndicator(progress = progress(), modifier = modifier)
    } else {
        androidx.compose.material3.LinearProgressIndicator(progress = progress, modifier = modifier)
    }
}

@Composable
fun LinearProgressIndicator(modifier: Modifier = Modifier) {
    if (isMiuix()) top.yukonga.miuix.kmp.basic.LinearProgressIndicator(modifier = modifier)
    else androidx.compose.material3.LinearProgressIndicator(modifier = modifier)
}

@Composable
fun ListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.BasicComponent(
            modifier = modifier,
            startAction = leadingContent?.let { { Box(Modifier.padding(end = 12.dp)) { it() } } },
            endActions = { trailingContent?.invoke() },
        ) {
            overlineContent?.invoke()
            headlineContent()
            CompositionLocalProvider(LocalContentColor provides MiuixTheme.colorScheme.onSurfaceVariantSummary) {
                supportingContent?.invoke()
            }
        }
    } else {
        androidx.compose.material3.ListItem(headlineContent, modifier, overlineContent,
            supportingContent, leadingContent, trailingContent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isMiuix()) {
        var visible by remember { mutableStateOf(true) }
        val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.88f
        WindowBottomSheet(
            show = visible, title = title,
            onDismissRequest = { visible = false }, onDismissFinished = onDismissRequest,
            modifier = modifier.testTag("miuix-sheet").semantics { dialog() },
            insideMargin = DpSize(0.dp, 8.dp),
            endAction = {
                top.yukonga.miuix.kmp.basic.IconButton(onClick = { visible = false }) {
                    top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Close, "关闭")
                }
            },
        ) {
            Column(Modifier.fillMaxWidth().heightIn(max = maxHeight), content = content)
        }
        return
    }
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        content = content,
    )
}
