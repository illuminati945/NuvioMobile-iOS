package com.nuvio.app.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * An authentic iOS edge-swipe back container.
 * When [enabled] is true, swiping right from the left edge of the screen
 * interactively slides the content right and invokes [onBack] when dismissed.
 */
@Composable
fun IosSwipeBackContainer(
    onBack: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
        return
    }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val edgeThresholdPx = with(density) { 44.dp.toPx() }
    val offsetX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var hapticFired by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val dismissThreshold = widthPx * 0.30f

        val gestureModifier = Modifier.pointerInput(enabled, widthPx) {
            if (!enabled || widthPx <= 0f) return@pointerInput

            awaitEachGesture {
                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                if (down.position.x > edgeThresholdPx) return@awaitEachGesture

                val velocityTracker = VelocityTracker()
                velocityTracker.addPosition(down.uptimeMillis, down.position)

                val pointerId = down.id
                var totalDragX = 0f
                var isHorizontalSwipe = false

                while (true) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break

                    if (change.pressed) {
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        val deltaX = change.positionChange().x
                        val deltaY = change.positionChange().y

                        if (!isHorizontalSwipe) {
                            if (abs(deltaX) > abs(deltaY) && deltaX > 0) {
                                isHorizontalSwipe = true
                                isDragging = true
                                change.consume()
                            } else if (abs(deltaY) > abs(deltaX)) {
                                break
                            }
                        }

                        if (isHorizontalSwipe) {
                            change.consume()
                            totalDragX = (totalDragX + deltaX).coerceAtLeast(0f)

                            coroutineScope.launch {
                                offsetX.snapTo(totalDragX)
                            }

                            if (totalDragX >= dismissThreshold && !hapticFired) {
                                hapticFired = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } else if (totalDragX < dismissThreshold && hapticFired) {
                                hapticFired = false
                            }
                        }
                    } else {
                        if (isHorizontalSwipe) {
                            val velocity = velocityTracker.calculateVelocity().x
                            val shouldDismiss = totalDragX >= dismissThreshold || velocity > 800f

                            coroutineScope.launch {
                                if (shouldDismiss) {
                                    offsetX.animateTo(
                                        targetValue = widthPx,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow,
                                        ),
                                    )
                                    onBack()
                                } else {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                }
                                isDragging = false
                                hapticFired = false
                            }
                        }
                        break
                    }
                }
            }
        }

        val currentOffset = offsetX.value
        val progress = if (widthPx > 0f) (currentOffset / widthPx).coerceIn(0f, 1f) else 0f

        Box(modifier = Modifier.fillMaxSize().then(gestureModifier)) {
            if (currentOffset > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = (1f - progress) * 0.45f)),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = currentOffset
                    },
            ) {
                content()

                if (currentOffset > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .width(16.dp)
                            .graphicsLayer { translationX = -16.dp.toPx() }
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.25f * (1f - progress)),
                                    ),
                                ),
                            ),
                    )
                }
            }
        }
    }
}
