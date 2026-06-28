package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXPillShape

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
            Image(
                painter = painterResource(R.drawable.ic_ok),
                contentDescription = null
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
