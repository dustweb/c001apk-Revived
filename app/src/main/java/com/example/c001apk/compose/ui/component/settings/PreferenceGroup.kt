package com.example.c001apk.compose.ui.component.settings

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
fun PreferenceGroup(title: String, showTitleInMd3: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    val miuix = isMiuix()
    if (miuix) SmallTitle(title, Modifier.padding(top = 16.dp))
    else if (showTitleInMd3) BasicListItem(leadingText = title)
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = if (miuix) 16.dp else 0.dp)
        .testTag("preference-group-$title"),
        cornerRadius = if (miuix) top.yukonga.miuix.kmp.basic.CardDefaults.CornerRadius else 0.dp,
        colors = if (miuix) top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors()
            else top.yukonga.miuix.kmp.basic.CardColors(androidx.compose.ui.graphics.Color.Transparent,
                androidx.compose.material3.LocalContentColor.current)) {
        CompositionLocalProvider(LocalPreferenceGroup provides miuix) { content() }
    }
}

@Composable
internal fun PreferenceFrame(content: @Composable () -> Unit) {
    if (LocalPreferenceGroup.current) content()
    else Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) { content() }
}
