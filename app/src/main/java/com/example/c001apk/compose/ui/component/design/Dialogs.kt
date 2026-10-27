package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    if (isMiuix()) {
        var visible by remember { mutableStateOf(true) }
        WindowDialog(show = visible, modifier = modifier.semantics { dialog() },
            onDismissRequest = { visible = false }, onDismissFinished = onDismissRequest) {
            CompositionLocalProvider(LocalContentColor provides MiuixTheme.colorScheme.onSurface) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    icon?.invoke()
                    title?.let {
                        CompositionLocalProvider(LocalTextStyle provides MiuixTheme.textStyles.title4) { it() }
                    }
                    text?.invoke()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    ) {
                        dismissButton?.invoke()
                        CompositionLocalProvider(LocalPrimaryDialogAction provides true) { confirmButton() }
                    }
                }
            }
        }
    } else {
        androidx.compose.material3.AlertDialog(onDismissRequest, confirmButton, modifier,
            dismissButton = dismissButton, icon = icon, title = title, text = text, properties = properties)
    }
}

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    scrollState: ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isMiuix()) {
        OverlayListPopup(show = expanded, popupModifier = modifier, onDismissRequest = onDismissRequest) {
            CompositionLocalProvider(
                LocalContentColor provides MiuixTheme.colorScheme.onSurface,
                LocalTextStyle provides MiuixTheme.textStyles.main,
            ) {
                Column(content = content)
            }
        }
    } else {
        androidx.compose.material3.DropdownMenu(expanded, onDismissRequest, modifier,
            offset = offset, scrollState = scrollState, content = content)
    }
}

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.Surface(onClick = onClick, enabled = enabled, modifier = modifier,
            color = Color.Transparent) {
            Row(modifier = Modifier.alpha(if (enabled) 1f else 0.38f).padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                leadingIcon?.invoke()
                text()
                trailingIcon?.invoke()
            }
        }
    } else {
        androidx.compose.material3.DropdownMenuItem(text, onClick, modifier,
            leadingIcon = leadingIcon, trailingIcon = trailingIcon, enabled = enabled)
    }
}
