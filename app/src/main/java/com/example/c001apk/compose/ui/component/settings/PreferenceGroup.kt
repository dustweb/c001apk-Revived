package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle

internal val LocalPreferenceGroup = compositionLocalOf { false }

@Composable
fun PreferenceGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    if (isMiuix()) {
        SmallTitle(title, Modifier.padding(top = 16.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("preference-group-$title")) {
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
    else Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) { content() }
}
