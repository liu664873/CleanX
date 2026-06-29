package com.quickcleanpro.phonecleaner.presentation.common

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
 * 鍦嗚杩涘害鏉★紙涓ょ閮芥槸鍗婂渾褰級銆?
 *
 * @param progress 杩涘害鍊硷紝鑼冨洿 0f..1f
 * @param width 杩涘害鏉″搴︼紝榛樿 320.dp
 * @param height 杩涘害鏉￠珮搴︼紝榛樿 10.dp
 * @param trackColor 鑳屾櫙杞ㄩ亾棰滆壊锛岄粯璁ゅ崐閫忕櫧 (0x59FFFFFF)
 * @param fillColor 鍓嶆櫙濉厖棰滆壊锛岄粯璁ょ櫧鑹?
 * @param modifier 淇グ绗︼紝鍙互瑕嗙洊灏哄鎴栨坊鍔犵偣鍑荤瓑琛屼负
 */
@Composable
fun RoundedProgressBar(
    progress: Float,
    width: Dp = 320.dp,
    height: Dp = 10.dp,
    trackColor: Color = Color(0x59FFFFFF),
    fillColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(width = width, height = height)
    ) {
        // 鍦嗚鍗婂緞 = 楂樺害鐨勪竴鍗?鈫?涓ょ鍛堢幇鍗婂渾褰紙鑳跺泭褰級
        val cornerRadiusPx = size.height / 2
        val cornerRadius = CornerRadius(cornerRadiusPx)

        // 1. 缁樺埗鑳屾櫙杞ㄩ亾锛堝叏瀹斤級
        drawRoundRect(
            color = trackColor,
            cornerRadius = cornerRadius
        )

        // 2. 缁樺埗鍓嶆櫙杩涘害锛堝搴?= 鎬诲 脳 杩涘害锛?
        if (progress > 0f) {
            val fillWidth = size.width * progress.coerceIn(0f, 1f)
            drawRoundRect(
                color = fillColor,
                topLeft = Offset.Zero,
                size = Size(fillWidth, size.height),
                cornerRadius = cornerRadius
            )
        }
    }
}