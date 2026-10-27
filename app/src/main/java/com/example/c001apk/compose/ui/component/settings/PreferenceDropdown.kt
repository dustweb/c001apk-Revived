package com.example.c001apk.compose.ui.component.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.c001apk.compose.ui.component.rememberHapticClick
import top.yukonga.miuix.kmp.extra.SuperDropdown

@Composable
internal fun PreferenceDropdown(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
) {
    val click = rememberHapticClick {}
    val haptics = remember(click) {
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = click()
        }
    }
    PreferenceFrame {
        CompositionLocalProvider(LocalHapticFeedback provides haptics) {
            SuperDropdown(title = title, summary = summary, items = items, selectedIndex = selectedIndex,
                enabled = enabled, modifier = modifier, onSelectedIndexChange = onSelectedIndexChange)
        }
    }
}
