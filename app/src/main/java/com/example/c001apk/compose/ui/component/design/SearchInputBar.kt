package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.BackButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Search
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SearchInputBar(query: String, onQueryChange: (String) -> Unit, onSearch: (String) -> Unit,
    hint: String, onBack: () -> Unit, focusRequester: FocusRequester) {
    Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
        .padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        BackButton(onBack)
        InputField(query = query, onQueryChange = onQueryChange, onSearch = onSearch,
            expanded = true, onExpandedChange = {}, label = hint,
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            trailingIcon = if (query.isNotEmpty()) ({
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(MiuixIcons.Basic.Close, "清空搜索词", tint = MiuixTheme.colorScheme.onSurfaceVariantActions)
                }
            }) else null)
        IconButton(onClick = { onSearch(query) }) {
            Icon(MiuixIcons.Basic.Search, "搜索", tint = MiuixTheme.colorScheme.onSurface)
        }
    }
}
