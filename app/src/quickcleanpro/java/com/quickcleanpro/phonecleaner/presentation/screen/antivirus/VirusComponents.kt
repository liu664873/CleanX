package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold

@Composable
internal fun VirusPageScaffold(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = VirusBackground,
    bottomPadding: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    CleanXScaffold(
        title = stringResource(R.string.anti_virus),
        onBack = onBack,
        modifier = modifier,
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(bottom = bottomPadding)
        ) {
            content()
        }
    }
}

@Composable
internal fun VirusFeatureRow(
    icon: Int,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(VirusPanelShape)
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 15.dp)
                .size(25.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = label,
            color = VirusSecondary,
            fontSize = 15.sp,
            modifier = Modifier.padding(end = 15.dp)
        )
    }
}

@Composable
internal fun VirusPrimaryButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VirusBlue,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun VirusProgressTrack(
    mode: VirusScanMode,
    progress: Float,
    hasAdbRisk: Boolean,
    appThreatCount: Int,
    fileThreatCount: Int
) {
    val icons = if (mode == VirusScanMode.Deep) {
        listOf(R.mipmap.ic_lock_small, R.mipmap.ic_virus_small, R.mipmap.ic_malware_small, R.mipmap.ic_file_small)
    } else {
        listOf(R.mipmap.ic_lock_small, R.mipmap.ic_virus_small, R.mipmap.ic_malware_small)
    }
    val badgeCounts = if (mode == VirusScanMode.Deep) {
        listOf(if (hasAdbRisk) 1 else 0, 0, appThreatCount, fileThreatCount)
    } else {
        listOf(if (hasAdbRisk) 1 else 0, 0, appThreatCount)
    }
    val width = if (mode == VirusScanMode.Deep) 330.dp else 240.dp
    val circleDiameter = 60.dp
    val connectorWidth = 30.dp
    val iconSize = 30.dp
    val badgeSize = 15.dp
    val completedStepCount = remember(mode, progress) {
        val circleDiameter = 60f
        val connectorWidth = 30f
        val circleCount = icons.size
        val totalWidth = circleCount * circleDiameter + (circleCount - 1) * connectorWidth
        val fillEndX = totalWidth * progress.coerceIn(0f, 1f)

        (0 until circleCount).count { index ->
            val circleRight = index * (circleDiameter + connectorWidth) + circleDiameter
            fillEndX >= circleRight
        }
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(60.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            val circleCount = icons.size
            val circleDiameter = 60.dp.toPx()
            val circleRadius = circleDiameter / 2f
            val connectorWidth = 30.dp.toPx()
            val connectorHeight = 15.dp.toPx()
            val connectorOverlap = 2.dp.toPx()
            val centerY = size.height / 2f
            val totalWidth = circleCount * circleDiameter + (circleCount - 1) * connectorWidth
            val startX = ((size.width - totalWidth) / 2f).coerceAtLeast(0f)
            val fillEndX = startX + totalWidth * progress.coerceIn(0f, 1f)
            val defaultTrackColor = Color(0xFF8392A7)

            fun drawConnector(left: Float, right: Float, color: Color) {
                drawRect(
                    color = color,
                    topLeft = Offset(left, centerY - connectorHeight / 2f),
                    size = Size(right - left, connectorHeight)
                )
            }

            fun drawCircleAt(left: Float, color: Color) {
                drawCircle(
                    color = color,
                    radius = circleRadius,
                    center = Offset(left + circleRadius, centerY)
                )
            }

            repeat(circleCount) { index ->
                val circleLeft = startX + index * (circleDiameter + connectorWidth)
                drawCircleAt(circleLeft, defaultTrackColor)

                if (index < circleCount - 1) {
                    val connectorLeft = circleLeft + circleDiameter - connectorOverlap
                    val connectorRight = circleLeft + circleDiameter + connectorWidth + connectorOverlap
                    drawConnector(connectorLeft, connectorRight, defaultTrackColor)
                }
            }

            repeat(circleCount) { index ->
                val circleLeft = startX + index * (circleDiameter + connectorWidth)
                val circleRight = circleLeft + circleDiameter

                when {
                    fillEndX >= circleRight -> drawCircleAt(circleLeft, VirusBlue)
                    fillEndX > circleLeft -> clipRect(left = circleLeft, top = 0f, right = fillEndX, bottom = size.height) {
                        drawCircleAt(circleLeft, VirusBlue)
                    }
                }

                if (index < circleCount - 1) {
                    val connectorLeft = circleRight - connectorOverlap
                    val connectorRight = circleRight + connectorWidth + connectorOverlap
                    if (fillEndX > connectorLeft) {
                        drawConnector(connectorLeft, fillEndX.coerceAtMost(connectorRight), VirusBlue)
                    }
                }
            }
        }

        icons.forEachIndexed { index, icon ->
            val circleLeft = (circleDiameter + connectorWidth) * index
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier
                    .offset(x = circleLeft + 15.dp, y = 15.dp)
                    .size(iconSize)
            )
            val badgeCount = badgeCounts.getOrElse(index) { 0 }
            if (index < completedStepCount && badgeCount > 0) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .offset(x = circleLeft + 40.dp, y = 0.dp)
                        .size(badgeSize)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4B4B))
                )
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
        onSolve = onSolve
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_usb),
            contentDescription = null,
            modifier = Modifier.size(45.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.usb_debugging_enabled),
                color = VirusTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.adb_hint),
                color = VirusSecondary,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun VirusThreatCard(
    threat: VirusThreat,
    onSolve: () -> Unit
) {
    RiskCardShell(
        riskIcon = R.mipmap.ic_high,
        riskLabel = stringResource(R.string.high_risk),
        riskColor = VirusHigh,
        solveLabel = stringResource(if (threat.isFile) R.string.delete else R.string.solve),
        onSolve = onSolve
    ) {
        ThreatDrawableImage(
            drawable = threat.icon,
            fallback = R.mipmap.ic_virus_file,
            modifier = Modifier.size(45.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = threat.title,
                color = VirusTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = threat.description,
                color = VirusSecondary,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
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
    body: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(VirusPanelShape)
            .background(Color.White)
            .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(riskIcon),
                contentDescription = null,
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = riskLabel,
                color = riskColor,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = solveLabel,
                color = VirusBlue,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSolve() }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            content = body
        )
    }
}

@Composable
internal fun ThreatDrawableImage(
    drawable: Drawable?,
    fallback: Int,
    modifier: Modifier = Modifier
) {
    if (drawable != null) {
        Image(
            painter = remember(drawable) {
                BitmapPainter(drawable.toBitmap().asImageBitmap())
            },
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Image(
            painter = painterResource(fallback),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    }
}
