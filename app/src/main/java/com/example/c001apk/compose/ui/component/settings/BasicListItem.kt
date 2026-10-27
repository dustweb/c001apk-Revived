package com.example.c001apk.compose.ui.component.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BasicListItem(
    modifier: Modifier = Modifier,
    headlineText: String? = null,
    supportingText: String? = null,
    leadingImageVector: ImageVector? = null,
    leadingPainter: Painter? = null,
    leadingText: String? = null,
    trailingContent: @Composable () -> Unit = {},
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    if (isMiuix()) {
        if (leadingText != null) {
            Text(leadingText, modifier.padding(start = 28.dp, top = 20.dp, bottom = 8.dp),
                color = MiuixTheme.colorScheme.onBackgroundVariant,
                style = MaterialTheme.typography.titleSmall)
        } else {
            val grouped = LocalPreferenceGroup.current
            PreferenceFrame {
                BasicComponent(
                    title = headlineText, summary = supportingText,
                    modifier = modifier, enabled = enabled,
                    startAction = if (!grouped && (leadingImageVector != null || leadingPainter != null)) ({
                        Box(Modifier.padding(end = 12.dp)) {
                            if (leadingImageVector != null) Icon(leadingImageVector, null,
                                tint = if (enabled) MiuixTheme.colorScheme.onBackground
                                    else MiuixTheme.colorScheme.disabledOnSurface)
                            else if (leadingPainter != null) Image(leadingPainter, null, Modifier.size(24.dp))
                        }
                    }) else null,
                    endActions = { trailingContent() },
                    onClick = onClick,
                )
            }
        }
        return
    }
    ListItem(
        modifier = onClick?.let {
            modifier.clickable(
                onClick = onClick
            )
        } ?: modifier,
        headlineContent = {
            if (headlineText != null) {
                Text(headlineText)
            }
        },
        supportingContent = {
            if (supportingText != null) {
                Text(supportingText)
            }
        },
        leadingContent = {
            if (leadingText != null) {
                Text(
                    text = leadingText,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall
                )
            } else if (leadingImageVector != null) {
                Icon(imageVector = leadingImageVector, contentDescription = null)
            } else if (leadingPainter != null) {
                Image(
                    painter = leadingPainter, contentDescription = null, modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .size(24.dp)
                )
            }
        },
        trailingContent = trailingContent
    )
}
