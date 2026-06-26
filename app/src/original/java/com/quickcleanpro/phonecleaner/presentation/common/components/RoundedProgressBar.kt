package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 圆角进度条（两端都是半圆形）。
 *
 * @param progress 进度值，范围 0f..1f
 * @param width 进度条宽度，默认 320.dp
 * @param height 进度条高度，默认 10.dp
 * @param trackColor 背景轨道颜色，默认半透白 (0x59FFFFFF)
 * @param fillColor 前景填充颜色，默认白色
 * @param modifier 修饰符，可以覆盖尺寸或添加点击等行为
 */
@Composable
fun RoundedProgressBar(
    progress: Float,
    width: Dp = 320.dp,
    height: Dp = 10.dp,
    trackColor: Color = Color(0x59FFFFFF),
    fillColor: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier =
            modifier
                .size(width = width, height = height),
    ) {
        // 圆角半径 = 高度的一半 → 两端呈现半圆形（胶囊形）
        val cornerRadiusPx = size.height / 2
        val cornerRadius = CornerRadius(cornerRadiusPx)

        // 1. 绘制背景轨道（全宽）
        drawRoundRect(
            color = trackColor,
            cornerRadius = cornerRadius,
        )

        // 2. 绘制前景进度（宽度 = 总宽 × 进度）
        if (progress > 0f) {
            val fillWidth = size.width * progress.coerceIn(0f, 1f)
            drawRoundRect(
                color = fillColor,
                topLeft = Offset.Zero,
                size = Size(fillWidth, size.height),
                cornerRadius = cornerRadius,
            )
        }
    }
}
