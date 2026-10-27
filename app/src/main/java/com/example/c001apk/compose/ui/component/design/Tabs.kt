package com.example.c001apk.compose.ui.component.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.mocharealm.gaze.capsule.ContinuousRoundedRectangle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    indicator: @Composable TabIndicatorScope.() -> Unit = {},
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit,
) {
    val miuix = isMiuix()
    androidx.compose.material3.SecondaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = if (miuix) modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(ContinuousRoundedRectangle(14.dp)) else modifier,
        containerColor = if (miuix) MiuixTheme.colorScheme.secondaryContainer else containerColor,
        contentColor = contentColor,
        indicator = { if (!miuix) indicator() },
        divider = { if (!miuix) divider() },
        tabs = tabs,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    indicator: @Composable TabIndicatorScope.() -> Unit = {},
    divider: @Composable () -> Unit = {},
    tabs: @Composable () -> Unit,
) {
    val miuix = isMiuix()
    androidx.compose.material3.SecondaryScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = if (miuix) modifier.padding(vertical = 6.dp).clip(ContinuousRoundedRectangle(14.dp)) else modifier,
        containerColor = if (miuix) MiuixTheme.colorScheme.secondaryContainer else containerColor,
        contentColor = contentColor, edgePadding = if (miuix) 8.dp else edgePadding,
        indicator = { if (!miuix) indicator() }, divider = { if (!miuix) divider() }, tabs = tabs,
    )
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
) {
    if (isMiuix()) {
        val background by animateColorAsState(
            if (selected) MiuixTheme.colorScheme.background else Color.Transparent, label = "tabSelection",
        )
        top.yukonga.miuix.kmp.basic.Surface(
            modifier = modifier.padding(4.dp).clip(ContinuousRoundedRectangle(10.dp))
                .selectable(selected, enabled = enabled, role = Role.Tab, onClick = onClick),
            shape = ContinuousRoundedRectangle(10.dp), color = background,
        ) {
            CompositionLocalProvider(LocalContentColor provides if (selected) selectedContentColor else unselectedContentColor) {
                Column(Modifier.fillMaxWidth().heightIn(min = 40.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    icon?.invoke()
                    text?.invoke()
                }
            }
        }
    } else {
        androidx.compose.material3.Tab(selected, onClick, modifier, enabled, text, icon,
            selectedContentColor = selectedContentColor, unselectedContentColor = unselectedContentColor)
    }
}
