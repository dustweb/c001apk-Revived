package com.example.c001apk.compose.ui.component

import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

object MiuixPageMotion {
    private fun offsetSpec() = spring(dampingRatio = 0.9f, stiffness = 500f,
        visibilityThreshold = IntOffset(1, 1))
    fun progressSpec() = spring<Float>(dampingRatio = 0.9f, stiffness = 500f, visibilityThreshold = 0.001f)

    fun tabEnter(direction: Int) = slideInHorizontally(offsetSpec()) { direction * it }
    fun tabExit(direction: Int) = slideOutHorizontally(offsetSpec()) { -direction * it }

    fun enter() = slideInHorizontally(offsetSpec()) { it }
    fun exit() = slideOutHorizontally(offsetSpec()) { -it / 4 } + fadeOut(progressSpec(), targetAlpha = 0.88f)
    fun popEnter() = slideInHorizontally(offsetSpec()) { -it / 4 } + fadeIn(progressSpec(), initialAlpha = 0.88f)
    fun popExit() = slideOutHorizontally(offsetSpec()) { it }
}
