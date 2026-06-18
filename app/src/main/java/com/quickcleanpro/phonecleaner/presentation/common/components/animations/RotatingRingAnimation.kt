package com.quickcleanpro.phonecleaner.presentation.common.components.animations

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RotatingRingAnimation(
    modifier: Modifier = Modifier,
    ringWidth: Dp = 20.dp,
    ringColor: Color = Color(0xFF337EFF),
    backgroundColor: Color = Color(0x33337EFF),
    animationDurationMillis: Int = 800,
    arcLength: Float = 180f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    CleanSpiralAnimation(
        modifier = modifier,
        containerSize = null,
        centerSize = 0.dp,
        animationDurationMillis = animationDurationMillis,
        content = content,
    )
}
