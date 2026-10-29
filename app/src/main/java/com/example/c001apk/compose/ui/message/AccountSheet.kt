package com.example.c001apk.compose.ui.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.c001apk.compose.SavedAccount
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.design.*
import com.example.c001apk.compose.util.decode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(onDismiss: () -> Unit, onLogin: () -> Unit) {
    val prefs = LocalUserPreferences.current
    val viewModel = hiltViewModel<AccountViewModel>()
    var removing by remember { mutableStateOf<SavedAccount?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss, title = "账号管理") {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!com.example.c001apk.compose.ui.theme.isMiuix()) Text("账号管理", style = MaterialTheme.typography.titleLarge)
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(prefs.accounts, key = { it.uid }) { account ->
                    Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
                        .clickable(enabled = !viewModel.busy) {
                            if (account.encryptedToken.isEmpty()) { onDismiss(); onLogin() }
                            else viewModel.switchTo(account.uid)
                        }.padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CoilLoader(url = account.avatar, modifier = Modifier.size(44.dp).clip(CircleShape))
                        Column(Modifier.weight(1f)) {
                            Text(runCatching { account.username.decode }.getOrDefault(account.username), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(if (prefs.isLogin && prefs.uid == account.uid) "当前账号" else if (account.encryptedToken.isEmpty()) "登录已失效，点击重新登录" else "UID: ${account.uid}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { removing = account }, enabled = !viewModel.busy) { Text("移除") }
                    }
                }
            }
            if (prefs.accounts.isEmpty()) Text("添加账号后，可在这里快速切换。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            viewModel.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { onDismiss(); onLogin() }, enabled = !viewModel.busy, modifier = Modifier.fillMaxWidth()) {
                Text(if (viewModel.busy) "正在处理…" else "添加账号")
            }
            if (prefs.isLogin) TextButton(onClick = { removing = prefs.accounts.find { it.uid == prefs.uid } },
                enabled = !viewModel.busy, modifier = Modifier.fillMaxWidth()) { Text("退出当前账号") }
            Spacer(Modifier.height(16.dp))
        }
    }
    removing?.let { account ->
        AlertDialog(onDismissRequest = { removing = null }, title = { Text("移除账号？") },
            text = { Text("将移除本机保存的登录信息，再次使用需要重新登录。") },
            confirmButton = { TextButton(onClick = { removing = null; viewModel.remove(account.uid) }) { Text("移除") } },
            dismissButton = { TextButton(onClick = { removing = null }) { Text("取消") } })
    }
}
