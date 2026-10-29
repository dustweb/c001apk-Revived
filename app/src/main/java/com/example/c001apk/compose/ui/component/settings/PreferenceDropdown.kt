package com.example.c001apk.compose.ui.component.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.c001apk.compose.ui.component.rememberHapticClick
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

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
    var expanded by remember { mutableStateOf(false) }
    com.example.c001apk.compose.ui.component.design.GlobalPopupBlur(expanded)
    val click = rememberHapticClick {}
    val haptics = remember(click) {
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = click()
        }
    }
    PreferenceFrame {
        CompositionLocalProvider(LocalHapticFeedback provides haptics) {
            OverlayDropdownPreference(title = title, summary = summary, items = items, selectedIndex = selectedIndex,
                enabled = enabled, modifier = modifier, onExpandedChange = { expanded = it }, onSelectedIndexChange = onSelectedIndexChange)
        }
    }
}
