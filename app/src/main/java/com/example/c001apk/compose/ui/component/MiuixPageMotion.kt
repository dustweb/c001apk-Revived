package com.example.c001apk.compose.ui.component

import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

object MiuixPageMotion {
    private fun offsetSpec() = spring(dampingRatio = 0.9f, stiffness = 500f,
        visibilityThreshold = IntOffset(1, 1))
    fun tabEnter(direction: Int) = slideInHorizontally(offsetSpec()) { direction * it }
    fun tabExit(direction: Int) = slideOutHorizontally(offsetSpec()) { -direction * it }

}
