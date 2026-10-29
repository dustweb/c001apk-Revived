// OS2 color-field presets: Copyright 2026, compose-miuix-ui contributors (Apache-2.0).
package com.example.c001apk.compose.ui.settings

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.c001apk.compose.logic.providable.LocalUserPreferences
import kotlinx.coroutines.isActive
import top.yukonga.miuix.kmp.shader.RuntimeShader
import top.yukonga.miuix.kmp.shader.asBrush
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun AboutAnimatedBackground(modifier: Modifier, alpha: () -> Float) {
    val dark = LocalUserPreferences.current.isDarkMode()
    val surface = MiuixTheme.colorScheme.surface
    val primary = MiuixTheme.colorScheme.primary
    val points = remember(dark) {
        if (dark) floatArrayOf(.63f, .50f, .88f, .69f, .75f, .80f, .17f, .66f, .81f, .14f, .24f, .72f)
        else floatArrayOf(.67f, .42f, 1f, .69f, .75f, 1f, .14f, .71f, .95f, .14f, .27f, .8f)
    }
    val shader = remember(dark) {
        if (!isRuntimeShaderSupported()) null else runCatching {
            RuntimeShader(ABOUT_BACKGROUND_SHADER).apply {
                setFloatUniform("uTranslateY", 0f)
                setFloatUniform("uAlphaMulti", 1f)
                setFloatUniform("uNoiseScale", 1.5f)
                setFloatUniform("uPointRadiusMulti", 1f)
                setFloatUniform("uLightOffset", if (dark) -.1f else .1f)
                setFloatUniform("uSaturateOffset", .2f)
                setFloatUniform("uPoints", points)
                setFloatUniform("uColors", if (dark)
                    floatArrayOf(0f, .31f, .58f, 1f, .53f, .29f, .15f, 1f, .46f, .06f, .27f, 1f, .16f, .12f, .45f, 1f)
                else floatArrayOf(.57f, .76f, .98f, 1f, .98f, .85f, .68f, 1f, .98f, .75f, .93f, 1f, .73f, .70f, .98f, 1f))
            }
        }.getOrNull()
    }
    val animatedPoints = remember { FloatArray(8) }
    var seconds by remember { mutableFloatStateOf(0f) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val motionEnabled = Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()
    LaunchedEffect(lifecycle, shader, motionEnabled) {
        if (shader == null || !motionEnabled) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            var previous = withFrameNanos { it }
            while (isActive) {
                val now = withFrameNanos { it }
                if (now - previous >= 33_333_333L) {
                    seconds += ((now - previous) / 1_000_000_000f).coerceAtMost(.1f) * .2f
                    previous = now
                }
            }
        }
    }
    Canvas(modifier) {
        drawRect(surface)
        if (shader == null) {
            drawRect(Brush.verticalGradient(listOf(primary.copy(alpha = .2f), Color.Transparent)), alpha = alpha())
        } else if (size.width > 0 && size.height > 0) {
            val time = seconds
            repeat(4) { index ->
                val x = points[index * 3]
                val y = points[index * 3 + 1]
                val animatedX = x + sin(time + y) * .1f
                animatedPoints[index * 2] = animatedX
                animatedPoints[index * 2 + 1] = y + cos(time + animatedX) * .1f
            }
            shader.setFloatUniform("uResolution", size.width, size.height)
            shader.setFloatUniform("uBound", 0f, .2f, 1f, .8f)
            shader.setFloatUniform("uAnimTime", time)
            shader.setFloatUniform("uPointsAnim", animatedPoints)
            drawRect(shader.asBrush(), alpha = alpha())
        }
    }
}
