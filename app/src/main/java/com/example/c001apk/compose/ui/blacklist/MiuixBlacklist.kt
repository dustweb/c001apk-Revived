package com.example.c001apk.compose.ui.blacklist

import com.example.c001apk.compose.ui.component.design.pageScroll
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.settings.PreferenceTopBar
import com.example.c001apk.compose.ui.component.design.Scaffold
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixBlacklist(type: String, entries: List<String>, input: TextFieldValue,
    onInput: (TextFieldValue) -> Unit, onAdd: () -> Unit, onOpen: (String) -> Unit,
    onRemove: (String) -> Unit, onClear: () -> Unit, onImport: () -> Unit, onExport: () -> Unit, onBack: () -> Unit) {
    val user = type == BlackListType.USER.name
    val scroll = MiuixScrollBehavior()
    Scaffold(Modifier.fillMaxSize().nestedScroll(scroll.nestedScrollConnection).testTag("miuix-blacklist"),
        topBar = { PreferenceTopBar(if (user) "用户黑名单" else "话题黑名单", scroll, onBack) }) { padding ->
        Column(Modifier.fillMaxSize().imePadding().pageScroll(rememberScrollState(), padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), insideMargin = PaddingValues(16.dp)) {
                TextField(value = input, onValueChange = onInput, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("blacklist-input"),
                    label = if (user) "添加用户 UID" else "添加话题", useLabelAsPlaceholder = true,
                    colors = TextFieldDefaults.textFieldColors(borderColor = Color.Transparent),
                    keyboardOptions = KeyboardOptions(keyboardType = if (user) KeyboardType.Number else KeyboardType.Text,
                        imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onAdd() }))
                Spacer(Modifier.height(12.dp))
                TextButton("添加", onAdd, Modifier.fillMaxWidth(), enabled = input.text.isNotBlank(),
                    colors = ButtonDefaults.textButtonColorsPrimary())
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton("导入", onImport, Modifier.weight(1f))
                    TextButton("导出", onExport, Modifier.weight(1f), enabled = entries.isNotEmpty())
                }
            }
            SmallTitle("已屏蔽 ${entries.size} ${if (user) "位用户" else "个话题"}")
            Card(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                if (entries.isEmpty()) BasicComponent(title = "暂无黑名单", summary = "添加后可在这里管理")
                entries.forEach { value ->
                    BasicComponent(title = if (user) "UID $value" else value,
                        modifier = Modifier.combinedClickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null,
                            onClick = { onOpen(value) }, onLongClick = { onRemove(value) }),
                        endActions = { IconButton(onClick = { onRemove(value) }) {
                            Icon(MiuixIcons.Medium.Delete, "移除 $value", tint = MiuixTheme.colorScheme.onSurfaceVariantActions)
                        } })
                }
            }
            if (entries.isNotEmpty()) TextButton("清空黑名单", onClear,
                Modifier.padding(horizontal = 16.dp).fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }
    }
}
