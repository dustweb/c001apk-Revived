package com.example.c001apk.compose.ui.component.design

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.nav.gesture.PredictiveBackHandler

/** A local action (web history or root tab) takes precedence over popping its page. */
@Composable
fun PageBackAction(enabled: Boolean, onBack: () -> Unit) {
    PredictiveBackHandler(enabled = enabled,
        onProgress = { events -> events.collect { } },
        onCommit = onBack, onCancel = {})
}
