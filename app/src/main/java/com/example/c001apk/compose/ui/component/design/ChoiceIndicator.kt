package com.example.c001apk.compose.ui.component.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.state.ToggleableState
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.CheckboxDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Visual indicator only: the owning row supplies radio/checkbox semantics and its click action. */
@Composable
internal fun ChoiceIndicator(checked: Boolean, modifier: Modifier = Modifier) {
    Checkbox(
        state = if (checked) ToggleableState.On else ToggleableState.Off,
        onClick = null,
        modifier = modifier.clearAndSetSemantics {},
        colors = CheckboxDefaults.checkboxColors(
            uncheckedBackgroundColor = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        ),
    )
}
