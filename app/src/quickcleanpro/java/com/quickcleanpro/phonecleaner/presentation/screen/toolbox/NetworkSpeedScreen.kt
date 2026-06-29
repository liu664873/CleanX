package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultCheckIcon
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultScreen
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme

@Composable
fun NetworkSpeedScreen(
    onBack: () -> Unit,
    onResultBack: () -> Unit = {},
    onNavigateTool: (String) -> Unit
) {
    NetworkSpeedScreenState(
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = viewModel()
    )
}

@Composable
internal fun NetworkSpeedRoute(
    onBack: () -> Unit,
    onResultBack: () -> Unit = {},
    onNavigateTool: (String) -> Unit
) {
    NetworkSpeedScreenState(
        onBack = onBack,
        onResultBack = onResultBack,
        onNavigateTool = onNavigateTool,
        viewModel = koinViewModel()
    )
}

@Composable
private fun NetworkSpeedScreenState(
    onBack: () -> Unit,
    onResultBack: () -> Unit,
    onNavigateTool: (String) -> Unit,
    viewModel: NetworkSpeedViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.speedState

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNetworkStateUntilNetworkAvailable()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (state == SpeedState.Done) {
        BackHandler(onBack = onResultBack)

        CommonResultScreen(
            titleRes = R.string.network_speed,
            onBack = onResultBack,
            onNavigateTool = onNavigateTool,
            excludedToolRoutes = setOf(Screen.NetworkSpeed.route)
        ) {
            NetworkSpeedResultContent(
                download = uiState.downloadLabel,
                upload = uiState.uploadLabel
            )
        }
        return
    }

    ToolboxScaffold(
        titleRes = R.string.network_speed,
        onBack = onBack,
        bottom = {
            PrimaryBottomButton(
                text = stringResource(
                    if (state == SpeedState.Running) R.string.stop else R.string.run_speed_test
                ),
                enabled = state == SpeedState.Running || uiState.hasNetwork,
                onClick = {
                    if (state == SpeedState.Running) {
                        viewModel.stopSpeedTest()
                    } else {
                        viewModel.runSpeedTest()
                    }
                }
            )
        }
    ) {
        NetworkInfoCard(network = if (state == SpeedState.Running) uiState.networkInfo else NetworkInfo.EMPTY)
        Spacer(modifier = Modifier.height(22.dp))

        if (!uiState.hasNetwork && state != SpeedState.Running) {
            PermissionPromptCard(
                title = stringResource(R.string.no_network_connection),
                description = stringResource(R.string.network_speed_no_connection_desc),
                action = stringResource(R.string.settings),
                onClick = { openSystemSettings(context, Settings.ACTION_WIRELESS_SETTINGS) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        SpeedMetricCard(
            download = uiState.downloadLabel,
            upload = uiState.uploadLabel,
            isDownloadTesting = uiState.isDownloadTesting,
            isUploadTesting = uiState.isUploadTesting
        )

        if (state == SpeedState.Running) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SemiCircularGauge(
                    modifier = Modifier.size(280.dp),   // 浠?200.dp 鏀逛负 280.dp锛屾洿澶ф洿閱掔洰
                    isAnimating = true,
                    arcStartColor = Color(0xFF00C9FF),
                    arcEndColor = Color(0xFF92FE9D),
                    needleColor = Color(0xFF3366FF),
                    tickColor = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
        }
    }
}

private fun openSystemSettings(context: Context, action: String) {
    runCatching { context.startActivity(Intent(action)) }
}

@Composable
private fun NetworkSpeedResultContent(
    download: String,
    upload: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CommonResultCheckIcon()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.speed_test_complete),
            color = CleanXMutedText,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier
            .height(22.dp)
            .fillMaxWidth())
        SpeedMetricCard(download = download, upload = upload)
    }
}

/**
 * 鍗婅疆鐩樻寚閽堟壂鎻忓姩鐢荤粍浠讹紙涓婂崐鍦嗗姬 + 鍔犵矖鎻忚竟锛? * 鎸囬拡锛氭牴閮ㄧ矖锛堝渾鐐圭姸锛夆啋 灏栭儴缁嗭紝璐村悎鈥滃渾鐐圭矖鍒板渾寮х粏鈥濈殑闇€姹? */
@Composable
fun SemiCircularGauge(
    modifier: Modifier = Modifier,
    isAnimating: Boolean,
    arcStartColor: Color,
    arcEndColor: Color,
    needleColor: Color,
    tickColor: Color
) {
    val angleState = remember { Animatable(-90f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            while (true) {
                angleState.animateTo(90f, tween(800))
                angleState.animateTo(-90f, tween(800))
            }
        }
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val radius = width * 0.45f
        val centerX = width / 2f
        val centerY = height * 0.65f

        // 1. 鍔犵矖涓婂崐鍦嗗姬锛堣摑鑹叉笎鍙橈級
        drawArc(
            brush = Brush.horizontalGradient(colors = listOf(arcStartColor, arcEndColor)),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 24f, cap = StrokeCap.Round)
        )

        val tickCount = 11
        for (i in 0..tickCount) {
            val ratio = i.toFloat() / tickCount
            val arcAngle = 180f * (1 - ratio)
            val innerRadius = radius - 8f
            val outerRadius = radius + 8f
            val startPoint = getPointOnCircle(centerX, centerY, innerRadius, arcAngle)
            val endPoint = getPointOnCircle(centerX, centerY, outerRadius, arcAngle)
            drawLine(color = tickColor, start = startPoint, end = endPoint, strokeWidth = 4f)
        }

        // 3. 缁樺埗鎸囬拡锛堟牴閮ㄧ矖 鈫?灏栭儴缁嗭級
        rotate(degrees = angleState.value, pivot = Offset(centerX, centerY)) {
            val needleLength = radius * 0.8f
            val rootWidth = 20f
            val tipWidth = 4f
            // 姊舰鎸囬拡锛氬簳閮紙鍦嗗績澶勶級瀹斤紝椤堕儴锛堝渾寮у锛夌獎
            val leftRoot = Offset(centerX - rootWidth / 2, centerY)
            val rightRoot = Offset(centerX + rootWidth / 2, centerY)
            val leftTip = Offset(centerX - tipWidth / 2, centerY - needleLength)
            val rightTip = Offset(centerX + tipWidth / 2, centerY - needleLength)

            val needlePath = Path().apply {
                moveTo(leftRoot.x, leftRoot.y)
                lineTo(rightRoot.x, rightRoot.y)
                lineTo(rightTip.x, rightTip.y)
                lineTo(leftTip.x, leftTip.y)
                close()
            }
            drawPath(path = needlePath, color = needleColor)

            // 涓績澶у渾鐐癸紙寮哄寲绮楀渾鎰燂級
            drawCircle(color = needleColor, radius = 14f, center = Offset(centerX, centerY))
            drawCircle(color = Color.White, radius = 5f, center = Offset(centerX, centerY))
        }
    }
}

private fun DrawScope.getPointOnCircle(
    cx: Float,
    cy: Float,
    radius: Float,
    angleDeg: Float
): Offset {
    val angleRad = Math.toRadians(angleDeg.toDouble())
    val x = cx + radius * Math.cos(angleRad).toFloat()
    val y = cy + radius * Math.sin(angleRad).toFloat()
    return Offset(x, y)
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewNetworkSpeedScreen() {
    QuickCleanTheme { NetworkSpeedScreen(onBack = {}, onResultBack = {}, onNavigateTool = {}) }
}
