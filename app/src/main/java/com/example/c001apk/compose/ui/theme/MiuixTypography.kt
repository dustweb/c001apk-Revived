package com.example.c001apk.compose.ui.theme

import androidx.compose.material3.Typography
import top.yukonga.miuix.kmp.theme.defaultTextStyles

internal val MiuixTypography = defaultTextStyles().let { native ->
    Typography(
        displayLarge = native.title1, displayMedium = native.title1, displaySmall = native.title2,
        headlineLarge = native.title1, headlineMedium = native.title2, headlineSmall = native.title3,
        titleLarge = native.title3, titleMedium = native.headline1, titleSmall = native.headline2,
        bodyLarge = native.main, bodyMedium = native.body1, bodySmall = native.body2,
        labelLarge = native.button, labelMedium = native.footnote1, labelSmall = native.footnote2,
    )
}
