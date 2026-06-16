package com.quickcleanpro.phonecleaner.presentation.common.components.animations

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R

@Composable
fun CleanXScanSpiralAnimation(
    modifier: Modifier = Modifier,
    containerSize: Dp = 252.dp,
    centerSize: Dp = 100.dp,
    @DrawableRes spiralResId: Int = R.drawable.antivirus_scan_spiral,
    glowColor: Color = Color(0x663D7BFF),
    centerColor: Color = Color.White,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val transition = rememberInfiniteTransition(label = "cleanXScanSpiral")
    val slowRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cleanXScanSpiralSlowRotation",
    )
    val fastRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cleanXScanSpiralFastRotation",
    )

    Box(
        modifier = modifier.size(containerSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        glowColor.copy(alpha = 0.26f),
                        Color.Transparent,
                    ),
                ),
            )
        }

        Image(
            painter = painterResource(spiralResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = slowRotation),
            contentScale = ContentScale.Fit,
        )

        Image(
            painter = painterResource(spiralResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.92f)
                .graphicsLayer(rotationZ = fastRotation, alpha = 0.32f),
            contentScale = ContentScale.Fit,
        )

        Box(
            modifier = Modifier
                .size(centerSize)
                .shadow(elevation = 22.dp, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(centerColor),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}
