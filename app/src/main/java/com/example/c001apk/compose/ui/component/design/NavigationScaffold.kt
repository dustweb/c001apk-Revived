package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class NavigationDestination(val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

@Composable
fun AppNavigationScaffold(
    selected: Int,
    destinations: List<NavigationDestination>,
    compact: Boolean,
    badge: Int,
    onSelect: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    val miuix = isMiuix()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (compact) {
                if (miuix) {
                    Box {
                        MiuixNavigationBar(
                            items = destinations.mapIndexed { index, destination ->
                                NavigationItem(destination.label,
                                    if (selected == index) destination.selectedIcon else destination.icon)
                            },
                            selected = selected, onClick = onSelect,
                        )
                        if (badge > 0) {
                            Badge(Modifier.align(Alignment.TopCenter).padding(start = 30.dp, top = 5.dp)) {
                                Text(if (badge > 99) "99+" else badge.toString())
                            }
                        }
                    }
                } else {
                    NavigationBar {
                        destinations.forEachIndexed { index, destination ->
                            NavigationBarItem(
                                selected = selected == index,
                                onClick = { onSelect(index) },
                                icon = { DestinationIcon(destination, selected == index, if (index == 1) badge else 0) },
                                label = { Text(destination.label) },
                                alwaysShowLabel = false,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        // Only navigation controls change implementation; the page keeps its composition position.
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (!compact) {
                if (miuix) {
                    Column(
                        Modifier.width(88.dp).fillMaxHeight().background(MiuixTheme.colorScheme.surfaceContainer)
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Vertical)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Spacer(Modifier.height(12.dp))
                        destinations.forEachIndexed { index, destination ->
                            top.yukonga.miuix.kmp.basic.Surface(
                                modifier = Modifier.padding(horizontal = 6.dp).fillMaxWidth()
                                    .selectable(selected == index, role = Role.Tab, onClick = { onSelect(index) }),
                                shape = RoundedCornerShape(18.dp),
                                color = if (selected == index) MiuixTheme.colorScheme.tertiaryContainer
                                    else Color.Transparent,
                            ) {
                                Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    DestinationIcon(destination, selected == index, if (index == 1) badge else 0)
                                    Text(destination.label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                } else {
                    NavigationRail {
                        destinations.forEachIndexed { index, destination ->
                            NavigationRailItem(
                                selected = selected == index, onClick = { onSelect(index) },
                                icon = { DestinationIcon(destination, selected == index, if (index == 1) badge else 0) },
                                label = { Text(destination.label) }, alwaysShowLabel = false,
                            )
                        }
                    }
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) { content() }
        }
    }
}

@Composable
private fun DestinationIcon(destination: NavigationDestination, selected: Boolean, badge: Int) {
    BadgedBox(badge = { if (badge > 0) Badge { Text(if (badge > 99) "99+" else badge.toString()) } }) {
        Icon(if (selected) destination.selectedIcon else destination.icon, contentDescription = null)
    }
}
