package com.example.c001apk.compose.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.c001apk.compose.UiStyle
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.MiuixPopupHost
import top.yukonga.miuix.kmp.theme.darkColorScheme as miuixDarkColors
import top.yukonga.miuix.kmp.theme.lightColorScheme as miuixLightColors

val LocalUiStyle = compositionLocalOf { UiStyle.MD3 }

@Composable
fun isMiuix() = LocalUiStyle.current == UiStyle.MIUIX

fun ColorScheme.miuixColors(dark: Boolean, pureBlack: Boolean): Colors {
    val base = if (dark) miuixDarkColors() else miuixLightColors()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryVariant = primary,
        onPrimaryVariant = onPrimary,
        primaryContainer = primary,
        onPrimaryContainer = onPrimary,
        disabledPrimary = primary.copy(alpha = 0.35f),
        disabledOnPrimary = onPrimary.copy(alpha = 0.5f),
        disabledPrimaryButton = primary.copy(alpha = 0.35f),
        disabledOnPrimaryButton = onPrimary.copy(alpha = 0.5f),
        disabledPrimarySlider = primary.copy(alpha = 0.35f),
        tertiaryContainer = primaryContainer,
        onTertiaryContainer = onPrimaryContainer,
        background = if (dark && pureBlack) Color.Black else base.background,
    )
}

fun ColorScheme.withMiuixSurfaces(colors: Colors): ColorScheme = copy(
    background = colors.surface,
    onBackground = colors.onSurface,
    surface = colors.background,
    onSurface = colors.onBackground,
    surfaceVariant = colors.secondaryContainer,
    onSurfaceVariant = colors.onSurfaceVariantSummary,
    surfaceContainerLowest = colors.surface,
    surfaceContainerLow = colors.background,
    surfaceContainer = colors.surfaceContainer,
    surfaceContainerHigh = colors.surfaceContainerHigh,
    surfaceContainerHighest = colors.surfaceContainerHighest,
    outlineVariant = colors.dividerLine,
)

@Composable
fun UiStyleTheme(style: UiStyle, colors: Colors, content: @Composable () -> Unit) {
    val materialIndication = LocalIndication.current
    // Keep the provider tree stable so a style change does not discard page state.
    MiuixTheme(colors = colors) {
        CompositionLocalProvider(
            LocalUiStyle provides style,
            LocalIndication provides if (style == UiStyle.MIUIX) LocalIndication.current
                else materialIndication,
        ) {
            Box(Modifier.fillMaxSize()) {
                content()
                if (style == UiStyle.MIUIX) MiuixPopupHost()
            }
        }
    }
}
