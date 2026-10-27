package com.example.c001apk.compose.ui.component.cards.miuix

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.c001apk.compose.ui.component.LinkText
import com.example.c001apk.compose.ui.component.CoilLoader
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.mocharealm.gaze.capsule.ContinuousRoundedRectangle
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Personal

internal data class ContentAction(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
internal fun ContentAvatar(url: String?, size: Dp, tag: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.size(size).clip(CircleShape).testTag(tag).clickable(onClick = onClick),
        shape = CircleShape, color = MiuixTheme.colorScheme.secondaryContainer) {
        if (!url.isNullOrBlank()) CoilLoader(url = url, modifier = Modifier.fillMaxSize())
        else Icon(MiuixIcons.Useful.Personal, null, Modifier.padding(7.dp),
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

@Composable
internal fun ContentMenu(expanded: Boolean, onDismiss: () -> Unit, actions: List<ContentAction>) {
    val visible = remember { mutableStateOf(expanded) }
    visible.value = expanded
    ListPopup(show = visible, onDismissRequest = onDismiss) {
        androidx.compose.foundation.layout.Column {
            actions.forEach { action ->
                BasicComponent(
                    title = action.title,
                    leftAction = { Icon(action.icon, null, Modifier.padding(end = 12.dp).size(22.dp)) },
                    onClick = rememberHapticClick { onDismiss(); action.onClick() },
                )
            }
        }
    }
}

@Composable
internal fun ContentActionButton(
    icon: ImageVector,
    label: String,
    count: String,
    tag: String,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    val foreground = if (active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
    Surface(
        onClick = rememberHapticClick(onClick = onClick),
        modifier = Modifier.testTag(tag).heightIn(min = 40.dp).semantics {
            contentDescription = "$label ${count.ifBlank { "0" }}"
            selected = active
        },
        shape = ContinuousRoundedRectangle(12.dp),
        color = if (active) MiuixTheme.colorScheme.tertiaryContainer else Color.Transparent,
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(19.dp), tint = foreground)
            Text(count.takeIf { it.isNotBlank() && it != "0" } ?: label,
                color = foreground, fontSize = 13.sp, style = MiuixTheme.textStyles.footnote1)
        }
    }
}

@Composable
internal fun ContentRichText(
    text: String?,
    onOpenLink: (String, String?) -> Unit,
    modifier: Modifier = Modifier,
    size: Float = 16f,
    bold: Boolean = false,
    maxLines: Int? = null,
    secondary: Boolean = false,
    isReply: Boolean = false,
    onShowReplies: (() -> Unit)? = null,
    images: List<String>? = null,
) {
    LinkText(
        modifier = modifier,
        text = text,
        textSize = size,
        isBold = bold,
        maxLines = maxLines,
        color = (if (secondary) MiuixTheme.colorScheme.onSurfaceVariantSummary
            else MiuixTheme.colorScheme.onBackground).toArgb(),
        linkColor = MiuixTheme.colorScheme.primary.toArgb(),
        lineSpacingMultiplier = 1.4f,
        onOpenLink = onOpenLink,
        isReply = isReply,
        onShowTotalReply = onShowReplies,
        imgList = images,
    )
}
