package com.example.c001apk.compose.ui.component.settings

import com.example.c001apk.compose.ui.component.design.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.ui.component.design.NoNativeHaptics
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SwitchListItem(
    value: Boolean,
    leadingImageVector: ImageVector,
    headlineText: String,
    supportingText: String? = null,
    onValueChanged: (value: Boolean) -> Unit
) {
    val performHaptic = rememberHapticClick {}
    val toggle = rememberHapticClick {
        onValueChanged(!value)
    }
    if (isMiuix()) {
        PreferenceFrame {
            CompositionLocalProvider(LocalHapticFeedback provides NoNativeHaptics) {
                SwitchPreference(checked = value, title = headlineText, summary = supportingText,
                    onCheckedChange = { performHaptic(); onValueChanged(it) })
            }
        }
        return
    }
    BasicListItem(
        leadingImageVector = leadingImageVector,
        headlineText = headlineText,
        supportingText = supportingText,
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = {
                    performHaptic()
                    onValueChanged(it)
                },
            )
        }
    ) {
        toggle()
    }
}
