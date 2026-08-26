package com.nuvio.app.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * An authentic iOS edge-swipe back container.
 * When [enabled] is true, swiping right from the leftmost edge of the screen
 * interactively slides the content right and invokes [onBack] when dismissed.
 * Uses a dedicated bezel edge detector to ensure all buttons and interactions
 * in [content] remain 100% responsive and unblocked.
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
    val edgeWidth = 24.dp
    val offsetX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var hapticFired by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val dismissThreshold = screenWidthPx * 0.30f

        val currentOffset = offsetX.value
        val progress = if (screenWidthPx > 0f) (currentOffset / screenWidthPx).coerceIn(0f, 1f) else 0f

        Box(modifier = Modifier.fillMaxSize()) {
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

            // Dedicated left-edge touch strip
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .then(if (isDragging) Modifier.fillMaxWidth() else Modifier.width(edgeWidth))
                    .pointerInput(enabled, screenWidthPx) {
                        if (!enabled || screenWidthPx <= 0f) return@pointerInput

                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                hapticFired = false
                            },
                            onDragEnd = {
                                val shouldDismiss = offsetX.value >= dismissThreshold
                                coroutineScope.launch {
                                    if (shouldDismiss) {
                                        offsetX.animateTo(
                                            targetValue = screenWidthPx,
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
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                    isDragging = false
                                    hapticFired = false
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = (offsetX.value + dragAmount).coerceAtLeast(0f)
                                coroutineScope.launch {
                                    offsetX.snapTo(newOffset)
                                }

                                if (newOffset >= dismissThreshold && !hapticFired) {
                                    hapticFired = true
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else if (newOffset < dismissThreshold && hapticFired) {
                                    hapticFired = false
                                }
                            },
                        )
                    },
            )
        }
    }
}
