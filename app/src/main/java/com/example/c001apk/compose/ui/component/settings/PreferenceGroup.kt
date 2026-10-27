package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val LocalPreferenceGroup = compositionLocalOf { false }

@Composable
fun PreferenceGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    if (isMiuix()) {
        Text(title, Modifier.padding(start = 26.dp, top = 24.dp, bottom = 8.dp),
            fontSize = 13.sp, fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.primary)
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("preference-group-$title"),
            cornerRadius = 20.dp, insideMargin = PaddingValues(vertical = 4.dp)) {
            CompositionLocalProvider(LocalPreferenceGroup provides true) { content() }
        }
    } else {
        BasicListItem(leadingText = title)
        Column(content = content)
    }
}

@Composable
internal fun PreferenceFrame(content: @Composable () -> Unit) {
    if (LocalPreferenceGroup.current) content()
    else Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        cornerRadius = 16.dp, insideMargin = PaddingValues(0.dp)) { content() }
}
