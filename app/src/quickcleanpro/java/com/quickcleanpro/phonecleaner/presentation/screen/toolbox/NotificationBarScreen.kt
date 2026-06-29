package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

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
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import com.quickcleanpro.phonecleaner.presentation.common.CleanXActionIconSize
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCompactPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXItemSpacing
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLargePadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineBody
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineBodySmall
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineCaption
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineSubtitle
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMediumPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPagePadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScanRingAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSmallShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextBody
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextBodySmall
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextCaption
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextSubtitle
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.screen.notification.NotificationEnableGuideDialog
import com.quickcleanpro.phonecleaner.presentation.screen.notification.NotificationBlockerViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.notification.NotificationPage
import kotlinx.coroutines.delay

private val NotificationOnboardingTitleSize = 26.sp
private val NotificationOnboardingTitleLineHeight = 34.sp
private val NotificationOnboardingButtonGap = 28.dp
private val NotificationScanRingSize = 260.dp
private val NotificationScanRingWidth = 20.dp
private val NotificationStatusIconSize = 256.dp
private val NotificationRowHeight = 78.dp
private val NotificationBellBadgeSize = 68.dp
private val NotificationBellIconSize = 34.dp
private val NotificationSelectionSize = 21.dp
private val NotificationSelectionCheckSize = 15.dp
private val NotificationSelectionStrokeWidth = 2.dp
private val NotificationCountLineHeight = 17.sp
private const val NotificationScanMinMillis = 1800L

@Preview
@Composable
fun NotificationBarScreen(
    onBack: () -> Unit = {}
) {
    NotificationBarScreenState(
        onBack = onBack,
        viewModel = viewModel()
    )
}

@Composable
internal fun NotificationBarRoute(
    onBack: () -> Unit = {}
) {
    NotificationBarScreenState(
        onBack = onBack,
        viewModel = koinViewModel()
    )
}

@Composable
private fun NotificationBarScreenState(
    onBack: () -> Unit,
    viewModel: NotificationBlockerViewModel
) {
    val context = LocalContext.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val page = uiState.page
    var showPermissionGuide by remember { mutableStateOf(false) }
    var showBlockingTurnedOffDialog by remember { mutableStateOf(false) }

    fun openPermissionGuide() {
        showPermissionGuide = true
    }

    fun finishPermissionGuide() {
        showPermissionGuide = false
        viewModel.refreshState()
    }

    fun openNotificationListenerSettings() {
        showPermissionGuide = false
        permissionCoordinator.guard(
            action = CleanXProtectedAction.NotificationBarEnable,
            onGranted = {
                viewModel.setBlockingEnabled(true)
                viewModel.refreshState()
            },
        )
    }

    fun openAppNotificationSettings(packageName: String) {
        runCatching {
            context.startActivity(viewModel.appNotificationSettingsIntent(packageName))
        }.onFailure {
            runCatching { context.startActivity(viewModel.appDetailsSettingsIntent(packageName)) }
        }
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
            if (page == NotificationPage.Settings) {
                viewModel.leaveSettings()
            } else {
                onBack()
            }
        },
        actions = {
            if (page == NotificationPage.Complete || page == NotificationPage.Status) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = CleanXText,
                    modifier = Modifier
                        .size(CleanXActionIconSize)
                        .clickable { viewModel.showSettings() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = CleanXPagePadding)
        ) {
            if (!uiState.isInitialized) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else when (page) {
                NotificationPage.Onboarding -> NotificationBarOnboardingContent(
                    modifier = Modifier.weight(1f),
                    onTidyUp = { openPermissionGuide() }
                )
                NotificationPage.Scanning -> NotificationBarScanningContent(
                    modifier = Modifier.weight(1f),
                    blockedCount = uiState.blockedCount,
                    onFinished = viewModel::finishScanning
                )
                NotificationPage.Complete,
                NotificationPage.Status -> NotificationBarStatusContent(
                    enabled = uiState.enabled,
                    blockedCount = uiState.blockedCount,
                    apps = uiState.apps,
                    blockedCountsByPackage = uiState.blockedCountsByPackage
                )
                NotificationPage.Settings -> NotificationBarSettingsContent(
                    enabled = uiState.enabled,
                    apps = uiState.apps,
                    selectedPackages = uiState.selectedPackages,
                    onEnabledChange = { checked ->
                        if (checked) {
                            openNotificationListenerSettings()
                        } else {
                            viewModel.setBlockingEnabled(false)
                            showBlockingTurnedOffDialog = true
                        }
                    },
                    onTogglePackage = viewModel::togglePackage,
                )
            }
        }
    }

    if (showPermissionGuide) {
        NotificationEnableGuideDialog(
            onDismiss = { finishPermissionGuide() },
            onOpenSettings = { openNotificationListenerSettings() }
        )
    }

    if (showBlockingTurnedOffDialog) {
        NotificationBlockingTurnedOffDialog(
            onDismiss = { showBlockingTurnedOffDialog = false }
        )
    }
}

@Composable
private fun NotificationBarScanningContent(
    modifier: Modifier = Modifier,
    blockedCount: Int,
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(NotificationScanMinMillis)
        onFinished()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            CleanXScanRingAnimation(
                modifier = Modifier.size(NotificationScanRingSize),
                ringWidth = NotificationScanRingWidth,
                ringColor = CleanXBlue,
                backgroundColor = CleanXBlue.copy(alpha = 0.12f),
                animationDurationMillis = 900
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = blockedCount.toString(),
                        color = CleanXBlue,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.scan_loading_fallback),
                        color = CleanXMutedText,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationBarOnboardingContent(
    modifier: Modifier = Modifier,
    onTidyUp: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = CleanXPagePadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieNotificationGuide(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(modifier = Modifier.height(CleanXLargePadding + CleanXCompactPadding / 2))
        Text(
            text = stringResource(R.string.notification_tidy_one_tap),
            color = CleanXText,
            fontSize = NotificationOnboardingTitleSize,
            lineHeight = NotificationOnboardingTitleLineHeight,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(NotificationOnboardingButtonGap))
        CleanXPrimaryButton(
            text = stringResource(R.string.notification_one_tap_tidy_up),
            onClick = onTidyUp
        )
    }
}

@Composable
fun LottieNotificationGuide(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("notification_animation/notification.json"),
        imageAssetsFolder = "notification_animation/images/"
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true,
        speed = 1f
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (composition == null) {
            Text(
                stringResource(R.string.notification_loading_animation),
                color = CleanXMutedText,
                fontSize = CleanXTextBody
            )
        } else {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun NotificationBarStatusContent(
    enabled: Boolean,
    blockedCount: Int,
    apps: List<BlockableNotificationApp>,
    blockedCountsByPackage: Map<String, Int>
) {
    val blockedRows = remember(apps, blockedCountsByPackage) {
        val appsByPackage = apps.associateBy { it.packageName }
        blockedCountsByPackage.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
            .map { (packageName, count) ->
                (appsByPackage[packageName] ?: BlockableNotificationApp(packageName, packageName)) to count
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = CleanXLargePadding + CleanXCompactPadding / 2),
        verticalArrangement = Arrangement.spacedBy(CleanXItemSpacing)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = CleanXSmallShape
        ) {
            Column(
                modifier = Modifier.padding(vertical = CleanXLargePadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BellBadge()
                Spacer(modifier = Modifier.height(CleanXLargePadding - 2.dp))
                Text(
                    text = stringResource(
                        if (enabled) {
                            R.string.notification_status_enabled
                        } else {
                            R.string.notification_status_disabled
                        }
                    ),
                    color = CleanXText,
                    fontSize = CleanXTextSubtitle,
                    lineHeight = CleanXLineSubtitle,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(CleanXMediumPadding - 2.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.notifications_blocked_count,
                        blockedCount,
                        blockedCount
                    ),
                    color = CleanXText,
                    fontSize = CleanXTextSubtitle
                )
                Spacer(modifier = Modifier.height(CleanXPagePadding))
                Text(
                    stringResource(R.string.notification_showing_limit),
                    color = CleanXMutedText,
                    fontSize = CleanXTextBody
                )
                if (blockedCount == 0) {
                    Spacer(modifier = Modifier.height(CleanXPagePadding))
                    Image(
                        modifier = Modifier.size(NotificationStatusIconSize),
                        painter = painterResource(R.drawable.files_blank),
                        contentDescription = null
                    )
                }
            }
        }

        if (blockedCount > 0) {
            blockedRows.forEach { (app, count) ->
                BlockedNotificationAppRow(app = app, count = count)
            }
        }
    }
}

@Composable
private fun NotificationBarSettingsContent(
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
            .padding(bottom = CleanXLargePadding + CleanXCompactPadding / 2),
        verticalArrangement = Arrangement.spacedBy(CleanXLargePadding)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = CleanXSmallShape
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = CleanXPagePadding,
                    vertical = CleanXLargePadding - 2.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.notification_blocked_notifications),
                        color = CleanXText,
                        fontSize = CleanXTextSubtitle,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(CleanXCompactPadding / 2))
                    Text(
                        stringResource(R.string.notification_block_settings_desc),
                        color = CleanXMutedText,
                        fontSize = CleanXTextBodySmall,
                        lineHeight = CleanXLineCaption
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

        apps.forEach { app ->
            NotificationBarAppRow(
                app = app,
                selected = app.packageName in selectedPackages,
                onToggleSelection = { onTogglePackage(app.packageName) },
            )
        }
    }
}

@Composable
private fun NotificationBarAppRow(
    app: BlockableNotificationApp,
    selected: Boolean,
    onToggleSelection: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(NotificationRowHeight)
            .clickable { onToggleSelection() },
        color = Color.White,
        shape = CleanXSmallShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CleanXPagePadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PackageAppIcon(
                packageName = app.packageName,
                fallbackText = app.appName.take(1).ifBlank { "A" },
                color = CleanXBlue
            )
            Spacer(modifier = Modifier.width(CleanXItemSpacing))
            Text(
                text = app.appName,
                color = CleanXText,
                fontSize = CleanXTextSubtitle,
                lineHeight = CleanXLineSubtitle,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            SelectionBubble(
                selected = selected,
                modifier = Modifier.clickable { onToggleSelection() }
            )
        }
    }
}

@Composable
private fun BlockedNotificationAppRow(
    app: BlockableNotificationApp,
    count: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(NotificationRowHeight),
        color = Color.White,
        shape = CleanXSmallShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = CleanXPagePadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PackageAppIcon(
                packageName = app.packageName,
                fallbackText = app.appName.take(1).ifBlank { "A" },
                color = CleanXBlue
            )
            Spacer(modifier = Modifier.width(CleanXItemSpacing))
            Text(
                text = app.appName,
                color = CleanXText,
                fontSize = CleanXTextSubtitle,
                lineHeight = CleanXLineSubtitle,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = pluralStringResource(
                    R.plurals.notifications_blocked_count,
                    count,
                    count
                ),
                color = CleanXMutedText,
                fontSize = CleanXTextCaption,
                lineHeight = NotificationCountLineHeight,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun NotificationBlockingTurnedOffDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = CleanXSmallShape,
        title = {
            Text(
                text = stringResource(R.string.notification_blocking_off_message),
                color = CleanXText,
                fontSize = CleanXTextSubtitle,
                lineHeight = CleanXLineSubtitle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.notification_blocking_off_detail),
                color = CleanXMutedText,
                fontSize = CleanXTextBody,
                lineHeight = CleanXLineBody,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.ok),
                    color = CleanXBlue,
                    fontSize = CleanXTextBody,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun BellBadge() {
    Box(
        modifier = Modifier
            .size(NotificationBellBadgeSize)
            .clip(CircleShape)
            .background(Color(0xFFEAF3FF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = Color(0xFF1877FF),
            modifier = Modifier.size(NotificationBellIconSize)
        )
    }
}

@Composable
private fun SelectionBubble(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(NotificationSelectionSize)
            .clip(CircleShape)
            .background(if (selected) CleanXBlue else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(NotificationSelectionCheckSize)
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFFD7DEE8),
                    radius = size.minDimension * 0.42f,
                    style = Stroke(width = NotificationSelectionStrokeWidth.toPx())
                )
            }
        }
    }
}

