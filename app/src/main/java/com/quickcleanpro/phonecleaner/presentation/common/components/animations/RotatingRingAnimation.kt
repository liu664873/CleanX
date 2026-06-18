package com.quickcleanpro.phonecleaner.presentation.common.components.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RotatingRingAnimation(
    modifier: Modifier = Modifier,
    ringWidth: Dp = 2.dp,
    ringColor: Color = Color(0xFF337EFF),
    backgroundColor: Color = Color(0x33337EFF),
    animationDurationMillis: Int = 800,
    arcLength: Float = 180f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "rotatingRing")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMillis,
                easing = LinearEasing,
            ),
        ),
        label = "rotatingRingAngle",
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = ringWidth.toPx()
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
                size =
                    androidx.compose.ui.geometry.Size(
                        size.width - strokeWidth,
                        size.height - strokeWidth,
                    ),
            )
        }
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(rotationZ = rotation),
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = ringWidth.toPx()
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = arcLength,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size =
                        androidx.compose.ui.geometry.Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth,
                        ),
                )
            }
        }
        content()
    }
}
