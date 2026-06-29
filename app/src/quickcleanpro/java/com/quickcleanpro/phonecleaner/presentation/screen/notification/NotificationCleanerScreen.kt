package com.quickcleanpro.phonecleaner.presentation.screen.notification

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp

private data class NotificationQuickActionSpec(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit
)

@Composable
fun NotificationCleanerScreen(
    onBack: () -> Unit,
    onDeviceInfo: () -> Unit,
    onJunkRemoval: () -> Unit,
    onBatteryInfo: () -> Unit,
    onNetworkScan: () -> Unit,
    onNetworkUsage: () -> Unit
) {
    NotificationCleanerScreenState(
        onBack = onBack,
        onDeviceInfo = onDeviceInfo,
        onJunkRemoval = onJunkRemoval,
        onBatteryInfo = onBatteryInfo,
        onNetworkScan = onNetworkScan,
        onNetworkUsage = onNetworkUsage,
        viewModel = viewModel()
    )
}

@Composable
internal fun NotificationCleanerRoute(
    onBack: () -> Unit,
    onDeviceInfo: () -> Unit,
    onJunkRemoval: () -> Unit,
    onBatteryInfo: () -> Unit,
    onNetworkScan: () -> Unit,
    onNetworkUsage: () -> Unit
) {
    NotificationCleanerScreenState(
        onBack = onBack,
        onDeviceInfo = onDeviceInfo,
        onJunkRemoval = onJunkRemoval,
        onBatteryInfo = onBatteryInfo,
        onNetworkScan = onNetworkScan,
        onNetworkUsage = onNetworkUsage,
        viewModel = koinViewModel()
    )
}

@Composable
private fun NotificationCleanerScreenState(
    onBack: () -> Unit,
    onDeviceInfo: () -> Unit,
    onJunkRemoval: () -> Unit,
    onBatteryInfo: () -> Unit,
    onNetworkScan: () -> Unit,
    onNetworkUsage: () -> Unit,
    viewModel: NotificationBlockerViewModel
) {
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val page = uiState.page
    var showPermissionGuide by remember { mutableStateOf(false) }

    fun enableBlockingOrRequestAccess() {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.NotificationCleanerEnable,
            onGranted = {
                viewModel.setBlockingEnabled(true)
                showPermissionGuide = false
                viewModel.markComplete()
            },
            onRejected = { showPermissionGuide = false },
        )
    }

    fun openNotificationListenerSettings() {
        showPermissionGuide = false
        enableBlockingOrRequestAccess()
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CleanXScaffold(
        titleRes = R.string.notification_bar,
        onBack = {
            if (page == NotificationPage.Settings) viewModel.leaveSettings() else onBack()
        },
        actions = {
            if (page != NotificationPage.Settings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = CleanXText,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.showSettings() }
                )
            }
        },
        bottomBar = {
            if (page == NotificationPage.Onboarding || page == NotificationPage.Complete) {
                CleanXBottomActionBar(
                    text = stringResource(R.string.notification_one_tap_tidy_up),
                    onClick = { enableBlockingOrRequestAccess() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (page) {
                NotificationPage.Onboarding -> NotificationOnboardingContent()
                NotificationPage.Complete -> NotificationCompleteContent(blockedCount = uiState.blockedCount)
                NotificationPage.Scanning,
                NotificationPage.Status -> NotificationStatusContent(
                    enabled = uiState.enabled,
                    blockedCount = uiState.blockedCount,
                    onOpenSettings = viewModel::showSettings,
                    onDeviceInfo = onDeviceInfo,
                    onJunkRemoval = onJunkRemoval,
                    onBatteryInfo = onBatteryInfo,
                    onNetworkScan = onNetworkScan,
                    onNetworkUsage = onNetworkUsage
                )
                NotificationPage.Settings -> NotificationSettingsContent(
                    enabled = uiState.enabled,
                    apps = uiState.apps,
                    selectedPackages = uiState.selectedPackages,
                    onEnabledChange = { checked ->
                        if (checked) {
                            showPermissionGuide = true
                        } else {
                            viewModel.setBlockingEnabled(false)
                        }
                    },
                    onTogglePackage = viewModel::togglePackage
                )
            }
        }
    }

    if (showPermissionGuide) {
        NotificationEnableGuideDialog(
            onDismiss = { showPermissionGuide = false },
            onOpenSettings = ::openNotificationListenerSettings
        )
    }
}

@Composable
private fun NotificationOnboardingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.notification_overload_title),
            color = CleanXText,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Box(modifier = Modifier.size(width = 343.dp, height = 430.dp), contentAlignment = Alignment.Center) {
            AnimatedNotificationFrames(modifier = Modifier.size(300.dp))
            Sparkle(Modifier.align(Alignment.TopEnd).padding(top = 18.dp, end = 20.dp))
            Sparkle(Modifier.align(Alignment.TopStart).padding(top = 58.dp, start = 30.dp), small = true)
            Sparkle(Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 72.dp), small = true)
            Sparkle(Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 160.dp), small = true)
        }
        Spacer(modifier = Modifier.height(42.dp))
        Text(
            text = stringResource(R.string.notification_tidy_one_tap),
            color = CleanXText,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(92.dp))
    }
}

@Composable
private fun NotificationCompleteContent(blockedCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.notification_tidy_complete),
            color = CleanXText,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.size(width = 292.dp, height = 320.dp), contentAlignment = Alignment.TopCenter) {
            PhoneOutline(modifier = Modifier.size(width = 246.dp, height = 318.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 10.dp
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    BellBadge(size = 64)
                    Spacer(modifier = Modifier.width(18.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(blockedCount.toString(), color = Color(0xFFFF3039), fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                pluralStringResource(R.plurals.notifications_count, blockedCount),
                                color = CleanXText,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.notification_collected_suffix),
                            color = CleanXText,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(62.dp))
        Text(
            text = stringResource(R.string.notification_tidy_one_tap),
            color = CleanXText,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(92.dp))
    }
}

@Composable
private fun NotificationStatusContent(
    enabled: Boolean,
    blockedCount: Int,
    onOpenSettings: () -> Unit,
    onDeviceInfo: () -> Unit = {},
    onJunkRemoval: () -> Unit = {},
    onBatteryInfo: () -> Unit = {},
    onNetworkScan: () -> Unit = {},
    onNetworkUsage: () -> Unit = {}
) {
    val actions = remember(
        onDeviceInfo,
        onJunkRemoval,
        onBatteryInfo,
        onNetworkScan,
        onNetworkUsage,
        onOpenSettings
    ) {
        listOf(
            NotificationQuickActionSpec(R.drawable.ic_n_device_info, R.string.device_info, onDeviceInfo),
            NotificationQuickActionSpec(R.drawable.ic_n_junk_removal, R.string.junk_removal, onJunkRemoval),
            NotificationQuickActionSpec(R.drawable.ic_n_battery_info, R.string.battery_info, onBatteryInfo),
            NotificationQuickActionSpec(R.drawable.ic_n_network_scan, R.string.network_scan, onNetworkScan),
            NotificationQuickActionSpec(R.drawable.ic_n_network_usage, R.string.network_usage, onNetworkUsage),
            NotificationQuickActionSpec(R.drawable.ic_n_notification_cleaner, R.string.notification_cleaner, onOpenSettings)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSettings() },
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BellBadge(size = 68)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        if (enabled) {
                            R.string.notification_status_enabled
                        } else {
                            R.string.notification_status_disabled
                        }
                    ),
                    color = CleanXText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.notifications_blocked_count,
                        blockedCount,
                        blockedCount
                    ),
                    color = CleanXText,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.notification_showing_limit),
                    color = CleanXMutedText,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            actions.chunked(3).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowActions.forEach { action ->
                        NotificationQuickAction(
                            iconRes = action.iconRes,
                            label = stringResource(action.labelRes),
                            modifier = Modifier.weight(1f),
                            onClick = action.onClick
                        )
                    }
                    repeat(3 - rowActions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationQuickAction(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = CleanXText,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NotificationSettingsContent(
    enabled: Boolean,
    apps: List<BlockableNotificationApp>,
    selectedPackages: Set<String>,
    onEnabledChange: (Boolean) -> Unit,
    onTogglePackage: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.notification_blocked_notifications),
                        color = CleanXText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.notification_block_settings_desc),
                        color = CleanXMutedText,
                        fontSize = 15.sp,
                        lineHeight = 18.sp
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CleanXBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD8DDE6)
                    )
                )
            }
        }

        apps.forEachIndexed { index, app ->
            NotificationAppRow(
                app = app,
                selected = app.packageName in selectedPackages,
                color = appColor(index),
                onClick = { onTogglePackage(app.packageName) }
            )
        }
    }
}

@Composable
private fun NotificationAppRow(
    app: BlockableNotificationApp,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCircleIcon(app.appName.take(1).ifBlank { "A" }, color)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = app.appName,
                color = CleanXText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            SelectionCircle(selected = selected)
        }
    }
}

@Composable
private fun AnimatedNotificationFrames(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "notifAnim")
    val waveOffset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        PhoneOutline(modifier = Modifier.fillMaxSize())

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .padding(top = 26.dp),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(8) { index ->
                    val alpha = if (waveOffset > index * 60f) {
                        ((waveOffset - index * 60f) / 60f).coerceIn(0.15f, 0.9f)
                    } else 0.15f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFD0DCE8).copy(alpha = alpha.coerceIn(0.15f, 1f)))
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .padding(top = 140.dp),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(6) { index ->
                    val alpha = if (waveOffset > 120f + index * 70f) {
                        ((waveOffset - 120f - index * 70f) / 70f).coerceIn(0.15f, 0.9f)
                    } else 0.15f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFD0DCE8).copy(alpha = alpha.coerceIn(0.15f, 1f)))
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .padding(top = 240.dp),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(5) { index ->
                    val alpha = if (waveOffset > 240f + index * 80f) {
                        ((waveOffset - 240f - index * 80f) / 80f).coerceIn(0.15f, 0.9f)
                    } else 0.15f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFD0DCE8).copy(alpha = alpha.coerceIn(0.15f, 1f)))
                    )
                }
            }
        }
    }
}

@Composable
private fun BellBadge(size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEAF4FF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = Color(0xFF178BFF),
            modifier = Modifier.size((size * 0.48f).dp)
        )
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
            style = Stroke(stroke)
        )
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(size.width * 0.31f, size.height * 0.02f),
            size = Size(size.width * 0.38f, size.height * 0.10f),
            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
        )
    }
}

@Composable
private fun AppCircleIcon(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(50))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SelectionCircle(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(21.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) CleanXBlue else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(Color(0xFFD7DEE8), radius = size.minDimension * 0.42f, style = Stroke(2.dp.toPx()))
            }
        }
    }
}

@Composable
private fun Sparkle(modifier: Modifier = Modifier, small: Boolean = false) {
    Canvas(modifier = modifier.size(if (small) 28.dp else 42.dp)) {
        val c = Offset(size.width / 2f, size.height / 2f)
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(c.x, 0f)
                lineTo(c.x + size.width * 0.12f, c.y - size.height * 0.12f)
                lineTo(size.width, c.y)
                lineTo(c.x + size.width * 0.12f, c.y + size.height * 0.12f)
                lineTo(c.x, size.height)
                lineTo(c.x - size.width * 0.12f, c.y + size.height * 0.12f)
                lineTo(0f, c.y)
                lineTo(c.x - size.width * 0.12f, c.y - size.height * 0.12f)
                close()
            },
            color = Color(0xFFF4B400)
        )
    }
}

private fun appColor(index: Int): Color =
    listOf(
        Color(0xFF1877FF),
        Color(0xFFFF171E),
        Color(0xFF1ED760),
        Color(0xFFFF8F21),
        Color(0xFF9B3DF4),
        Color(0xFF23A6F0)
    )[index % 6]
