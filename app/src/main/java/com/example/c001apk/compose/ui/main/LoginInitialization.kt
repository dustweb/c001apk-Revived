package com.example.c001apk.compose.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.design.AlertDialog
import com.example.c001apk.compose.ui.component.design.CircularProgressIndicator
import com.example.c001apk.compose.ui.component.design.Text
import com.example.c001apk.compose.ui.component.design.TextButton

@Composable
internal fun LoginInitialization() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            CircularProgressIndicator()
            Text("登录成功，正在自动获取数字联盟 ID…")
        }
    }
}

@Composable
internal fun LoginDeviceIdNotice(notice: MainViewModel.LoginDeviceIdNotice, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (notice.success) "已自动获取数字联盟 ID" else "登录成功") },
        text = { Text(notice.message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } })
}
