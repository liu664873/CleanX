package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RotatingRingAnimation(
    modifier: Modifier = Modifier,
    ringWidth: Dp = 20.dp,
    ringColor: Color = Color(0xFF337EFF),
    backgroundColor: Color = Color(0x33337EFF),
    animationDurationMillis: Int = 800,
    arcLength: Float = 180f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotatingRing")
    val startAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotatingRingAngle"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = ringWidth.toPx()
        val radius = size.minDimension / 2f - strokeWidth / 2f
        if (radius <= 0f) return@Canvas

        val topLeft = Offset(
            x = size.width / 2f - radius,
            y = size.height / 2f - radius
        )
        val arcSize = Size(radius * 2f, radius * 2f)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
        drawArc(
            color = ringColor,
            startAngle = startAngle,
            sweepAngle = arcLength.coerceIn(0f, 360f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke
        )
    }
}
