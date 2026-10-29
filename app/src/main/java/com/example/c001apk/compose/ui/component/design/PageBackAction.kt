package com.example.c001apk.compose.ui.component.design

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.nav.gesture.PredictiveBackHandler

/** A local action (web history or root tab) takes precedence over popping its page. */
@Composable
fun PageBackAction(enabled: Boolean, onBack: () -> Unit) {
    if (isMiuix()) {
        PredictiveBackHandler(enabled = enabled,
            onProgress = { events -> events.collect { } },
            onCommit = onBack, onCancel = {})
    } else {
        BackHandler(enabled, onBack)
    }
}
