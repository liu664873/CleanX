package com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationcleaner.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXFeature
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationBarPage
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationBarUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationCleanerViewModel
import kotlinx.coroutines.delay

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

private enum class NotificationCleanerPage {
    Onboarding,
    Scanning,
    Complete,
    Status,
    Settings,
}

private data class NotificationQuickActionSpec(
    val iconRes: Int,
    val labelRes: Int,
    val onClick: () -> Unit,
)

@Composable
internal fun NotificationCleanerScreenState(
    onBack: () -> Unit,
    onDeviceInfo: () -> Unit,
    onJunkRemoval: () -> Unit,
    onBatteryInfo: () -> Unit,
    onNetworkScan: () -> Unit,
    onNetworkUsage: () -> Unit,
    viewModel: NotificationCleanerViewModel,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingCompletion by rememberSaveable { mutableStateOf(false) }
    var showComplete by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshState()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(uiState.hasAccess, uiState.enabled) {
        if (!uiState.hasAccess) {
            pendingCompletion = false
            showComplete = false
        } else if (!uiState.enabled && !pendingCompletion) {
            showComplete = false
        }
    }

    val page =
        when {
            uiState.page == NotificationBarPage.Settings -> NotificationCleanerPage.Settings
            showComplete -> NotificationCleanerPage.Complete
            pendingCompletion || uiState.page == NotificationBarPage.Scanning -> NotificationCleanerPage.Scanning
            !uiState.enabled -> NotificationCleanerPage.Onboarding
            else -> NotificationCleanerPage.Status
        }

    CleanXScaffoldPage(
        title = stringResource(R.string.notification_cleaner),
        permissionFeature = CleanXFeature.NotificationCleaner,
        scrollEnabled = false,
        onBack = {
            if (page == NotificationCleanerPage.Settings) {
                viewModel.leaveSettings()
            } else {
                onBack()
            }
        },
        actions = {
            if (page != NotificationCleanerPage.Settings) {
                IconButton(
                    onClick = {
                        showComplete = false
                        pendingCompletion = false
                        viewModel.showSettings()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings),
                        tint = Navy,
                    )
                }
            }
        },
    ) {
        when {
            !uiState.isInitialized -> NotificationScanningContent(
                blockedCount = uiState.blockedCount,
                onFinished = viewModel::finishScanning,
            )
            uiState.errorMessage != null -> NotificationErrorContent(
                message = uiState.errorMessage.orEmpty(),
                onRetry = viewModel::refreshState,
            )
            else -> {
                when (page) {
                    NotificationCleanerPage.Onboarding -> NotificationOnboardingContent(
                        onEnableClick = {
                            showComplete = false
                            pendingCompletion = true
                            if (viewModel.setBlockingEnabled(true)) {
                                pendingCompletion = false
                            }
                        },
                    )
                    NotificationCleanerPage.Scanning -> NotificationScanningContent(
                        blockedCount = uiState.blockedCount,
                        onFinished = {
                            viewModel.finishScanning()
                            if (pendingCompletion) {
                                pendingCompletion = false
                                showComplete = true
                            }
                        },
                    )
                    NotificationCleanerPage.Complete -> NotificationCompleteContent(
                        blockedCount = uiState.blockedCount,
                        onConfirm = { showComplete = false },
                    )
                    NotificationCleanerPage.Status -> NotificationStatusContent(
                        uiState = uiState,
                        onOpenSettings = viewModel::showSettings,
                        onDeviceInfo = onDeviceInfo,
                        onJunkRemoval = onJunkRemoval,
                        onBatteryInfo = onBatteryInfo,
                        onNetworkScan = onNetworkScan,
                        onNetworkUsage = onNetworkUsage,
                    )
                    NotificationCleanerPage.Settings -> NotificationSettingsContent(
                        uiState = uiState,
                        onEnabledChange = { checked ->
                            showComplete = false
                            pendingCompletion = false
                            viewModel.setBlockingEnabled(checked)
                        },
                        onTogglePackage = viewModel::togglePackage,
                        onDone = viewModel::leaveSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationOnboardingContent(onEnableClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.notification_overload_title),
            color = Navy,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(26.dp))
        Box(
            modifier = Modifier.size(width = 320.dp, height = 360.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedNotificationFrames(modifier = Modifier.size(270.dp))
            Sparkle(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 20.dp),
            )
            Sparkle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 54.dp, start = 24.dp),
                small = true,
            )
            Sparkle(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 18.dp, bottom = 64.dp),
                small = true,
            )
            Sparkle(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 6.dp, bottom = 140.dp),
                small = true,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.notification_tidy_one_tap),
            color = Navy,
            fontSize = 22.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(28.dp))
        CleanXPrimaryButton(
            text = stringResource(R.string.notification_one_tap_tidy_up),
            onClick = onEnableClick,
        )
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun NotificationScanningContent(
    blockedCount: Int,
    onFinished: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(1800L)
        onFinished()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier.size(252.dp),
            contentAlignment = Alignment.Center,
        ) {
            CleanSpiralAnimation(
                modifier = Modifier.size(252.dp),
                centerSize = 118.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BellBadge(size = 78.dp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = blockedCount.toString(),
                        color = CleanXBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.notification_loading_animation),
                        color = NavyMuted,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCompleteContent(
    blockedCount: Int,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Image(
            painter = painterResource(R.drawable.ic_ok),
            contentDescription = null,
            modifier = Modifier.size(132.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.notification_tidy_complete),
            color = Navy,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shape = RoundedCornerShape(CardRadius),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BellBadge(size = 64.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = pluralStringResource(
                            R.plurals.notifications_count,
                            blockedCount,
                            blockedCount,
                        ),
                        color = Navy,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.notification_collected_suffix),
                        color = NavyMuted,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        CleanXPrimaryButton(
            text = stringResource(R.string.ok),
            onClick = onConfirm,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NotificationStatusContent(
    uiState: NotificationBarUiState,
    onOpenSettings: () -> Unit,
    onDeviceInfo: () -> Unit,
    onJunkRemoval: () -> Unit,
    onBatteryInfo: () -> Unit,
    onNetworkScan: () -> Unit,
    onNetworkUsage: () -> Unit,
) {
    val actions =
        remember(
            onDeviceInfo,
            onJunkRemoval,
            onBatteryInfo,
            onNetworkScan,
            onNetworkUsage,
            onOpenSettings,
        ) {
            listOf(
                NotificationQuickActionSpec(R.drawable.ic_n_device_info, R.string.device_info, onDeviceInfo),
                NotificationQuickActionSpec(R.drawable.ic_n_junk_removal, R.string.junk_removal, onJunkRemoval),
                NotificationQuickActionSpec(R.drawable.ic_n_battery_info, R.string.battery_info, onBatteryInfo),
                NotificationQuickActionSpec(R.drawable.ic_n_network_scan, R.string.network_scan, onNetworkScan),
                NotificationQuickActionSpec(R.drawable.ic_n_network_usage, R.string.network_usage, onNetworkUsage),
                NotificationQuickActionSpec(
                    R.drawable.ic_n_notification_cleaner,
                    R.string.notification_cleaner,
                    onOpenSettings,
                ),
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shape = RoundedCornerShape(CardRadius),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BellBadge(size = 68.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        if (uiState.enabled) {
                            R.string.notification_status_enabled
                        } else {
                            R.string.notification_status_disabled
                        },
                    ),
                    color = Navy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.notifications_blocked_count,
                        uiState.blockedCount,
                        uiState.blockedCount,
                    ),
                    color = Navy,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.notification_showing_limit),
                    color = NavyMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        actions.chunked(3).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowActions.forEach { action ->
                    NotificationQuickAction(
                        iconRes = action.iconRes,
                        label = stringResource(action.labelRes),
                        modifier = Modifier.weight(1f),
                        onClick = action.onClick,
                    )
                }
                repeat(3 - rowActions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (uiState.blockedCountsByPackage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Divider15)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.notification_blocked_notifications),
                color = Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            val appsByPackage = remember(uiState.apps) { uiState.apps.associateBy { it.packageName } }
            uiState.blockedCountsByPackage.entries
                .filter { it.value > 0 }
                .sortedByDescending { it.value }
                .forEach { (packageName, count) ->
                    NotificationBlockedAppRow(
                        app = appsByPackage[packageName] ?: BlockableNotificationApp(packageName, packageName),
                        count = count,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
        }
    }
}

@Composable
private fun NotificationQuickAction(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .clickable(onClick = onClick),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(30.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Navy,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun NotificationSettingsContent(
    uiState: NotificationBarUiState,
    onEnabledChange: (Boolean) -> Unit,
    onTogglePackage: (String) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shape = RoundedCornerShape(CardRadius),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.notification_blocked_notifications),
                        color = Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.notification_block_settings_desc),
                        color = NavyMuted,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                    )
                }
                Switch(
                    checked = uiState.enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CleanXBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD8DDE6),
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        uiState.apps.forEach { app ->
            NotificationAppRow(
                app = app,
                selected = app.packageName in uiState.selectedPackages,
                onToggleSelection = { onTogglePackage(app.packageName) },
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        CleanXPrimaryButton(
            text = stringResource(R.string.ok),
            onClick = onDone,
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NotificationAppRow(
    app: BlockableNotificationApp,
    selected: Boolean,
    onToggleSelection: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleSelection),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppInitialBadge(text = app.appName.take(1).ifBlank { "A" })
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = app.appName,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            SelectionBubble(selected = selected)
        }
    }
}

@Composable
private fun NotificationBlockedAppRow(
    app: BlockableNotificationApp,
    count: Int,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppInitialBadge(text = app.appName.take(1).ifBlank { "A" })
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = app.appName,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = count.toString(),
                color = NavyMuted,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun NotificationErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.error),
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                color = NavyMuted,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
            CleanXPrimaryButton(
                text = stringResource(R.string.retry),
                onClick = onRetry,
            )
        }
    }
}

@Composable
private fun AnimatedNotificationFrames(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PhoneOutline(modifier = Modifier.fillMaxSize())

        NotificationPreviewCard(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 26.dp),
            lineCount = 8,
        )
        NotificationPreviewCard(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 132.dp),
            lineCount = 6,
        )
        NotificationPreviewCard(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(top = 224.dp),
            lineCount = 5,
        )
    }
}

@Composable
private fun NotificationPreviewCard(
    modifier: Modifier,
    lineCount: Int,
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(lineCount) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = if (index == lineCount - 1) 0.75f else 1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (index % 2 == 0) {
                                Color(0xFFDCE9F8)
                            } else {
                                Color(0xFFEAF2FC)
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun BellBadge(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFEAF3FF)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = CleanXBlue,
            modifier = Modifier.size(size * 0.48f),
        )
    }
}

@Composable
private fun AppInitialBadge(text: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFEAF3FF)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = CleanXBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SelectionBubble(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (selected) CleanXBlue else Color.White),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun PhoneOutline(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 4.dp.toPx()
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(size.width * 0.08f, size.height * 0.02f),
            size = Size(size.width * 0.84f, size.height * 0.96f),
            cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
            style = Stroke(stroke),
        )
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(size.width * 0.31f, size.height * 0.02f),
            size = Size(size.width * 0.38f, size.height * 0.10f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
        )
    }
}

@Composable
private fun Sparkle(
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    Canvas(modifier = modifier.size(if (small) 28.dp else 42.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawPath(
            path = Path().apply {
                moveTo(center.x, 0f)
                lineTo(center.x + size.width * 0.12f, center.y - size.height * 0.12f)
                lineTo(size.width, center.y)
                lineTo(center.x + size.width * 0.12f, center.y + size.height * 0.12f)
                lineTo(center.x, size.height)
                lineTo(center.x - size.width * 0.12f, center.y + size.height * 0.12f)
                lineTo(0f, center.y)
                lineTo(center.x - size.width * 0.12f, center.y - size.height * 0.12f)
                close()
            },
            color = Color(0xFFF4B400),
        )
    }
}
