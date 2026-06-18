package com.quickcleanpro.phonecleaner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quickcleanpro.phonecleaner.data.source.notification.PersistentNotificationService
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationDataSource
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationSpecs
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionCopy
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionRequiredDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.InlinePermissionOverlay
import com.quickcleanpro.phonecleaner.presentation.common.route.AppNavGraph
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.CleanXTheme
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settingsRepository: SettingsRepository by inject()
    private val appLockRepository: AppLockRepository by inject()

    private var pendingNotificationRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        pendingNotificationRoute = intent.notificationTargetRoute()

        setContent {
            CleanXTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route
                val targetRoute = pendingNotificationRoute

                AppNavGraph(
                    navController = navController,
                    startDestination = if (targetRoute != null) Screen.Home.route else Screen.Splash.route,
                )

                LaunchedEffect(navController, targetRoute) {
                    if (targetRoute != null) {
                        pendingNotificationRoute = null
                        navController.navigate(targetRoute) {
                            val route = navController.currentDestination?.route
                            if (route == Screen.Splash.route || route == Screen.OnboardingScan.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                            launchSingleTop = true
                        }
                    }
                }

                NotificationPermissionPrompt(
                    isHomeVisible = currentRoute == Screen.Home.route,
                    hasNotificationPermission = ::hasNotificationPermission,
                    hasDeniedNotificationPermission = {
                        runCatching { settingsRepository.hasDeniedNotificationRuntimePermission() }.getOrDefault(false)
                    },
                    rememberNotificationDenied = {
                        runCatching { settingsRepository.saveNotificationRuntimePermissionDenied() }
                    },
                    openAppSettings = {
                        runCatching { startActivity(settingsRepository.appSettingsIntent()) }
                    },
                    onPermissionGranted = { runCatching { PersistentNotificationService.start(this@MainActivity) } },
                )
            }
        }

        runCatching { startNotificationServiceWhenAllowed() }
        runCatching { syncPersistentNotificationService() }
    }

    override fun onStart() {
        super.onStart()
        PersistentNotificationService.setAppInForeground(true)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNotificationRoute = intent.notificationTargetRoute()
    }

    override fun onStop() {
        PersistentNotificationService.notifyAppBackground(this)
        super.onStop()
    }

    private fun startNotificationServiceWhenAllowed() {
        if (hasNotificationPermission()) {
            PersistentNotificationService.start(this)
        }
    }

    private fun syncPersistentNotificationService() {
        val canMonitor =
            runCatching {
                appLockRepository.isPinSet() &&
                    appLockRepository.isMonitoringEnabled() &&
                    appLockRepository.lockedAppCount() > 0 &&
                    appLockRepository.hasOverlayPermission() &&
                    appLockRepository.hasUsageAccess()
            }.getOrDefault(false)
        if (canMonitor) {
            runCatching { PersistentNotificationService.enableMonitoring(this) }
        } else {
            runCatching { PersistentNotificationService.disableMonitoring(this) }
        }
    }

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            runCatching {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
}

private fun Intent.notificationTargetRoute(): String? =
    getStringExtra(ToolNotificationDataSource.EXTRA_TARGET_ROUTE)
        ?.takeIf(::isValidNotificationRoute)

private fun isValidNotificationRoute(route: String): Boolean =
    route in validNotificationRoutes

private val validNotificationRoutes: Set<String> =
    buildSet {
        addAll(ToolNotificationSpecs.map { it.route })
        add(Screen.NotificationCleaner.route)
        add(Screen.NotificationBar.route)
    }

@Composable
private fun NotificationPermissionPrompt(
    isHomeVisible: Boolean,
    hasNotificationPermission: () -> Boolean,
    hasDeniedNotificationPermission: () -> Boolean,
    rememberNotificationDenied: () -> Unit,
    openAppSettings: () -> Unit,
    onPermissionGranted: () -> Unit,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasNotificationPermission()) }
    var showDialog by remember { mutableStateOf(false) }
    var notificationSettingsLaunchPending by rememberSaveable { mutableStateOf(false) }
    var homePromptSuppressed by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            granted = isGranted
            showDialog = false
            if (isGranted) {
                onPermissionGranted()
            } else {
                rememberNotificationDenied()
                notificationSettingsLaunchPending = true
                homePromptSuppressed = true
                openAppSettings()
            }
        }

    fun refreshPermission(returningFromSettings: Boolean) {
        val nowGranted = hasNotificationPermission()
        granted = nowGranted
        if (nowGranted) {
            showDialog = false
            onPermissionGranted()
        } else {
            showDialog = isHomeVisible && !returningFromSettings && !homePromptSuppressed
        }
    }

    LaunchedEffect(isHomeVisible) {
        if (isHomeVisible) {
            refreshPermission(returningFromSettings = false)
        } else {
            showDialog = false
            homePromptSuppressed = false
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

    if (showDialog && !granted && isHomeVisible) {
        InlinePermissionOverlay(
            onDismiss = { showDialog = false },
        ) {
            CleanXPermissionRequiredDialog(
                copy = CleanXPermissionCopy(
                    titleRes = com.quickcleanpro.phonecleaner.R.string.permission_title_required,
                    descriptionRes = com.quickcleanpro.phonecleaner.R.string.permission_post_notifications_desc,
                    hint1Res = com.quickcleanpro.phonecleaner.R.string.permission_hint_app_notifications,
                    hint2Res = com.quickcleanpro.phonecleaner.R.string.permission_hint_no_personal,
                ),
                onSubmit = {
                    showDialog = false
                    if (hasDeniedNotificationPermission()) {
                        notificationSettingsLaunchPending = true
                        homePromptSuppressed = true
                        openAppSettings()
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onCancel = { showDialog = false },
            )
        }
    }
}
