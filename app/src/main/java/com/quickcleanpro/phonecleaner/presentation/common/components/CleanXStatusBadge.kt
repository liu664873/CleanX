package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.RotatingRingAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue

enum class CleanXStatusBadgeState {
    Inactive,
    Active,
    Complete,
}

@Composable
fun CleanXStatusBadge(
    state: CleanXStatusBadgeState,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    ringWidth: Dp = 1.8.dp,
    ringColor: Color = CleanXBlue,
    backgroundColor: Color = Color(0xFFC8D2DE),
    animationDurationMillis: Int = 1200,
    arcLength: Float = 180f,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            CleanXStatusBadgeState.Complete -> CleanXCheckBadge(checked = true, size = size)
            CleanXStatusBadgeState.Active ->
                RotatingRingAnimation(
                    modifier = Modifier.size(size),
                    ringWidth = ringWidth,
                    ringColor = ringColor,
                    backgroundColor = backgroundColor,
                    animationDurationMillis = animationDurationMillis,
                    arcLength = arcLength,
                )
            CleanXStatusBadgeState.Inactive -> CleanXCheckBadge(checked = false, size = size)
        }
    }
}
