package com.quickcleanpro.phonecleaner.presentation.common.components.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rotating spiral animation component with a center composable slot.
 *
 * Used in scanning and cleaning states of JunkCleanScreen.
 *
 * @param modifier Modifier applied to the outer Box
 * @param containerSize Overall size of the circular container (default 252.dp per Figma)
 * @param spiralColor Color of the spiral arcs
 * @param bgColor Background color of the circular container
 * @param animationDurationMillis Duration of one full rotation
 * @param ringCount Number of concentric rings in the spiral
 * @param content Composable slot rendered at the center
 */
@Composable
fun CleanSpiralAnimation(
    modifier: Modifier = Modifier,
    containerSize: Dp = 252.dp,
    spiralColor: Color = Color(0xFF4179FC),
    bgColor: Color = Color.White,
    animationDurationMillis: Int = 2400,
    ringCount: Int = 6,
    content: @Composable () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spiralRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMillis,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spiralAngle",
    )

    Box(
        modifier = modifier.size(containerSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(containerSize)) {
            val strokeWidth = 4.dp.toPx()
            val maxRadius = size.minDimension / 2f - strokeWidth / 2f
            if (maxRadius <= 0f) return@Canvas
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background white circle
            drawCircle(
                color = bgColor,
                radius = maxRadius + strokeWidth / 2f,
            )

            // Rotating spiral arcs
            rotate(rotation) {
                for (i in 0 until ringCount) {
                    val radius = maxRadius * (0.6f + 0.4f * i / ringCount)
                    val sweepAngle = 120f + 40f * (ringCount - i) / ringCount
                    val alpha = 0.3f + 0.7f * i / ringCount
                    val startAngleOffset = i * 50f

                    drawArc(
                        color = spiralColor.copy(alpha = alpha),
                        startAngle = startAngleOffset,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(
                            x = center.x - radius,
                            y = center.y - radius,
                        ),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )
                }
            }
        }

        // Center content
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
