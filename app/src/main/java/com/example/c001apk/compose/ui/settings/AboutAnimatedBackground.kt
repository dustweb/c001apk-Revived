// OS3 color-field presets: Copyright 2026, compose-miuix-ui contributors (Apache-2.0).
package com.example.c001apk.compose.ui.settings

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.shader.RuntimeShader
import top.yukonga.miuix.kmp.shader.asBrush
import top.yukonga.miuix.kmp.shader.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

@Composable
internal fun AboutAnimatedBackground(modifier: Modifier, alpha: () -> Float) {
    val dark = LocalUserPreferences.current.isDarkMode()
    val surface = MiuixTheme.colorScheme.surface
    val primary = MiuixTheme.colorScheme.primary
    val points = remember {
        floatArrayOf(.8f, .2f, 1f, .8f, .9f, 1f, .2f, .9f, 1f, .2f, .2f, 1f)
    }
    val palettes = remember(dark) { aboutColorStages(dark) }
    val colorStage = remember(dark) { Animatable(0f) }
    val shader = remember(dark) {
        if (!isRuntimeShaderSupported()) null else runCatching {
            RuntimeShader(ABOUT_BACKGROUND_SHADER).apply {
                setFloatUniform("uTranslateY", 0f)
                setFloatUniform("uAlphaMulti", 1f)
                setFloatUniform("uNoiseScale", 1.5f)
                setFloatUniform("uPointRadiusMulti", 1f)
                setFloatUniform("uLightOffset", if (dark) 0f else .1f)
                setFloatUniform("uSaturateOffset", if (dark) .17f else .2f)
                setFloatUniform("uPoints", points)
            }
        }.getOrNull()
    }
    val animatedPoints = remember { FloatArray(8) }
    val animatedColors = remember { FloatArray(16) }
    var seconds by remember { mutableFloatStateOf(0f) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val motionEnabled = Build.VERSION.SDK_INT < 26 || ValueAnimator.areAnimatorsEnabled()
    LaunchedEffect(lifecycle, shader, motionEnabled, dark) {
        if (shader == null || !motionEnabled) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                var target = floor(colorStage.value) + 1f
                while (isActive) {
                    delay(if (dark) 4000L else 2500L)
                    colorStage.animateTo(target, spring(dampingRatio = .9f, stiffness = 35f))
                    target += 1f
                }
            }
            var previous = withFrameNanos { it }
            while (isActive) {
                val now = withFrameNanos { it }
                if (now - previous >= 16_666_666L) {
                    seconds += ((now - previous) / 1_000_000_000f).coerceAtMost(.1f)
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
            val pointOffset = if (dark) .4f else .2f
            repeat(4) { index ->
                val x = points[index * 3]
                val y = points[index * 3 + 1]
                val animatedX = x + sin(time + y) * pointOffset
                animatedPoints[index * 2] = animatedX
                animatedPoints[index * 2 + 1] = y + cos(time + animatedX) * pointOffset
            }
            val stage = colorStage.value
            val base = floor(stage).toInt()
            val fraction = stage - base
            val start = palettes[base.mod(palettes.size)]
            val end = palettes[(base + 1).mod(palettes.size)]
            repeat(animatedColors.size) { animatedColors[it] = start[it] + (end[it] - start[it]) * fraction }
            shader.setFloatUniform("uResolution", size.width, size.height)
            shader.setFloatUniform("uBound", 0f, .2f, 1f, .8f)
            shader.setFloatUniform("uAnimTime", time)
            shader.setFloatUniform("uPointsAnim", animatedPoints)
            shader.setFloatUniform("uColors", animatedColors)
            drawRect(shader.asBrush(), alpha = alpha())
        }
    }
}

private fun aboutColorStages(dark: Boolean): List<FloatArray> {
    val first: FloatArray
    val middle: FloatArray
    val last: FloatArray
    if (dark) {
        first = floatArrayOf(.2f, .06f, .88f, .4f, .3f, .14f, .55f, .5f, 0f, .64f, .96f, .5f, .11f, .16f, .83f, .4f)
        middle = floatArrayOf(.07f, .15f, .79f, .5f, .62f, .21f, .67f, .5f, .06f, .25f, .84f, .5f, 0f, .2f, .78f, .5f)
        last = floatArrayOf(.58f, .3f, .74f, .4f, .27f, .18f, .6f, .5f, .66f, .26f, .62f, .5f, .12f, .16f, .7f, .6f)
    } else {
        first = floatArrayOf(1f, .9f, .94f, 1f, 1f, .84f, .89f, 1f, .97f, .73f, .82f, 1f, .64f, .65f, .98f, 1f)
        middle = floatArrayOf(.58f, .74f, 1f, 1f, 1f, .9f, .93f, 1f, .74f, .76f, 1f, 1f, .97f, .77f, .84f, 1f)
        last = floatArrayOf(.98f, .86f, .9f, 1f, .6f, .73f, .98f, 1f, .92f, .93f, 1f, 1f, .56f, .69f, 1f, 1f)
    }
    return listOf(middle, first, middle, last)
}
