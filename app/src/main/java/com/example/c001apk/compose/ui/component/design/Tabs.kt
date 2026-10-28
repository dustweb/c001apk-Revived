package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.theme.isMiuix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryTabRow(
    selectedTabIndex: Int,
    labels: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    indicator: @Composable TabIndicatorScope.() -> Unit = {},
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit,
) {
    if (isMiuix()) NativeTabs(labels, selectedTabIndex, onTabSelected, modifier)
    else androidx.compose.material3.SecondaryTabRow(
        selectedTabIndex = selectedTabIndex, modifier = modifier,
        containerColor = containerColor, contentColor = contentColor,
        indicator = indicator, divider = divider, tabs = tabs,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryScrollableTabRow(
    selectedTabIndex: Int,
    labels: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    indicator: @Composable TabIndicatorScope.() -> Unit = {},
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit,
) {
    if (isMiuix()) NativeTabs(labels, selectedTabIndex, onTabSelected, modifier)
    else androidx.compose.material3.SecondaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex, modifier = modifier,
        containerColor = containerColor, contentColor = contentColor,
        edgePadding = edgePadding, indicator = indicator, divider = divider, tabs = tabs,
    )
}

@Composable
private fun NativeTabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier) {
    val haptic = rememberHapticClick {}
    FilledTabs(labels, selected, { haptic(); onSelect(it) }, modifier.padding(horizontal = 12.dp, vertical = 6.dp))
}

@Composable
fun FilledTabs(tabs: List<String>, selectedTabIndex: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    if (tabs.isEmpty()) return
    BoxWithConstraints(modifier.fillMaxWidth()) {
        top.yukonga.miuix.kmp.basic.TabRowWithContour(
            tabs = tabs,
            selectedTabIndex = selectedTabIndex.coerceIn(tabs.indices),
            onTabSelected = onTabSelected,
            maxWidth = maxWidth.coerceAtLeast(top.yukonga.miuix.kmp.basic.TabRowDefaults.TabRowWithContourMinWidth),
            height = 52.dp,
            cornerRadius = top.yukonga.miuix.kmp.basic.TabRowDefaults.TabRowCornerRadius,
            colors = top.yukonga.miuix.kmp.basic.TabRowDefaults.tabRowColors(
                backgroundColor = Color.Transparent,
                selectedBackgroundColor = MiuixTheme.colorScheme.surfaceContainer,
                contentColor = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                selectedContentColor = MiuixTheme.colorScheme.onSurface,
            ),
        )
    }
}

@Composable
fun Tab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    selectedContentColor: Color = MaterialTheme.colorScheme.primary,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) = androidx.compose.material3.Tab(
    selected, onClick, modifier, enabled, text, icon,
    selectedContentColor = selectedContentColor, unselectedContentColor = unselectedContentColor,
)
