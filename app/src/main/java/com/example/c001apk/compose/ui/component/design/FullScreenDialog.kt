package com.example.c001apk.compose.ui.component.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import top.yukonga.miuix.kmp.utils.WindowNavigationEventScope

/** Full-page editors and comparison tables keep a platform window with their own page scaffold. */
@Composable
fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest, properties = properties) {
        WindowNavigationEventScope(content = content)
    }
}
