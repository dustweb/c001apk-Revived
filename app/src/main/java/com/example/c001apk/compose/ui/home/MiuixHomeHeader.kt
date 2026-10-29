package com.example.c001apk.compose.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.rememberHapticClick
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixHomeHeader(
    labels: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onSearch: () -> Unit,
    onEditTabs: () -> Unit,
) {
    val haptic = rememberHapticClick {}
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().height(TopAppBarDefaults.CollapsedHeight).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.size(48.dp))
            Box(
                Modifier.weight(1f).fillMaxHeight()
                    .clickable(onClickLabel = "编辑首页板块", onClick = rememberHapticClick(onClick = onEditTabs))
                    .semantics { contentDescription = "编辑首页板块" },
                contentAlignment = Alignment.Center,
            ) {
                Text("主页", style = MiuixTheme.textStyles.title3, fontWeight = FontWeight.Medium,
                    color = MiuixTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(modifier = Modifier.size(48.dp), onClick = rememberHapticClick(onClick = onSearch)) {
                Icon(MiuixIcons.Medium.Search, "搜索", Modifier.size(24.dp), tint = MiuixTheme.colorScheme.onSurface)
            }
        }
        if (labels.isNotEmpty()) BoxWithConstraints(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            TabRowWithContour(
                tabs = labels,
                selectedTabIndex = selectedTabIndex.coerceIn(labels.indices),
                onTabSelected = { haptic(); onTabSelected(it) },
                minWidth = 56.dp,
                maxWidth = maxWidth.coerceAtLeast(56.dp),
                colors = TabRowDefaults.tabRowColors(
                    backgroundColor = Color.Transparent,
                    selectedBackgroundColor = MiuixTheme.colorScheme.surfaceContainer,
                    contentColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    selectedContentColor = MiuixTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}
