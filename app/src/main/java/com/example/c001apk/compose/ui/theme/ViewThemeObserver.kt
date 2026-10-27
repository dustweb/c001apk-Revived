package com.example.c001apk.compose.ui.theme

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.c001apk.compose.logic.datastore.UserPreferencesCompat
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import com.example.c001apk.compose.logic.repository.UserPreferencesRepository
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.TextStyles

fun Activity.observeViewTheme(repository: UserPreferencesRepository,
    onTheme: (UserPreferencesCompat, Boolean, ColorScheme, TextStyles) -> Unit) {
    val observer = ComposeView(this).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val prefs by repository.data.collectAsStateWithLifecycle(initialValue = null)
            prefs?.let { value ->
                val dark = value.isDarkMode()
                CompositionLocalProvider(LocalUserPreferences provides value) {
                    C001apkComposeTheme(darkTheme = dark, themeType = value.themeType,
                        seedColor = value.seedColor, materialYou = value.materialYou, pureBlack = value.pureBlack,
                        paletteStyle = value.paletteStyle, fontScale = value.fontScale,
                        contentScale = value.contentScale, uiStyle = value.uiStyle) {
                        val colors = MaterialTheme.colorScheme
                        val textStyles = MiuixTheme.textStyles
                        SideEffect { onTheme(value, dark, colors, textStyles) }
                    }
                }
            }
        }
    }
    (window.decorView as ViewGroup).addView(observer, ViewGroup.LayoutParams(0, 0))
}
