package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun ActionDialog(title: String, actions: List<MenuAction>, onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<(() -> Unit)?>(null) }
    if (isMiuix()) {
        WindowDialog(show = visible, title = title, modifier = Modifier.testTag("miuix-action-dialog"),
            onDismissRequest = { visible = false },
            onDismissFinished = { onDismiss(); selected?.invoke() }) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                actions.forEach { action ->
                    BasicComponent(title = action.title, onClick = { selected = action.onClick; visible = false })
                }
            }
        }
    } else AlertDialog(onDismissRequest = onDismiss, title = { androidx.compose.material3.Text(title) },
        confirmButton = {}, text = {
            Column {
                actions.forEach { action ->
                    TextButton(onClick = { onDismiss(); action.onClick() }, modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.material3.Text(action.title)
                    }
                }
            }
        })
}

@Composable
fun ProgressContent() {
    Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ProgressDialog(message: String) {
    if (isMiuix()) WindowDialog(show = true, title = message, onDismissRequest = null) {
        ProgressContent()
    } else AlertDialog(onDismissRequest = {}, confirmButton = {}, title = { androidx.compose.material3.Text(message) },
        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        text = { CircularProgressIndicator() })
}
