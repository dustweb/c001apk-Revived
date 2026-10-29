package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.logic.network.SzlmProtocol
import com.example.c001apk.compose.ui.component.design.*

@Composable
fun DeviceIdentityDialog(value: String, viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    var input by rememberSaveable { mutableStateOf(value) }
    LaunchedEffect(value) { input = value }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("数字联盟 ID") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth(),
                    placeholderText = "输入 ID 或粘贴官方复制内容", maxLines = 4)
                TextButton(onClick = viewModel::acquireDeviceId, enabled = !viewModel.acquiringDeviceId,
                    modifier = Modifier.fillMaxWidth()) { Text(if (viewModel.acquiringDeviceId) "正在获取…" else "自动获取并保存") }
                viewModel.deviceIdMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.saveDeviceId(input, onDismiss) },
            enabled = !viewModel.acquiringDeviceId && (input.isBlank() || SzlmProtocol.parseId(input) != null)) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } })
}
