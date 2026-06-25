package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme
import androidx.compose.runtime.ReadOnlyComposable

private val StorageBlue: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.storageBlue
private val StorageYellow: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.storageYellow
private val StorageOrange: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.storageOrange
private val VirusCardBlue: Brush @Composable @ReadOnlyComposable get() =
    Brush.linearGradient(
        colors = listOf(Color(0xFF90B2FB), Color(0xFF88ABFB)),
    )
private val AppLockCardBlue: Brush @Composable @ReadOnlyComposable get() =
    Brush.linearGradient(
        colors = listOf(Color(0xFF90E7FB), Color(0xFF88DAFB)),
    )


@Composable
fun HomeTabContent(
    summaryState: HomeSummaryUiState,
    onFeatureClick: () -> Unit = {},
) {
    val router = LocalRouter.current
    val storageInfo = summaryState.storageInfo
    val usedStorageText =
        if (summaryState.isLoading && storageInfo.totalBytes <= 0L) {
            "--"
        } else {
            storageInfo.formattedUsed
        }
    val totalStorageText =
        if (summaryState.isLoading && storageInfo.totalBytes <= 0L) {
            "--"
        } else {
            storageInfo.formattedTotal
        }
    val storageProgress = (storageInfo.usagePercent / 100f).coerceIn(0f, 1f)
    val storageCardColor = storageCardColor(storageProgress)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = storageCardColor,
                shape = RoundedCornerShape(20.dp),
            ) {}

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_storage_label),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = usedStorageText,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "/$totalStorageText",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                RoundedProgressBar(
                    progress = storageProgress,
                    width = 311.dp,
                    height = 12.dp,
                    trackColor = Color(0xA6FFFFFF),
                    fillColor = Color.White,
                )
                Spacer(modifier = Modifier.height(27.dp))
                Button(
                    onClick = {
                        onFeatureClick()
                        router.navigate(Screen.Scan)
                    },
                    shape = RoundedCornerShape(28.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = storageCardColor,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_remove_junk),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = null,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-16).dp, y = (-24).dp)
                        .size(width = 90.dp, height = 84.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        EntryCard(
            gradient = VirusCardBlue,
            imageRes = R.drawable.virus_shield,
            imageWidth = 98.dp,
            imageHeight = 81.dp,
            title = stringResource(R.string.home_virus_title),
            description = stringResource(R.string.home_virus_desc),
            onClick = {
                onFeatureClick()
                router.navigate(Screen.AntiVirus)
            },
        )

        Spacer(modifier = Modifier.height(15.dp))

        EntryCard(
            gradient = AppLockCardBlue,
            imageRes = R.drawable.app_lock,
            imageWidth = 81.dp,
            imageHeight = 81.dp,
            title = stringResource(R.string.home_app_lock_title),
            description = stringResource(R.string.home_app_lock_desc),
            onClick = {
                onFeatureClick()
                router.navigate(Screen.AppLock)
            },
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun storageCardColor(progress: Float): Color =
    when {
        progress < 1f / 3f -> StorageBlue
        progress >= 2f / 3f -> StorageOrange
        else -> StorageYellow
    }

@Composable
private fun EntryCard(
    gradient: Brush,
    imageRes: Int,
    imageWidth: Dp,
    imageHeight: Dp,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(brush = gradient, shape = shape)
                .clip(shape),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.width(193.dp)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = description,
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    )
                }
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(width = imageWidth, height = imageHeight),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = CleanXBlue,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
            ) {
                Text(
                    text = stringResource(R.string.clean_now),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
