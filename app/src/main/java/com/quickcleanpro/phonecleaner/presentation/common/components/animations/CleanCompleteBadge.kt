 package com.quickcleanpro.phonecleaner.presentation.common.components.animations

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clean complete badge with gradient circle, white checkmark, and decorative dots/lines.
 *
 * Used in the clean-complete transition state of JunkCleanScreen.
 *
 * @param modifier Modifier applied to the outer container
 * @param badgeSize Size of the central gradient circle (114.dp per Figma)
 */
@Composable
fun CleanCompleteBadge(
    modifier: Modifier = Modifier,
    badgeSize: Dp = 114.dp,
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "badgeScale",
    )

    LaunchedEffect(Unit) { visible = true }

    // Total area to accommodate badge + decorative dots
    val totalWidth = badgeSize * 2.44f  // ~278dp to fit dots at edges
    val totalHeight = badgeSize * 1.3f  // ~149dp

    Box(
        modifier = modifier.size(totalWidth, totalHeight),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .size(totalWidth, totalHeight)
                .graphicsLayer(scaleX = scale, scaleY = scale),
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val badgeRadius = badgeSize.toPx() / 2f
            val halfW = size.width / 2f
            val halfH = size.height / 2f

            // Decorative dot - blue (left side, position from Figma: 0,44 relative to 82,35 center)
            val blueDotOffset = Offset(centerX - badgeRadius - 25.dp.toPx(), centerY + 3.dp.toPx())
            drawCircle(
                color = Color(0xFF4179FC),
                radius = 7.5.dp.toPx(),
                center = blueDotOffset,
            )

            // Decorative dot - green (right side, position: 265,35 relative to group 48,219)
            val greenDotOffset = Offset(centerX + badgeRadius + 15.dp.toPx(), centerY - 5.dp.toPx())
            drawCircle(
                color = Color(0xFF67E3AD),
                radius = 6.5.dp.toPx(),
                center = greenDotOffset,
            )

            // Decorative line - blue from blue dot toward badge
            val blueLinePath = Path().apply {
                moveTo(blueDotOffset.x + 12.dp.toPx(), blueDotOffset.y - 4.dp.toPx())
                lineTo(centerX - badgeRadius + 8.dp.toPx(), centerY - 15.dp.toPx())
            }
            drawPath(
                path = blueLinePath,
                color = Color(0xFF4179FC),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
            )

            // Decorative line - green from green dot toward badge
            val greenLinePath = Path().apply {
                moveTo(greenDotOffset.x - 10.dp.toPx(), greenDotOffset.y + 5.dp.toPx())
                lineTo(centerX + badgeRadius - 10.dp.toPx(), centerY - 12.dp.toPx())
            }
            drawPath(
                path = greenLinePath,
                color = Color(0xFF67E3AD),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
            )

            // Gradient badge circle
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF4BBBF7), Color(0xFF4179FC)),
                    start = Offset(centerX - badgeRadius, centerY - badgeRadius),
                    end = Offset(centerX + badgeRadius, centerY + badgeRadius),
                ),
                radius = badgeRadius,
                center = Offset(centerX, centerY),
            )

            // White checkmark inside badge
            val checkSize = badgeRadius * 0.68f
            val checkLeft = centerX - checkSize * 0.65f
            val checkTop = centerY - checkSize * 0.15f
            val checkPath = Path().apply {
                moveTo(checkLeft, centerY)
                lineTo(checkLeft + checkSize * 0.4f, centerY + checkSize * 0.42f)
                lineTo(checkLeft + checkSize * 1.1f, centerY - checkSize * 0.4f)
            }
            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(
                    width = 9.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}
