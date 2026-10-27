package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.background
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation as materialSurfaceAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.example.c001apk.compose.ui.theme.isMiuix
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.squircle.squircleSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Custom media/grid layouts use the same surface roles and corners as native cards. */
@Composable
fun Modifier.contentCard(): Modifier = if (isMiuix()) {
    squircleSurface(MiuixTheme.colorScheme.surfaceContainer, CardDefaults.CornerRadius)
} else clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainer)

@Composable
fun ColorScheme.surfaceColorAtElevation(elevation: Dp) =
    if (isMiuix()) MiuixTheme.colorScheme.surfaceContainer else materialSurfaceAtElevation(elevation)

@Composable
fun secondaryTextColor() = if (isMiuix()) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.outline
