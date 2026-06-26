package com.quickcleanpro.phonecleaner.presentation.screen.tools.notificationbar.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.PackageAppIcon
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.NotificationBlockingTurnedOffDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerNavy
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationBarPage
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationBarUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification.NotificationBarViewModel
import kotlinx.coroutines.delay

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp
private const val NotificationGuideSlideDelayMillis = 2_000L

private data class NotificationGuideSlide(
    val imageRes: Int,
)

private val NotificationGuideSlides = listOf(
    NotificationGuideSlide(R.drawable.notification_bar_guide_1),
    NotificationGuideSlide(R.drawable.notification_bar_guide_2),
    NotificationGuideSlide(R.drawable.notification_bar_guide_3),
    NotificationGuideSlide(R.drawable.notification_bar_guide_4),
)

@Composable
internal fun NotificationBarScreenState(viewModel: NotificationBarViewModel) {
    val router = LocalRouter.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    var showBlockingTurnedOffDialog by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }

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

    fun handleBack() {
        when (uiState.page) {
            NotificationBarPage.Settings -> viewModel.leaveSettings()
            NotificationBarPage.Scanning -> {
                viewModel.cancelScanning()
                showStopDialog = true
            }
            else -> router.goBack()
        }
    }

    BackHandler(
        enabled = uiState.page == NotificationBarPage.Settings ||
            uiState.page == NotificationBarPage.Scanning,
        onBack = ::handleBack,
    )

    CleanXScaffoldPage(
        title = stringResource(R.string.notification_bar),
        onBack = ::handleBack,
        actions = {
            if (uiState.page == NotificationBarPage.Status) {
                IconButton(
                    onClick = { viewModel.showSettings() },
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
            !uiState.isInitialized -> Spacer(modifier = Modifier.height(220.dp))
            uiState.errorMessage != null -> ErrorCard(
                message = uiState.errorMessage.orEmpty(),
                onRetry = viewModel::refreshState,
            )
            else -> {
                when (uiState.page) {
                    NotificationBarPage.Onboarding -> OnboardingContent(
                        onEnableClick = {
                            permissionCoordinator.guard(CleanXProtectedAction.NotificationBarEnable) {
                                viewModel.setBlockingEnabled(true)
                            }
                        },
                    )
                    NotificationBarPage.Scanning -> ScanningContent(
                        blockedCount = uiState.blockedCount,
                        onFinished = viewModel::finishScanning,
                    )
                    NotificationBarPage.Status -> StatusContent(uiState = uiState)
                    NotificationBarPage.Settings -> SettingsContent(
                        uiState = uiState,
                        onEnabledChange = { checked ->
                            if (checked) {
                                permissionCoordinator.guard(CleanXProtectedAction.NotificationBarEnable) {
                                    viewModel.setBlockingEnabled(true)
                                }
                            } else {
                                showBlockingTurnedOffDialog = true
                            }
                        },
                        onTogglePackage = viewModel::togglePackage,
                        onDone = viewModel::leaveSettings,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showBlockingTurnedOffDialog) {
        NotificationBlockingTurnedOffDialog(
            onCancel = { showBlockingTurnedOffDialog = false },
            onConfirm = {
                showBlockingTurnedOffDialog = false
                viewModel.disableBlockingAndClearSelections()
            },
        )
    }

    if (showStopDialog) {
        StopScanDialog(
            onQuit = {
                showStopDialog = false
                router.goBack()
            },
            onResume = {
                showStopDialog = false
                viewModel.restartScanning()
            },
        )
    }
}

@Composable
private fun OnboardingContent(onEnableClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NotificationBarGuideCarousel(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(360.dp),
        )
        Spacer(modifier = Modifier.height(150.dp))
        Text(
            text = stringResource(R.string.notification_tidy_one_tap),
            color = Navy,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        CleanXPrimaryButton(
            text = stringResource(R.string.notification_one_tap_tidy_up),
            onClick = onEnableClick,
        )
    }
}

@Composable
private fun NotificationBarGuideCarousel(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { NotificationGuideSlides.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(NotificationGuideSlideDelayMillis)
            val nextPage = (pagerState.currentPage + 1) % NotificationGuideSlides.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.clipToBounds(),
        verticalAlignment = Alignment.CenterVertically,
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(NotificationGuideSlides[page].imageRes),
                contentDescription = null,
                modifier = Modifier
                    .heightIn(max = 348.dp)
                    .widthIn(max = 340.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun ScanningContent(
    blockedCount: Int,
    onFinished: () -> Unit,
) {
    LaunchedEffect(Unit) {
        delay(1800L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 67.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CleanSpiralAnimation {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = blockedCount.toString(),
                        color = CleanXBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.scan_loading_fallback),
                        color = NavyMuted,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusContent(uiState: NotificationBarUiState) {
    val blockedRows =
        remember(uiState.apps, uiState.blockedCountsByPackage) {
            val appsByPackage = uiState.apps.associateBy { it.packageName }
            uiState.blockedCountsByPackage.entries
                .filter { it.value > 0 }
                .sortedByDescending { it.value }
                .map { (packageName, count) ->
                    (appsByPackage[packageName] ?: BlockableNotificationApp(packageName, packageName)) to count
                }
        }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BellBadge()
            Spacer(modifier = Modifier.height(18.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text =
                    if (uiState.blockedCount == 0) {
                        stringResource(R.string.notification_intercepted_appear_here)
                    } else {
                        pluralStringResource(R.plurals.notifications_blocked_count, uiState.blockedCount, uiState.blockedCount)
                    },
                color = Navy,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.notification_showing_limit),
                color = NavyMuted,
                fontSize = 14.sp,
            )
        }
    }

    if (blockedRows.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        blockedRows.forEach { (app, count) ->
            BlockedAppRow(app = app, count = count)
            Spacer(modifier = Modifier.height(12.dp))
        }
    } else if (uiState.blockedCount == 0) {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(R.drawable.notification_cleaner_blank),
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 374.dp)
                    .aspectRatio(1f),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun SettingsContent(
    uiState: NotificationBarUiState,
    onEnabledChange: (Boolean) -> Unit,
    onTogglePackage: (String) -> Unit,
    onDone: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
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
                colors =
                    SwitchDefaults.colors(
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
            enabled = uiState.enabled,
            selected = app.packageName in uiState.selectedPackages,
            onToggleSelection = { onTogglePackage(app.packageName) },
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    CleanXPrimaryButton(
        text = stringResource(R.string.ok),
        onClick = onDone,
    )
}

@Composable
private fun NotificationAppRow(
    app: BlockableNotificationApp,
    enabled: Boolean,
    selected: Boolean,
    onToggleSelection: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onToggleSelection),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PackageAppIcon(
                packageName = app.packageName,
                fallbackText = app.appName.take(1).ifBlank { "A" },
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = app.appName,
                color = if (enabled) Navy else NavyMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            SelectionBubble(selected = selected, enabled = enabled)
        }
    }
}

@Composable
private fun BlockedAppRow(
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
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = app.appName,
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = pluralStringResource(R.plurals.notifications_blocked_count, count, count),
                color = NavyMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun BellBadge() {
    Box(
        modifier =
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF3FF)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = CleanXBlue,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun AppInitialBadge(text: String) {
    Box(
        modifier =
            Modifier
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
private fun SelectionBubble(
    selected: Boolean,
    enabled: Boolean = true,
) {
    Box(
        modifier =
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        selected && enabled -> CleanXBlue
                        selected -> CleanXBlue.copy(alpha = 0.35f)
                        else -> Color.White
                    },
                ),
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
private fun ErrorCard(
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
            )
            CleanXPrimaryButton(
                text = stringResource(R.string.retry),
                onClick = onRetry,
            )
        }
    }
}
