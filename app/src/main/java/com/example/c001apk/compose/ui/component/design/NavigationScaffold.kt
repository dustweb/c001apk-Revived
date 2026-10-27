package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem

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
                    MiuixNavigationBar {
                        destinations.forEachIndexed { index, destination ->
                            MiuixNavigationBarItem(
                                selected = selected == index, onClick = { onSelect(index) },
                                icon = if (selected == index) destination.selectedIcon else destination.icon,
                                label = destination.label,
                                badge = if (index == 1 && badge > 0) ({ NativeBadge(badge) }) else null,
                            )
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
                    top.yukonga.miuix.kmp.basic.NavigationRail {
                        destinations.forEachIndexed { index, destination ->
                            top.yukonga.miuix.kmp.basic.NavigationRailItem(
                                selected = selected == index, onClick = { onSelect(index) },
                                icon = if (selected == index) destination.selectedIcon else destination.icon,
                                label = destination.label,
                                badge = if (index == 1 && badge > 0) ({ NativeBadge(badge) }) else null,
                            )
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

@Composable
private fun NativeBadge(count: Int) {
    top.yukonga.miuix.kmp.basic.Badge {
        top.yukonga.miuix.kmp.basic.Text(if (count > 99) "99+" else count.toString())
    }
}
