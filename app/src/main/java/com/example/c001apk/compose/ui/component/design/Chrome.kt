package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Cancel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
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
            modifier = modifier, color = MiuixTheme.colorScheme.surface,
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
                        LocalTextStyle provides MaterialTheme.typography.titleLarge.copy(
                            fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                        ),
                        content = title,
                    )
                }
                actions()
            }
        }
    } else {
        androidx.compose.material3.TopAppBar(title, modifier, navigationIcon, actions,
            windowInsets = windowInsets, colors = colors, scrollBehavior = scrollBehavior)
    }
}

@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.elevatedShape,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.Card(
            modifier = modifier, insideMargin = PaddingValues(0.dp),
            colors = top.yukonga.miuix.kmp.basic.CardColors(colors.containerColor, colors.contentColor),
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.contentColor) { content() }
        }
    } else {
        androidx.compose.material3.ElevatedCard(modifier, shape, colors, content = content)
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
            leftAction = leadingContent?.let { { Box(Modifier.padding(end = 12.dp)) { it() } } },
            rightActions = { trailingContent?.invoke() },
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
        val visible = remember { mutableStateOf(true) }
        val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.88f
        SuperDialog(show = visible, onDismissRequest = onDismissRequest,
            modifier = modifier.heightIn(max = maxHeight).testTag("miuix-sheet").semantics { dialog() },
            insideMargin = DpSize(0.dp, 8.dp)) {
            Column(Modifier.fillMaxWidth().heightIn(max = maxHeight)) {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    top.yukonga.miuix.kmp.basic.Text(title.orEmpty(), Modifier.weight(1f),
                        style = MiuixTheme.textStyles.title4, color = MiuixTheme.colorScheme.onBackground)
                    top.yukonga.miuix.kmp.basic.IconButton(onClick = onDismissRequest) {
                        top.yukonga.miuix.kmp.basic.Icon(MiuixIcons.Useful.Cancel, "关闭",
                            Modifier.size(22.dp), tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    }
                }
                Column(Modifier.fillMaxWidth().weight(1f, fill = false), content = content)
            }
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
