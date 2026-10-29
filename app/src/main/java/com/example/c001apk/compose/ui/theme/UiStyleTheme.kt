package com.example.c001apk.compose.ui.theme

import com.example.c001apk.compose.ui.component.design.LocalModalBlur
import com.example.c001apk.compose.ui.component.design.rememberModalBlurState
import com.example.c001apk.compose.ui.component.design.modalBackgroundBlur
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
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

val LocalUiStyle = compositionLocalOf { UiStyle.MD3 }

@Composable
fun isMiuix() = LocalUiStyle.current == UiStyle.MIUIX

@Composable
fun rememberMiuixController(
    dark: Boolean,
    keyColor: Color?,
    pureBlack: Boolean,
    paletteStyle: String,
): ThemeController {
    val controller = remember(dark, keyColor, paletteStyle) {
        ThemeController(
            colorSchemeMode = if (dark) ColorSchemeMode.MonetDark else ColorSchemeMode.MonetLight,
            keyColor = keyColor,
            paletteStyle = ThemePaletteStyle.entries.firstOrNull { it.name == paletteStyle }
                ?: ThemePaletteStyle.TonalSpot,
        )
    }
    val colors = controller.currentColors()
    return if (dark && pureBlack) remember(colors) {
        ThemeController(ColorSchemeMode.Dark,
            darkColors = colors.copy(background = Color.Black, surface = Color.Black, surfaceContainer = Color.Black))
    } else controller
}

fun ColorScheme.withMiuixSurfaces(colors: Colors): ColorScheme = copy(
    primary = colors.primary,
    onPrimary = colors.onPrimary,
    primaryContainer = colors.tertiaryContainer,
    onPrimaryContainer = colors.onTertiaryContainer,
    error = colors.error,
    onError = colors.onError,
    errorContainer = colors.errorContainer,
    onErrorContainer = colors.onErrorContainer,
    outline = colors.outline,
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
fun UiStyleTheme(style: UiStyle, controller: ThemeController, content: @Composable () -> Unit) {
    val materialIndication = LocalIndication.current
    val materialOverscroll = LocalOverscrollFactory.current
    // Keep the provider tree stable so a style change does not discard page state.
    val modalBlur = rememberModalBlurState()
    MiuixTheme(controller = controller) {
        CompositionLocalProvider(
            LocalUiStyle provides style,
            LocalModalBlur provides modalBlur,
            LocalOverscrollFactory provides if (style == UiStyle.MIUIX) LocalOverscrollFactory.current else materialOverscroll,
            LocalIndication provides if (style == UiStyle.MIUIX) LocalIndication.current
                else materialIndication,
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding).modalBackgroundBlur(modalBlur)) { content() }
            }
        }
    }
}
