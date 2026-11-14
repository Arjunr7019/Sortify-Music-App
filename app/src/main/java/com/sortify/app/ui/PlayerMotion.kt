package com.sortify.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.sortify.app.Screen
import com.sortify.app.SortifyViewModel

/** Directional flings win; short, slow drags return to their starting state. */
internal fun settlePlayerExpanded(
    startedExpanded: Boolean,
    collapsedFraction: Float,
    distanceThreshold: Float,
    velocity: Float,
    velocityThreshold: Float,
): Boolean = when {
    velocity <= -velocityThreshold -> true
    velocity >= velocityThreshold -> false
    startedExpanded -> collapsedFraction < distanceThreshold
    else -> 1f - collapsedFraction >= distanceThreshold
}

@Stable
class PlayerMotionState(initialExpanded: Boolean) {
    val animation = Animatable(if (initialExpanded) 0f else 1f)
    var dragging by mutableStateOf(false); private set
    private var dragFraction by mutableFloatStateOf(1f)
    private var startedExpanded = initialExpanded
    var settleRequest by mutableIntStateOf(0); private set
    val fraction: Float get() = if (dragging) dragFraction else animation.value

    fun start(expanded: Boolean) {
        startedExpanded = expanded
        dragFraction = animation.value
        dragging = true
    }

    fun drag(delta: Float, height: Float) {
        if (dragging) dragFraction = (dragFraction + delta / height.coerceAtLeast(1f)).coerceIn(0f, 1f)
    }

    suspend fun settle(velocity: Float, height: Float, density: Float, setExpanded: (Boolean) -> Unit) {
        if (!dragging) return
        val target = settlePlayerExpanded(startedExpanded, dragFraction,
            (56f * density / height.coerceAtLeast(1f)).coerceAtMost(.25f), velocity, 900f * density)
        animation.snapTo(dragFraction)
        dragging = false
        setExpanded(target)
        settleRequest++
    }

    suspend fun animate(expanded: Boolean) {
        if (dragging) {
            animation.snapTo(dragFraction)
            dragging = false
        }
        // Compose's duration scale also honors Android's Remove animations setting.
        animation.animateTo(if (expanded) 0f else 1f,
            tween(durationMillis = if (expanded) 320 else 240, easing = FastOutSlowInEasing))
    }
}

@Composable
fun Modifier.playerDrag(
    motion: PlayerMotionState,
    expanded: Boolean,
    heightPx: Float,
    onExpandedChange: (Boolean) -> Unit,
): Modifier {
    val density = LocalDensity.current.density
    // An intentional horizontal drag cancels a tap without expanding the card.
    return draggable(rememberDraggableState { }, Orientation.Horizontal).draggable(
        state = rememberDraggableState { motion.drag(it, heightPx) },
        orientation = Orientation.Vertical,
        onDragStarted = { motion.start(expanded) },
        onDragStopped = { velocity -> motion.settle(velocity, heightPx, density, onExpandedChange) },
    )
}

@Composable
fun PlayerOverlay(vm: SortifyViewModel, motion: PlayerMotionState, heightPx: Float) {
    val expanded = vm.screen == Screen.Player || vm.screen == Screen.Queue
    val focusManager = LocalFocusManager.current
    LaunchedEffect(expanded, motion.settleRequest) {
        if (expanded) focusManager.clearFocus(force = true)
        motion.animate(expanded)
    }
    if (vm.current != null && (expanded || motion.dragging || motion.animation.value < 1f)) {
        val drag = Modifier.playerDrag(motion, true, heightPx) { open -> if (!open && vm.screen == Screen.Player) vm.back() }
        Surface(Modifier.fillMaxSize().graphicsLayer {
            translationY = motion.fraction * heightPx
            shape = RoundedCornerShape((motion.fraction * 16f).dp)
            clip = true
        }, color = MaterialTheme.colorScheme.background) {
            ScreenBackground(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxHeight().widthIn(max = 900.dp).align(Alignment.TopCenter)) {
                    if (vm.screen == Screen.Queue) QueueScreen(vm) else PlayerScreen(vm, dismissGesture = drag)
                }
            }
        }
    }
}
