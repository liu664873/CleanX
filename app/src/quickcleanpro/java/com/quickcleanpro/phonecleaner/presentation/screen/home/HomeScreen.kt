package com.quickcleanpro.phonecleaner.presentation.screen.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationSpec
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.presentation.ads.AdPlacement
import com.quickcleanpro.phonecleaner.presentation.ads.NativeAdSlot
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCard
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCardShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCompactButtonHeight
import com.quickcleanpro.phonecleaner.presentation.common.CleanXDanger
import com.quickcleanpro.phonecleaner.presentation.common.CleanXIconButtonSize
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPagePadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPillShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXRateDialog
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSegmentTabs
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSoftPanel
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSubtlePanel
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXWarning
import com.quickcleanpro.phonecleaner.presentation.common.cleanXDebouncedClick
import com.quickcleanpro.phonecleaner.presentation.common.CleanDestination
import com.quickcleanpro.phonecleaner.presentation.common.CoreDestination
import com.quickcleanpro.phonecleaner.presentation.common.SecurityDestination
import com.quickcleanpro.phonecleaner.presentation.common.route.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val PrimaryText = CleanXText
private val SecondaryText = CleanXMutedText
private val SelectedBlue = CleanXBlue

private data class CleanXTab(@param:StringRes val titleRes: Int)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    externalBlockingPromptActive: Boolean = false,
    initialTabIndex: Int = 0,
    onNavigate: (AppNavigationEvent) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val exitPromptSpec = viewModel.exitPromptSpec
    val tabs = remember {
        listOf(
            CleanXTab(R.string.home_tab_home),
            CleanXTab(R.string.home_tab_file_manager),
            CleanXTab(R.string.home_tab_toolbox)
        )
    }
    val pagerState = rememberPagerState(
        initialPage = initialTabIndex.coerceIn(0, tabs.lastIndex),
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(externalBlockingPromptActive) {
        viewModel.setExternalBlockingPromptActive(externalBlockingPromptActive)
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { viewModel.onTabInteraction() }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSummary()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = exitPromptSpec == null && !viewModel.showAutoRateDialog) {
        viewModel.requestExitPrompt()
    }

    fun exitApp() {
        viewModel.dismissExitPrompt()
        context.findActivity()?.finish()
    }

    fun openExitPromptFeature() {
        val spec = viewModel.consumeExitPromptForNavigation() ?: return
        onNavigate(AppNavigationEvent.AdDestination(spec.route))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = CleanXPagePadding)
            ) {
                Header(
                    onSettingsClick = {
                        onNavigate(AppNavigationEvent.Destination(CoreDestination.Settings.route))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CleanXSegmentTabs(
                    tabs = tabs.map { stringResource(it.titleRes) },
                    selectedIndex = pagerState.currentPage,
                    onSelected = { index ->
                        viewModel.onTabInteraction()
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    containerColor = Color.Transparent
                )

                Spacer(modifier = Modifier.height(14.dp))

                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 2,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> HomeTabContent(
                            storageInfo = summaryState.storageInfo,
                            lockedAppCount = summaryState.lockedAppCount,
                            onFeatureClick = viewModel::onFeatureClicked,
                            onNavigate = onNavigate
                        )

                        1 -> FileManagerTabContent(
                            onFeatureClick = viewModel::onFeatureClicked,
                            onNavigate = onNavigate
                        )
                        2 -> ToolBoxTabContent(
                            batteryInfo = summaryState.batteryInfo,
                            deviceModel = summaryState.deviceModel,
                            androidVersion = summaryState.androidVersion,
                            onFeatureClick = viewModel::onFeatureClicked,
                            onNavigate = onNavigate
                        )
                    }
                }
            }
        }
    }

    if (viewModel.showAutoRateDialog) {
        CleanXRateDialog(onDismiss = viewModel::dismissAutoRateDialog)
    }

    if (exitPromptSpec != null) {
        HomeExitPromptDialog(
            spec = exitPromptSpec,
            onExit = ::exitApp,
            onViewNow = ::openExitPromptFeature
        )
    }
}

@Composable
private fun Header(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            color = PrimaryText,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(CleanXIconButtonSize)
                .clip(CleanXPillShape)
                .cleanXDebouncedClick(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = PrimaryText,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun HomeExitPromptDialog(
    spec: ToolNotificationSpec,
    onExit: () -> Unit,
    onViewNow: () -> Unit
) {
    if (spec.route == Screen.NotificationBar.route) {
        NotificationBarExitPromptDialog(
            onEnableNow = onViewNow,
            onClose = onExit
        )
    } else {
        ToolExitPromptDialog(
            spec = spec,
            onExit = onExit,
            onViewNow = onViewNow
        )
    }
}

@Composable
private fun ToolExitPromptDialog(
    spec: ToolNotificationSpec,
    onExit: () -> Unit,
    onViewNow: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .widthIn(max = 343.dp),
            color = Color.White,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(spec.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(toolExitPromptMessageRes(spec.route)),
                    color = CleanXText,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(CleanXPillShape)
                            .clickable { onExit() }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.exit),
                            color = Color(0xFFB3BDCB),
                            fontSize = 15.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = onViewNow,
                        modifier = Modifier.height(34.dp),
                        shape = CleanXPillShape,
                        border = BorderStroke(1.dp, CleanXBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = CleanXBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 19.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.view_now),
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationBarExitPromptDialog(
    onEnableNow: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .widthIn(max = 343.dp),
            color = Color.White,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.exit_prompt_notification_title),
                    color = CleanXText,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(14.dp))
                NotificationPromptTip(text = stringResource(R.string.exit_prompt_notification_tip_block))
                Spacer(modifier = Modifier.height(13.dp))
                NotificationPromptTip(text = stringResource(R.string.exit_prompt_notification_tip_matters))
                Spacer(modifier = Modifier.height(13.dp))
                NotificationPromptTip(text = stringResource(R.string.exit_prompt_notification_tip_focus))
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onEnableNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = CleanXPillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CleanXBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.enable_now),
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(13.dp))
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = CleanXPillShape,
                    border = BorderStroke(1.dp, CleanXBlue),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = CleanXBlue
                    )
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPromptTip(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(19.dp)
                .clip(CleanXPillShape)
                .background(CleanXBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = text,
            color = CleanXMutedText,
            fontSize = 16.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@StringRes
private fun toolExitPromptMessageRes(route: String): Int =
    when (route) {
        Screen.DeviceInfo.route -> R.string.exit_prompt_device_info
        Screen.Scan.route -> R.string.exit_prompt_junk_removal
        Screen.BatteryInfo.route -> R.string.exit_prompt_battery_info
        Screen.NetworkScan.route -> R.string.exit_prompt_network_scan
        Screen.NetworkUsage.route -> R.string.exit_prompt_network_usage
        else -> R.string.exit_prompt_device_info
    }

@Composable
private fun HomeTabContent(
    storageInfo: StorageInfo,
    lockedAppCount: Int,
    onFeatureClick: () -> Unit,
    onNavigate: (AppNavigationEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        StorageSummary(
            storageInfo = storageInfo,
            onFeatureClick = onFeatureClick,
            onNavigate = onNavigate
        )

        NativeAdSlot(placement = AdPlacement.HomeNative)

        Spacer(modifier = Modifier.height(20.dp))

        GradientCleanCard(
            title = stringResource(R.string.home_virus_title),
            description = stringResource(R.string.home_virus_desc),
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFF73A8FF), Color(0xFFBCE4FF))
            ),
            iconAlignment = Alignment.TopStart,
            onClick = {
                onFeatureClick()
                onNavigate(AppNavigationEvent.AdDestination(SecurityDestination.AntiVirus.route))
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        GradientCleanCard(
            title = pluralStringResource(
                R.plurals.locked_apps_count,
                lockedAppCount,
                lockedAppCount
            ),
            description = stringResource(R.string.home_locked_apps_desc),
            gradient = Brush.horizontalGradient(
                listOf(Color(0xFF8E7AF2), Color(0xFFB7AEFF))
            ),
            iconAlignment = Alignment.TopEnd,
            onClick = {
                onFeatureClick()
                onNavigate(AppNavigationEvent.AdDestination(SecurityDestination.AppLock.route))
            }
        )
    }
}

@Composable
private fun StorageSummary(
    storageInfo: StorageInfo,
    onFeatureClick: () -> Unit,
    onNavigate: (AppNavigationEvent) -> Unit
) {
    CleanXCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp),
        contentPadding = PaddingValues(16.dp),
        containerColor = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stringResource(R.string.home_storage_label),
                        color = PrimaryText,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = storageInfo.formattedUsed,
                            color = SelectedBlue,
                            fontSize = 19.sp,
                            lineHeight = 21.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "/${storageInfo.formattedTotal}",
                            color = SecondaryText,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                StorageProgress(usagePercent = storageInfo.usagePercent)

                Button(
                    onClick = {
                        onFeatureClick()
                        onNavigate(AppNavigationEvent.AdDestination(CleanDestination.Scan.route))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = CleanXPillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = removeJunkButtonColor(storageInfo.usagePercent),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_remove_junk),
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

//            Spacer(modifier = Modifier.width(14.dp))
            Image(
                painter = painterResource(id = R.drawable.trash_can),
                contentDescription = null,
                modifier = Modifier.size(104.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun removeJunkButtonColor(usagePercent: Int): Color {
    val usageRatio = usagePercent.coerceIn(0, 100) / 100f
    return when {
        usageRatio < 1f / 3f -> CleanXBlue
        usageRatio < 2f / 3f -> CleanXWarning
        else -> CleanXDanger
    }
}

@Composable
private fun StorageProgress(usagePercent: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(CleanXPillShape)
            .background(CleanXSubtlePanel)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(usagePercent.coerceIn(0, 100) / 100f)
                .height(12.dp)
                .clip(CleanXPillShape)
                .background(removeJunkButtonColor(usagePercent))
        )
    }
}

@Composable
private fun GradientCleanCard(
    title: String,
    description: String,
    gradient: Brush,
    iconAlignment: Alignment,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 154.dp)
            .clip(CleanXCardShape)
            .background(gradient)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        val iconModifier = Modifier
            .align(iconAlignment)
            .padding(
                start = if (iconAlignment == Alignment.TopStart) 8.dp else 0.dp,
                end = if (iconAlignment == Alignment.TopEnd) 12.dp else 0.dp,
                top = 8.dp
            )
            .size(56.dp)

        ShieldIllustration(modifier = iconModifier)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (iconAlignment == Alignment.TopStart) 92.dp else 4.dp,
                    top = 14.dp,
                    end = if (iconAlignment == Alignment.TopEnd) 96.dp else 4.dp
                )
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }

        Button(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(CleanXCompactButtonHeight),
            shape = CleanXPillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = PrimaryText
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.clean_now),
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ShieldIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawShield()
        drawCircle(
            color = Color.White,
            radius = size.minDimension * 0.23f,
            center = Offset(size.width * 0.77f, size.height * 0.73f)
        )
        drawCircle(
            color = Color(0xFF36C8D4),
            radius = size.minDimension * 0.25f,
            center = Offset(size.width * 0.77f, size.height * 0.73f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        drawLine(
            color = Color(0xFF4EE384),
            start = Offset(size.width * 0.67f, size.height * 0.72f),
            end = Offset(size.width * 0.74f, size.height * 0.8f),
            strokeWidth = 5.dp.toPx()
        )
        drawLine(
            color = Color(0xFF4EE384),
            start = Offset(size.width * 0.74f, size.height * 0.8f),
            end = Offset(size.width * 0.9f, size.height * 0.64f),
            strokeWidth = 5.dp.toPx()
        )
    }
}

private fun DrawScope.drawShield() {
    val path = Path().apply {
        moveTo(size.width * 0.5f, size.height * 0.04f)
        cubicTo(
            size.width * 0.25f,
            size.height * 0.1f,
            size.width * 0.13f,
            size.height * 0.13f,
            size.width * 0.13f,
            size.height * 0.13f
        )
        lineTo(size.width * 0.13f, size.height * 0.47f)
        cubicTo(
            size.width * 0.13f,
            size.height * 0.7f,
            size.width * 0.32f,
            size.height * 0.86f,
            size.width * 0.5f,
            size.height * 0.95f
        )
        cubicTo(
            size.width * 0.68f,
            size.height * 0.86f,
            size.width * 0.87f,
            size.height * 0.7f,
            size.width * 0.87f,
            size.height * 0.47f
        )
        lineTo(size.width * 0.87f, size.height * 0.13f)
        cubicTo(
            size.width * 0.87f,
            size.height * 0.13f,
            size.width * 0.75f,
            size.height * 0.1f,
            size.width * 0.5f,
            size.height * 0.04f
        )
        close()
    }
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE9FDFF), Color(0xFF50B7FF)),
            center = Offset(size.width * 0.42f, size.height * 0.32f),
            radius = size.minDimension * 0.65f
        )
    )
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.55f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
    )

    val bolt = Path().apply {
        moveTo(size.width * 0.5f, size.height * 0.28f)
        lineTo(size.width * 0.35f, size.height * 0.58f)
        lineTo(size.width * 0.52f, size.height * 0.56f)
        lineTo(size.width * 0.43f, size.height * 0.76f)
        lineTo(size.width * 0.67f, size.height * 0.43f)
        lineTo(size.width * 0.5f, size.height * 0.46f)
        close()
    }
    drawPath(
        path = bolt,
        brush = Brush.verticalGradient(listOf(Color(0xFF5F75FF), Color(0xFF20E6F4)))
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

@Preview(showBackground = true, backgroundColor = 0xFFF7FAFD)
@Composable
private fun PreviewHomeScreen() {
    QuickCleanTheme {
        HomeScreen()
    }
}
