package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import android.graphics.Matrix as AndroidMatrix
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF as AndroidRectF
import android.graphics.SweepGradient as AndroidSweepGradient

@Composable
fun CleanXScanRingAnimation(
    modifier: Modifier = Modifier,
    ringModifier: Modifier? = null,
    backgroundResId: Int? = R.mipmap.ic_scan_nor_bg,
    backgroundModifier: Modifier? = null,
    ringWidth: Dp = 20.dp,
    ringColor: Color = Color(0xFF1AA7EC),
    backgroundColor: Color = Color(0xFFF1F9FC),
    animationDurationMillis: Int = 1000,
    tailSweepDegrees: Float = 180f,
    content: @Composable BoxScope.() -> Unit = {}
) {

    val infiniteTransition = rememberInfiniteTransition(label = "cleanXScanRing")
    val startAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cleanXScanRingAngle"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (backgroundResId != null) {
            Image(
                painter = painterResource(backgroundResId),
                contentDescription = null,
                modifier = backgroundModifier ?: Modifier.matchParentSize(),
                contentScale = ContentScale.Fit
            )
        }

        val resolvedRingModifier = ringModifier ?: if (backgroundResId != null) {
            Modifier.fillMaxSize(0.7f)
        } else {
            Modifier.matchParentSize()
        }

        Canvas(modifier = resolvedRingModifier) {
            val strokeWidth = ringWidth.toPx()
            val radius = size.minDimension / 2f - strokeWidth / 2f
            if (radius <= 0f) return@Canvas

            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val rect = AndroidRectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )
            val backgroundPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                style = AndroidPaint.Style.STROKE
                strokeCap = AndroidPaint.Cap.ROUND
                this.strokeWidth = strokeWidth
                color = backgroundColor.toArgb()
            }
            val ringPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                style = AndroidPaint.Style.STROKE
                strokeCap = AndroidPaint.Cap.ROUND
                this.strokeWidth = strokeWidth
            }
            val sweepAngle = tailSweepDegrees.coerceIn(0f, 360f)
            val gradient = AndroidSweepGradient(
                centerX,
                centerY,
                intArrayOf(backgroundColor.toArgb(), ringColor.toArgb(), backgroundColor.toArgb()),
                floatArrayOf(0f, sweepAngle / 360f, 1f)
            )
            val matrix = AndroidMatrix().apply {
                setRotate(startAngle, centerX, centerY)
            }
            gradient.setLocalMatrix(matrix)
            ringPaint.shader = gradient

            drawContext.canvas.nativeCanvas.apply {
                drawArc(rect, 0f, 360f, false, backgroundPaint)
                drawArc(rect, startAngle, sweepAngle, false, ringPaint)
            }
        }

        content()
    }
}

@Preview
@Composable
fun PreviewCleanXScanRingAnimation() {
    CleanXScanRingAnimation(
        modifier = Modifier.size(300.dp),
        ringModifier = Modifier.size(210.dp)
    )
}
