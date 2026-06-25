package com.quickcleanpro.phonecleaner.presentation.app

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme
import java.util.Calendar
import androidx.compose.material3.Icon
import kotlinx.coroutines.delay

internal enum class NotificationPermissionRequestSource {
    Splash,
    HomeSystem,
    HomeCustom,
}

internal enum class NotificationPermissionHomePromptAction {
    None,
    RequestSystemPermission,
    ShowCustomDialog,
}

internal data class NotificationPermissionPromptState(
    val isSplashVisible: Boolean,
    val isHomeVisible: Boolean,
    val hasNotificationPermission: Boolean,
    val hasRequestedBefore: Boolean,
    val shouldShowRationale: Boolean,
    val lastCustomPromptAt: Long,
)

internal data class NotificationPermissionRefreshAction(
    val homePromptAction: NotificationPermissionHomePromptAction,
    val notifyPermissionGranted: Boolean,
)

private const val HOME_NOTIFICATION_PERMISSION_PROMPT_DELAY_MILLIS = 350L

internal fun shouldRequestSplashNotificationPermission(
    state: NotificationPermissionPromptState,
): Boolean = state.isSplashVisible && !state.hasNotificationPermission && !state.hasRequestedBefore

internal fun notificationPermissionRefreshAction(
    state: NotificationPermissionPromptState,
    returningFromSettings: Boolean,
    suppressHomePrompt: Boolean,
    nowMillis: Long,
    allowCustomPromptInCurrentSession: Boolean,
): NotificationPermissionRefreshAction =
    NotificationPermissionRefreshAction(
        homePromptAction =
            when {
                state.hasNotificationPermission ||
                    !state.isHomeVisible ||
                    returningFromSettings ||
                    suppressHomePrompt -> NotificationPermissionHomePromptAction.None
                !state.hasRequestedBefore -> NotificationPermissionHomePromptAction.None
                state.shouldShowRationale -> NotificationPermissionHomePromptAction.RequestSystemPermission
                !allowCustomPromptInCurrentSession -> NotificationPermissionHomePromptAction.None
                canShowNotificationPermissionCustomPrompt(state.lastCustomPromptAt, nowMillis) ->
                    NotificationPermissionHomePromptAction.ShowCustomDialog
                else -> NotificationPermissionHomePromptAction.None
            },
        notifyPermissionGranted = state.hasNotificationPermission,
    )

internal fun canShowNotificationPermissionCustomPrompt(
    lastPromptAt: Long,
    nowMillis: Long,
): Boolean {
    if (lastPromptAt <= 0L) return true
    if (nowMillis <= 0L) return true
    return !isSameLocalDay(lastPromptAt, nowMillis)
}

private fun isSameLocalDay(
    firstMillis: Long,
    secondMillis: Long,
): Boolean {
    val first = Calendar.getInstance().apply { timeInMillis = firstMillis }
    val second = Calendar.getInstance().apply { timeInMillis = secondMillis }
    return first.get(Calendar.ERA) == second.get(Calendar.ERA) &&
        first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

@Composable
internal fun NotificationPermissionPrompt(
    isSplashVisible: Boolean,
    isHomeVisible: Boolean,
    hasNotificationPermission: () -> Boolean,
    hasRequestedNotificationPermissionBefore: () -> Boolean,
    saveNotificationPermissionRequestedBefore: () -> Unit,
    shouldShowNotificationPermissionRationale: () -> Boolean,
    readLastCustomPromptAt: () -> Long,
    saveLastCustomPromptAt: (Long) -> Unit,
    openAppSettings: () -> Boolean,
    allowCustomPromptInCurrentSession: Boolean = true,
    onHomeSystemPermissionRejectedThisSession: () -> Unit = {},
    onPermissionGranted: () -> Unit,
    onSplashPermissionActiveChange: (Boolean) -> Unit,
    onPermissionUiActiveChange: (Boolean) -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val lifecycleOwner = LocalLifecycleOwner.current
    var promptState by remember {
        mutableStateOf(
            NotificationPermissionPromptState(
                isSplashVisible = isSplashVisible,
                isHomeVisible = isHomeVisible,
                hasNotificationPermission = hasNotificationPermission(),
                hasRequestedBefore = hasRequestedNotificationPermissionBefore(),
                shouldShowRationale = shouldShowNotificationPermissionRationale(),
                lastCustomPromptAt = readLastCustomPromptAt(),
            ),
        )
    }
    var showCustomDialog by remember { mutableStateOf(false) }
    var requestSource by remember { mutableStateOf<NotificationPermissionRequestSource?>(null) }
    var notificationSettingsLaunchPending by rememberSaveable { mutableStateOf(false) }
    var suppressHomePromptUntilMillis by remember { mutableLongStateOf(0L) }

    fun setPermissionUiActive(active: Boolean) {
        onPermissionUiActiveChange(active)
    }

    fun updatePromptState(
        returningFromSettings: Boolean = false,
        keepHomeVisibility: Boolean = isHomeVisible,
        keepSplashVisibility: Boolean = isSplashVisible,
    ): NotificationPermissionPromptState {
        val updated =
            NotificationPermissionPromptState(
                isSplashVisible = keepSplashVisibility,
                isHomeVisible = keepHomeVisibility,
                hasNotificationPermission = hasNotificationPermission(),
                hasRequestedBefore = hasRequestedNotificationPermissionBefore(),
                shouldShowRationale = shouldShowNotificationPermissionRationale(),
                lastCustomPromptAt = readLastCustomPromptAt(),
            )
        promptState = updated
        if (updated.hasNotificationPermission) {
            onPermissionGranted()
            showCustomDialog = false
            onSplashPermissionActiveChange(false)
            setPermissionUiActive(false)
        } else if (returningFromSettings) {
            setPermissionUiActive(false)
        }
        return updated
    }

    fun openNotificationSettings() {
        showCustomDialog = false
        requestSource = NotificationPermissionRequestSource.HomeCustom
        setPermissionUiActive(true)
        val launched = openAppSettings()
        notificationSettingsLaunchPending = launched
        if (!launched) {
            requestSource = null
            setPermissionUiActive(false)
        }
    }

    fun clearPermissionUi(clearSplash: Boolean = false) {
        if (clearSplash) {
            onSplashPermissionActiveChange(false)
        }
        setPermissionUiActive(false)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            val source = requestSource
            requestSource = null
            showCustomDialog = false
            if (isGranted) {
                updatePromptState()
                clearPermissionUi(clearSplash = source == NotificationPermissionRequestSource.Splash)
                return@rememberLauncherForActivityResult
            }
            if (source == NotificationPermissionRequestSource.HomeSystem) {
                onHomeSystemPermissionRejectedThisSession()
            }
            promptState =
                promptState.copy(
                    hasNotificationPermission = false,
                    hasRequestedBefore = true,
                    shouldShowRationale = shouldShowNotificationPermissionRationale(),
                )
            suppressHomePromptUntilMillis = 0L
            clearPermissionUi(clearSplash = source == NotificationPermissionRequestSource.Splash)
        }

    fun launchPermissionRequest(source: NotificationPermissionRequestSource) {
        saveNotificationPermissionRequestedBefore()
        promptState =
            promptState.copy(
                hasRequestedBefore = true,
                shouldShowRationale = shouldShowNotificationPermissionRationale(),
            )
        requestSource = source
        if (source == NotificationPermissionRequestSource.Splash) {
            onSplashPermissionActiveChange(true)
        }
        setPermissionUiActive(true)
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun showDailyCustomPrompt() {
        val now = System.currentTimeMillis()
        saveLastCustomPromptAt(now)
        promptState = promptState.copy(lastCustomPromptAt = now)
        showCustomDialog = true
        setPermissionUiActive(true)
    }

    fun dismissCustomPrompt() {
        showCustomDialog = false
        setPermissionUiActive(false)
    }

    fun refreshPermission(returningFromSettings: Boolean) {
        if (returningFromSettings) {
            requestSource = null
        }
        val now = System.currentTimeMillis()
        val updated = updatePromptState(returningFromSettings = returningFromSettings)
        val action =
            notificationPermissionRefreshAction(
                state = updated,
                returningFromSettings = returningFromSettings,
                suppressHomePrompt = now < suppressHomePromptUntilMillis,
                nowMillis = now,
                allowCustomPromptInCurrentSession = allowCustomPromptInCurrentSession,
            )
        if (action.notifyPermissionGranted) {
            return
        }
        when (action.homePromptAction) {
            NotificationPermissionHomePromptAction.None -> {
                showCustomDialog = false
                setPermissionUiActive(false)
            }
            NotificationPermissionHomePromptAction.RequestSystemPermission -> {
                showCustomDialog = false
                suppressHomePromptUntilMillis = System.currentTimeMillis() + 5_000L
                launchPermissionRequest(NotificationPermissionRequestSource.HomeSystem)
            }
            NotificationPermissionHomePromptAction.ShowCustomDialog -> showDailyCustomPrompt()
        }
    }

    LaunchedEffect(isSplashVisible, isHomeVisible) {
        promptState =
            promptState.copy(
                isSplashVisible = isSplashVisible,
                isHomeVisible = isHomeVisible,
            )
    }

    LaunchedEffect(isSplashVisible, promptState.hasNotificationPermission, promptState.hasRequestedBefore) {
        if (
            shouldRequestSplashNotificationPermission(
                promptState.copy(
                    isSplashVisible = isSplashVisible,
                    isHomeVisible = isHomeVisible,
                ),
            )
        ) {
            launchPermissionRequest(NotificationPermissionRequestSource.Splash)
        }
    }

    LaunchedEffect(isSplashVisible, promptState.hasNotificationPermission) {
        if (!isSplashVisible || promptState.hasNotificationPermission) {
            onSplashPermissionActiveChange(false)
        }
    }

    LaunchedEffect(isHomeVisible) {
        if (isHomeVisible) {
            delay(HOME_NOTIFICATION_PERMISSION_PROMPT_DELAY_MILLIS)
            refreshPermission(returningFromSettings = false)
        } else {
            showCustomDialog = false
            suppressHomePromptUntilMillis = 0L
            setPermissionUiActive(false)
        }
    }

    LaunchedEffect(isHomeVisible, promptState.hasNotificationPermission, requestSource, suppressHomePromptUntilMillis) {
        val delayMillis = suppressHomePromptUntilMillis - System.currentTimeMillis()
        if (isHomeVisible && !promptState.hasNotificationPermission && requestSource == null && delayMillis > 0L) {
            delay(delayMillis)
            refreshPermission(returningFromSettings = false)
        }
    }

    DisposableEffect(lifecycleOwner, isHomeVisible) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME && isHomeVisible) {
                    val returningFromSettings = notificationSettingsLaunchPending
                    notificationSettingsLaunchPending = false
                    refreshPermission(returningFromSettings = returningFromSettings)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showCustomDialog && !promptState.hasNotificationPermission && isHomeVisible) {
        Dialog(
            onDismissRequest = ::dismissCustomPrompt,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            NotificationPermissionCustomDialog(
                onSubmit = ::openNotificationSettings,
                onCancel = ::dismissCustomPrompt,
            )
        }
    }
}

@Composable
private fun NotificationPermissionCustomDialog(
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onCancel,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 343.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {},
                    )
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    LocalVariantTheme.current.colors.surfaceBackground,
                                    Color.White,
                                    Color.White,
                                ),
                        ),
                    )
                    .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 24.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NotificationPermissionHero()
                Text(
                    text = stringResource(R.string.permission_post_notifications_desc),
                    color = LocalVariantTheme.current.colors.notificationTitleText,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = onSubmit,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = LocalVariantTheme.current.colors.notificationBlueIcon,
                            contentColor = Color.White,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.enable_now),
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionHero() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(137.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LocalVariantTheme.current.colors.notificationBlueBackground),
    ) {
        NotificationPermissionMiniCard(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 35.dp, y = 24.dp),
            withBlock = true,
        )
        NotificationPermissionMiniCard(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 181.dp, y = 46.dp),
            withToggle = true,
        )
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 24.dp)
                    .size(82.dp),
            tint = LocalVariantTheme.current.colors.notificationBlueIcon,
        )
    }
}

@Composable
private fun NotificationPermissionMiniCard(
    modifier: Modifier = Modifier,
    withBlock: Boolean = false,
    withToggle: Boolean = false,
) {
    Box(
        modifier =
            modifier
                .size(width = 113.dp, height = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.92f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LocalVariantTheme.current.colors.notificationPromptBackground),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.ic_notification_bar),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            if (withBlock) {
                Box(
                    modifier =
                        Modifier
                            .size(width = 34.dp, height = 16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(LocalVariantTheme.current.colors.notificationBadgeRed),
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .size(width = 40.dp, height = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(LocalVariantTheme.current.colors.notificationMutedRow),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (withToggle) {
                Box(
                    modifier =
                        Modifier
                            .size(width = 28.dp, height = 16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(LocalVariantTheme.current.colors.notificationBadgeGreen),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(start = 14.dp, top = 2.dp)
                                .size(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White),
                    )
                }
            }
        }
    }
}
