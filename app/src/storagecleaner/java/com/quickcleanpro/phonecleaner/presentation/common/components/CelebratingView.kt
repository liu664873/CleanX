package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R

@Composable
fun CelebratingView(
    imageRes: Int = R.drawable.compeleted,
    size: Dp = 160.dp
) {
    // 触发动画的状态
    var animationStarted by remember { mutableStateOf(false) }

    // 启动时触发动画
    LaunchedEffect(Unit) {
        animationStarted = true
    }

    // 缩放动画：从 0.3 到 1.2（轻微过冲，更有活力）
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1.2f else 0.3f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )

    // 淡入动画：从 0 到 1
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )

    Column (
        modifier = Modifier.fillMaxWidth()
            .padding(top = 139.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .scale(scale)
                .alpha(alpha)
        )
    }
}