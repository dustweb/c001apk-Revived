package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.c001apk.compose.ui.component.BackButton
import com.example.c001apk.compose.ui.component.design.TopAppBar
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.ScrollBehavior

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceTopBar(
    title: String,
    scrollBehavior: ScrollBehavior,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val navigation: @Composable () -> Unit = { onBack?.let { BackButton(it) } }
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.TopAppBar(
            title = title, scrollBehavior = scrollBehavior, navigationIcon = navigation,
            actions = actions, modifier = Modifier.testTag("collapsing-preference-header"),
        )
    } else {
        TopAppBar(title = { Text(title) }, navigationIcon = navigation, actions = actions)
    }
}
