package com.example.c001apk.compose.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.UiStyle
import com.example.c001apk.compose.ui.component.design.StyleButton
import com.example.c001apk.compose.ui.component.rememberHapticClick

@Composable
fun UiStyleSelector(selected: UiStyle, onSelect: (UiStyle) -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(UiStyle.MD3, UiStyle.MIUIX).forEach { style ->
            val active = selected == style
            StyleButton(
                onClick = rememberHapticClick { if (!active) onSelect(style) },
                modifier = Modifier.weight(1f).testTag("ui-style-${style.name}")
                    .semantics { this.selected = active },
                primary = active,
            ) {
                Text(if (style == UiStyle.MIUIX) "miuix" else "MD3")
            }
        }
    }
}
