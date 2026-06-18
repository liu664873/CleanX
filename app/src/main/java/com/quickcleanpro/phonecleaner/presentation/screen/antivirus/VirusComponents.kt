package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig

private val VirusPageBrush =
    Brush.verticalGradient(
        colors = listOf(VirusBackgroundTop, VirusBackgroundBottom),
    )

internal data class VirusFeatureItem(
    @DrawableRes val iconRes: Int,
    val label: String,
)

@Composable
internal fun VirusPageScaffold(
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
    permissionGateConfig: PermissionGateConfig? = null,
    content: @Composable () -> Unit,
) {
    CleanXScaffoldPage(
        title = stringResource(R.string.anti_virus),
        modifier = modifier,
        backgroundBrush = VirusPageBrush,
        scrollEnabled = false,
        contentPadding = PaddingValues(bottom = bottomPadding),
        permissionGateConfig = permissionGateConfig,
    ) {
        content()
    }
}

@Composable
internal fun VirusFeatureCard(
    items: List<VirusFeatureItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = VirusBackgroundCard,
        shape = VirusPanelShape,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = item.label,
                        color = VirusSecondary,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(156.dp),
                    )
                }
                if (index < items.lastIndex) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = VirusDivider, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
internal fun VirusCenterBadge(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF536BFF), Color(0xFF2387F8)),
    ),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush = backgroundBrush)
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.12f), shape = CircleShape),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
internal fun VirusPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = VirusButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = VirusBlue,
            contentColor = Color.White,
        ),
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun VirusSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = VirusButtonShape,
        border = BorderStroke(1.5.dp, VirusBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = VirusBlue,
        ),
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
internal fun VirusProgressTrack(
    mode: VirusScanMode,
    progress: Float,
    hasAdbRisk: Boolean,
    appThreatCount: Int,
    fileThreatCount: Int,
) {
    val visualIcons =
        when (mode) {
            VirusScanMode.Quick ->
                listOf(
                    R.mipmap.ic_lock_small,
                    R.mipmap.ic_virus_small,
                    R.mipmap.ic_malware_small,
                )
            VirusScanMode.Deep ->
                listOf(
                    R.mipmap.ic_lock_small,
                    R.mipmap.ic_virus_small,
                    R.mipmap.ic_malware_small,
                    R.mipmap.ic_file_small,
                )
        }
    val badgeCounts =
        if (mode == VirusScanMode.Deep) {
            listOf(if (hasAdbRisk) 1 else 0, 0, appThreatCount, fileThreatCount)
        } else {
            listOf(if (hasAdbRisk) 1 else 0, 0, appThreatCount)
        }
    val circleDiameter = 56.dp
    val trackWidth = 317.dp
    val trackHeight = 12.dp
    val iconSize = 24.dp
    val badgeSize = 14.dp
    val trackHorizontalInset = 4.dp
    val clampedProgress = progress.coerceIn(0f, 1f)
    val completedStepCount = remember(mode, clampedProgress) {
        val trackWidthPx = 317f
        val circleDiameterPx = 56f
        val trackStartPx = 4f
        val trackEndPx = trackWidthPx - 4f
        val nodeCount = visualIcons.size
        val fillEndX = trackStartPx + (trackEndPx - trackStartPx) * clampedProgress
        (0 until nodeCount).count { index ->
            val circleLeft =
                if (nodeCount <= 1) 0f else (trackWidthPx - circleDiameterPx) * index / (nodeCount - 1).toFloat()
            val circleRight = circleLeft + circleDiameterPx
            fillEndX >= circleRight
        }
    }

    Box(
        modifier = Modifier
            .width(trackWidth)
            .height(circleDiameter),
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val circleDiameterPx = circleDiameter.toPx()
            val circleRadius = circleDiameterPx / 2f
            val trackHeightPx = trackHeight.toPx()
            val trackStartX = trackHorizontalInset.toPx()
            val trackEndX = size.width - trackHorizontalInset.toPx()
            val trackUsableWidth = trackEndX - trackStartX
            val fillEndX = trackStartX + trackUsableWidth * clampedProgress
            val centerY = size.height / 2f
            val nodeCount = visualIcons.size

            fun circleLeft(index: Int): Float =
                if (nodeCount <= 1) {
                    0f
                } else {
                    (size.width - circleDiameterPx) * index / (nodeCount - 1).toFloat()
                }

            fun drawNode(left: Float, color: Color) {
                drawCircle(
                    color = color,
                    radius = circleRadius,
                    center = androidx.compose.ui.geometry.Offset(left + circleRadius, centerY),
                )
            }

            fun drawConnector(
                left: Float,
                right: Float,
                color: Color,
            ) {
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(left, centerY - trackHeightPx / 2f),
                    size = androidx.compose.ui.geometry.Size(right - left, trackHeightPx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2f),
                )
            }

            fun drawActiveTrack(
                left: Float,
                right: Float,
            ) {
                if (right <= left) return
                drawRoundRect(
                    color = VirusBlueDeep,
                    topLeft = androidx.compose.ui.geometry.Offset(left, centerY - trackHeightPx / 2f),
                    size = androidx.compose.ui.geometry.Size(right - left, trackHeightPx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2f),
                )
            }

            fun drawActiveTrackTransition(
                left: Float,
                right: Float,
            ) {
                if (right <= left) return
                drawRoundRect(
                    brush =
                        Brush.horizontalGradient(
                            colors = listOf(VirusBlueDeep, Color(0xFF1483B9), VirusTrackInactiveLine),
                            startX = left,
                            endX = right,
                        ),
                    topLeft = androidx.compose.ui.geometry.Offset(left, centerY - trackHeightPx / 2f),
                    size = androidx.compose.ui.geometry.Size(right - left, trackHeightPx),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeightPx / 2f),
                )
            }

            drawConnector(
                left = trackStartX,
                right = trackEndX,
                color = VirusTrackInactiveLine,
            )
            if (clampedProgress >= 1f) {
                drawActiveTrack(trackStartX, trackEndX)
            } else if (fillEndX > trackStartX) {
                val transitionWidth = 14.dp.toPx()
                val solidEndX = (fillEndX - transitionWidth / 2f).coerceAtLeast(trackStartX)
                val transitionEndX = (fillEndX + transitionWidth / 2f).coerceAtMost(trackEndX)
                drawActiveTrack(trackStartX, solidEndX)
                drawActiveTrackTransition(solidEndX, transitionEndX)
            }

            repeat(nodeCount) { index ->
                val left = circleLeft(index)
                drawNode(left, VirusTrackInactiveLine)
                val right = left + circleDiameterPx
                when {
                    fillEndX >= right -> drawNode(left, VirusBlueDeep)
                    fillEndX > left -> {
                        clipRect(left = left, top = 0f, right = fillEndX, bottom = size.height) {
                            drawNode(left, VirusBlueDeep)
                        }
                    }
                }
            }
        }

        visualIcons.forEachIndexed { index, icon ->
            val stepOffset =
                if (visualIcons.size <= 1) {
                    0.dp
                } else {
                    (trackWidth - circleDiameter) * (index / (visualIcons.size - 1).toFloat())
                }
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .offset(
                        x = stepOffset + (circleDiameter - iconSize) / 2,
                        y = (circleDiameter - iconSize) / 2,
                    )
                    .size(iconSize),
            )

            val badgeCount = badgeCounts.getOrElse(index) { 0 }
            if (index < completedStepCount && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = stepOffset + 42.dp, y = 0.dp)
                        .size(badgeSize)
                        .clip(CircleShape)
                        .background(VirusDanger)
                        .border(width = 1.dp, color = Color.White, shape = CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AdbRiskCard(onSolve: () -> Unit) {
    RiskCardShell(
        riskIcon = R.mipmap.ic_medium,
        riskLabel = stringResource(R.string.medium_risk),
        riskColor = VirusOrange,
        solveLabel = stringResource(R.string.solve),
        onSolve = onSolve,
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_usb),
            contentDescription = null,
            modifier = Modifier.size(45.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.usb_debugging_enabled),
                color = VirusBody,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.adb_hint),
                color = VirusSecondary,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun VirusThreatCard(
    threat: VirusThreat,
    onSolve: () -> Unit,
) {
    RiskCardShell(
        riskIcon = R.mipmap.ic_high,
        riskLabel = stringResource(R.string.high_risk),
        riskColor = VirusHigh,
        solveLabel = stringResource(if (threat.isFile) R.string.delete else R.string.solve),
        onSolve = onSolve,
    ) {
        ThreatDrawableImage(
            drawable = threat.icon,
            fallback = R.mipmap.ic_virus_file,
            modifier = Modifier.size(45.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = threat.title,
                color = VirusBody,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = threat.description,
                color = VirusSecondary,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RiskCardShell(
    riskIcon: Int,
    riskLabel: String,
    riskColor: Color,
    solveLabel: String,
    onSolve: () -> Unit,
    body: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = VirusPanelShape,
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(riskIcon),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = riskLabel,
                    color = riskColor,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = solveLabel,
                    color = VirusBlue,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSolve() },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = body,
            )
        }
    }
}

@Composable
internal fun ThreatDrawableImage(
    drawable: Drawable?,
    fallback: Int,
    modifier: Modifier = Modifier,
) {
    if (drawable != null) {
        Image(
            painter = remember(drawable) {
                BitmapPainter(drawable.toBitmap().asImageBitmap())
            },
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    } else {
        Image(
            painter = painterResource(fallback),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit,
        )
    }
}
