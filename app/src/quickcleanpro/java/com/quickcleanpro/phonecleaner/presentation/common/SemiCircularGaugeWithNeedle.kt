import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SemiCircularGauge(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    isAnimating: Boolean = true,
    // 鍦嗗姬娓愬彉锛堣摑闈掕壊璋冿級
    arcStartColor: Color = Color(0xFF00D2FF),
    arcEndColor: Color = Color(0xFF3A7BD5),
    // 鎸囬拡棰滆壊
    needleColor: Color = Color(0xFFFFD700),
    // 鍒诲害棰滆壊
    tickColor: Color = Color.White.copy(alpha = 0.8f)
) {
    // 鍗婂渾瑙掑害鑼冨洿锛堟湞涓婏細浠?-90搴?鍒?+90搴︼級
    val angleRange = 180f
    val startAngle = -90f
    // 鎸囬拡鎽嗗姩鑼冨洿锛堝乏鍙冲悇 70 搴︼紝鐣欏嚭杈圭紭锛?
    val swingRange = 70f
    val needleAngle = remember { Animatable(-swingRange) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            needleAngle.animateTo(
                targetValue = swingRange,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            needleAngle.stop()
        }
    }

    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.toPx() / 2
            val centerY = size.toPx() / 2
            val radius = size.toPx() / 2 * 0.85f
            val strokeWidth = radius * 0.1f

            // 1. 缁樺埗鍗婂渾寮э紙浠呮弿杈癸紝鏃犲～鍏咃級
            val arcBrush = Brush.linearGradient(
                colors = listOf(arcStartColor, arcEndColor),
                start = Offset(centerX - radius, centerY),
                end = Offset(centerX + radius, centerY)
            )
            drawArc(
                brush = arcBrush,
                startAngle = startAngle,
                sweepAngle = angleRange,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. 缁樺埗鍒诲害绾匡紙鍧囧寑鍒嗗竷鍦ㄥ崐鍦嗗姬涓婏紝鍚戝杈愬皠锛?
            val tickCount = 11
            val tickStartRadius = radius - strokeWidth / 2 - 2.dp.toPx()
            val tickEndRadius = tickStartRadius + 10.dp.toPx()
            for (i in 0..tickCount) {
                val angle = startAngle + (angleRange * i / tickCount)
                val rad = Math.toRadians(angle.toDouble())
                val startX = centerX + tickStartRadius * cos(rad).toFloat()
                val startY = centerY + tickStartRadius * sin(rad).toFloat()
                val endX = centerX + tickEndRadius * cos(rad).toFloat()
                val endY = centerY + tickEndRadius * sin(rad).toFloat()
                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. 缁樺埗鎸囬拡锛堜粠鍦嗗績鎸囧悜鍦嗗姬锛屽甫鑿卞舰绠ご锛?
            val currentAngle = startAngle + angleRange / 2 + needleAngle.value
            rotate(degrees = currentAngle) {
                val needleLength = radius * 0.7f
                val arrowSize = radius * 0.12f

                // 鎸囬拡缁嗙嚎
                val lineStart = Offset(centerX, centerY)
                val lineEnd = Offset(centerX, centerY - needleLength)

                // 鍏夋檿
                drawLine(
                    color = needleColor.copy(alpha = 0.5f),
                    start = lineStart,
                    end = lineEnd,
                    strokeWidth = strokeWidth * 0.8f,
                    cap = StrokeCap.Round
                )
                // 涓绘寚閽堢嚎
                drawLine(
                    color = needleColor,
                    start = lineStart,
                    end = lineEnd,
                    strokeWidth = strokeWidth * 0.35f,
                    cap = StrokeCap.Round
                )

                // 鑿卞舰绠ご
                val arrowPath = Path().apply {
                    val tipX = centerX
                    val tipY = centerY - needleLength - arrowSize
                    val leftX = centerX - arrowSize * 0.7f
                    val leftY = centerY - needleLength - arrowSize * 0.5f
                    val rightX = centerX + arrowSize * 0.7f
                    val rightY = leftY
                    val baseX = centerX
                    val baseY = centerY - needleLength + arrowSize * 0.3f
                    moveTo(tipX, tipY)
                    lineTo(leftX, leftY)
                    lineTo(baseX, baseY)
                    lineTo(rightX, rightY)
                    close()
                }
                drawPath(path = arrowPath, color = needleColor)
                drawPath(
                    path = arrowPath,
                    color = Color.White.copy(alpha = 0.4f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 4. 涓績瑁呴グ灏忓渾鐐癸紙绮捐嚧锛?
            drawCircle(
                color = needleColor,
                radius = radius * 0.08f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = radius * 0.04f,
                center = Offset(centerX, centerY)
            )
        }
    }
}