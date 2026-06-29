package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CleanXCheckBadge(
    modifier: Modifier = Modifier,
    checked: Boolean = true,
    size: Dp = 24.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CleanXPillShape)
            .background(if (checked) CleanXBlue else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.64f)
            )
        } else {
            Canvas(modifier = Modifier.size(size)) {
                drawCircle(
                    color = Color(0xFFC8D2DE),
                    radius = this.size.minDimension / 2.35f,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
