package com.quickcleanpro.phonecleaner.presentation.common.components.animations

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R

@Composable
fun CleanSpiralAnimation(
    modifier: Modifier = Modifier,
    containerSize: Dp? = 400.dp,
    centerSize: Dp = 250.dp,
    @DrawableRes spiralResId: Int = R.drawable.scan_spiral,
    animationDurationMillis: Int = 4000,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cleanSpiralRotation")
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
        label = "cleanSpiralAngle",
    )

    Box(
        modifier =
            if (containerSize != null) {
                modifier.size(containerSize)
            } else {
                modifier
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(centerSize)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
            content = content,
        )
        Image(
            painter = painterResource(spiralResId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = rotation),
            contentScale = ContentScale.Fit,
        )
    }

}
