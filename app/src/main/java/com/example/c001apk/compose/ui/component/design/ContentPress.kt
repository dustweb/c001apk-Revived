package com.example.c001apk.compose.ui.component.design

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/** Observe without consuming: rich text, media and child actions retain their own gestures. */
@Composable
fun Modifier.contentPress(enabled: Boolean = true, pressedScale: Float = 0.985f): Modifier {
    if (!enabled) return this
    val source = remember { MutableInteractionSource() }
    val scale = remember { Animatable(1f, visibilityThreshold = 0.0001f) }
    LaunchedEffect(source, pressedScale) {
        source.interactions.collectLatest { interaction ->
            scale.animateTo(
                if (interaction is PressInteraction.Press) pressedScale else 1f,
                spring(dampingRatio = 0.85f, stiffness = 350f, visibilityThreshold = 0.0001f),
            )
        }
    }
    return graphicsLayer { scaleX = scale.value; scaleY = scale.value }.pointerInput(source) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val press = PressInteraction.Press(down.position)
            source.tryEmit(press)
            var finished = false
            try {
                var released = false
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                    if ((pointer.position - down.position).getDistance() > viewConfiguration.touchSlop) break
                    if (!pointer.pressed) { released = true; break }
                }
                source.tryEmit(if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press))
                finished = true
            } finally {
                if (!finished) source.tryEmit(PressInteraction.Cancel(press))
            }
        }
    }
}
