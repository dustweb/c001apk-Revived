package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import top.yukonga.miuix.kmp.squircle.squircleBorder
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal val LocalPrimaryDialogAction = compositionLocalOf { false }
internal val NoNativeHaptics = object : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors? = null,
    contentPadding: PaddingValues? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (isMiuix()) {
        val nativeColors = colors?.let {
            MiuixButtonDefaults.buttonColors(it.containerColor, it.disabledContainerColor,
                it.contentColor, it.disabledContentColor)
        } ?: MiuixButtonDefaults.buttonColorsPrimary()
        val foreground = if (enabled) nativeColors.contentColor else nativeColors.disabledContentColor
        MiuixButton(onClick, modifier, enabled, insideMargin = contentPadding ?: MiuixButtonDefaults.InsideMargin, colors = nativeColors) {
            CompositionLocalProvider(
                LocalContentColor provides foreground,
                LocalTextStyle provides MiuixTheme.textStyles.button.copy(
                    color = foreground,
                ),
            ) {
                content()
            }
        }
    } else {
        androidx.compose.material3.Button(onClick, modifier, enabled, colors = colors ?: ButtonDefaults.buttonColors(),
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding, content = content)
    }
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (isMiuix()) {
        Button(onClick, modifier.squircleBorder(1.dp, MiuixTheme.colorScheme.outline, MiuixButtonDefaults.CornerRadius),
            enabled, colors = ButtonDefaults.outlinedButtonColors(), contentPadding = contentPadding, content = content)
    } else {
        androidx.compose.material3.OutlinedButton(onClick, modifier, enabled,
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding, content = content)
    }
}

@Composable
fun StyleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    if (isMiuix()) {
        val nativeColors = if (primary) MiuixButtonDefaults.buttonColorsPrimary()
            else MiuixButtonDefaults.buttonColors()
        val foreground = if (enabled) nativeColors.contentColor else nativeColors.disabledContentColor
        MiuixButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = nativeColors,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides foreground,
                LocalTextStyle provides MiuixTheme.textStyles.button.copy(color = foreground),
            ) { content() }
        }
    } else {
        androidx.compose.material3.FilledTonalButton(
            onClick = onClick, modifier = modifier, enabled = enabled,
            colors = if (primary) ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) else ButtonDefaults.filledTonalButtonColors(),
            content = content,
        )
    }
}

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) = StyleButton(onClick, modifier, enabled, content = content)

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    if (isMiuix()) {
        StyleButton(onClick, modifier, enabled, LocalPrimaryDialogAction.current, content)
    } else {
        androidx.compose.material3.TextButton(onClick, modifier, enabled, content = content)
    }
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    if (isMiuix()) {
        MiuixIconButton(onClick, modifier, enabled, minHeight = 48.dp, minWidth = 48.dp, content = content)
    } else {
        androidx.compose.material3.IconButton(onClick, modifier, enabled,
            interactionSource = interactionSource, content = content)
    }
}

@Composable
fun OutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    content: @Composable () -> Unit,
) {
    if (isMiuix()) {
        val outlined = when (val brush = border?.brush) {
            is SolidColor -> modifier.squircleBorder(border.width, brush.value,
                top.yukonga.miuix.kmp.basic.IconButtonDefaults.CornerRadius)
            null -> modifier
            else -> modifier.border(border, RoundedCornerShape(top.yukonga.miuix.kmp.basic.IconButtonDefaults.CornerRadius))
        }
        MiuixIconButton(onClick, outlined, minWidth = 48.dp, minHeight = 48.dp, content = content)
    } else {
        androidx.compose.material3.OutlinedIconButton(onClick, modifier, border = border, content = content)
    }
}

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    content: @Composable () -> Unit,
) {
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.FloatingActionButton(
            onClick = onClick, modifier = modifier, containerColor = containerColor,
            minWidth = 56.dp, minHeight = 56.dp,
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
        }
    } else {
        androidx.compose.material3.FloatingActionButton(onClick, modifier,
            containerColor = containerColor, contentColor = contentColor, content = content)
    }
}

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    if (isMiuix()) {
        CompositionLocalProvider(LocalHapticFeedback provides NoNativeHaptics) {
            top.yukonga.miuix.kmp.basic.Switch(checked, onCheckedChange, modifier, enabled = enabled)
        }
    } else {
        androidx.compose.material3.Switch(checked, onCheckedChange, modifier, enabled = enabled)
    }
}

@Composable
fun RadioButton(selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    if (isMiuix()) {
        CompositionLocalProvider(LocalHapticFeedback provides NoNativeHaptics) {
            top.yukonga.miuix.kmp.basic.RadioButton(
                selected = selected, onClick = onClick, modifier = modifier,
            )
        }
    } else {
        androidx.compose.material3.RadioButton(selected, onClick, modifier)
    }
}

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    if (isMiuix()) {
        top.yukonga.miuix.kmp.basic.Slider(
            value = value, onValueChange = onValueChange, modifier = modifier,
            valueRange = valueRange,
            hapticEffect = top.yukonga.miuix.kmp.basic.SliderDefaults.SliderHapticEffect.None,
        )
    } else {
        androidx.compose.material3.Slider(value, onValueChange, modifier, valueRange = valueRange)
    }
}
