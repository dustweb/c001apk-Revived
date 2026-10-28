package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

internal val LocalDialogAction = compositionLocalOf { false }

/** Equal-width actions share a row; additional actions use full-width rows. */
@Composable
internal fun DialogActions(actions: List<@Composable () -> Unit>) {
    if (actions.isEmpty()) return
    CompositionLocalProvider(LocalDialogAction provides true) {
        if (actions.size <= 2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                actions.forEach { action -> Box(Modifier.weight(1f)) { action() } }
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                actions.forEach { it() }
            }
        }
    }
}
