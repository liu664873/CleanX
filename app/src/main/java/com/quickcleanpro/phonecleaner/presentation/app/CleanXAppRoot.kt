package com.quickcleanpro.phonecleaner.presentation.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.quickcleanpro.phonecleaner.core.permission.appSettingsIntent
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionCoordinatorProvider
import com.quickcleanpro.phonecleaner.presentation.common.route.AppNavGraph
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.splash.SplashScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun CleanXAppRoot(
    launchCoordinator: AppLaunchCoordinator,
    onNotificationPermissionGranted: () -> Unit = {},
) {
    val context = LocalContext.current
    val settingsRepository: SettingsRepository = koinInject()
    val notificationPermissionSessionViewModel: NotificationPermissionSessionViewModel = koinViewModel()
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val pendingRequest = launchCoordinator.pendingRequest
    var splashNotificationPermissionActive by remember { mutableStateOf(false) }
    var notificationPermissionUiActive by remember { mutableStateOf(false) }
    val externalActivityLaunchHandler =
        ExternalActivityLaunchHandler(
            markLaunch = launchCoordinator::markExternalActivityLaunch,
            cancelLaunch = launchCoordinator::cancelExternalActivityLaunch,
        )

    fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            runCatching {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)

    fun shouldShowNotificationPermissionRationale(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true

    fun launchAppSettings(): Boolean {
        externalActivityLaunchHandler.markLaunch()
        return runCatching {
            context.startActivity(appSettingsIntent(context))
        }.onFailure {
            externalActivityLaunchHandler.cancelLaunch()
        }.isSuccess
    }

    val shouldPauseSplashForInitialNotificationRequest =
        (currentRoute == null || currentRoute == Screen.Splash.route) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !hasNotificationPermission() &&
            !settingsRepository.hasRequestedNotificationRuntimePermissionBefore()

    LaunchedEffect(currentRoute) {
        launchCoordinator.onRouteChanged(currentRoute)
    }

    CompositionLocalProvider(
        LocalExternalActivityLaunchHandler provides externalActivityLaunchHandler,
    ) {
        CleanXPermissionCoordinatorProvider {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    navController = navController,
                    launchCoordinator = launchCoordinator,
                    startDestination = Screen.Splash.route,
                    splashPaused = splashNotificationPermissionActive || shouldPauseSplashForInitialNotificationRequest,
                    externalBlockingPromptActive = notificationPermissionUiActive,
                    splashNotificationPermissionPrompt = {
                        NotificationPermissionPrompt(
                            isSplashVisible = true,
                            isHomeVisible = false,
                            hasNotificationPermission = ::hasNotificationPermission,
                            hasRequestedNotificationPermissionBefore =
                                settingsRepository::hasRequestedNotificationRuntimePermissionBefore,
                            saveNotificationPermissionRequestedBefore =
                                settingsRepository::saveNotificationRuntimePermissionRequestedBefore,
                            shouldShowNotificationPermissionRationale =
                                ::shouldShowNotificationPermissionRationale,
                            readLastCustomPromptAt = settingsRepository::readLastNotificationPermissionCustomPromptAt,
                            saveLastCustomPromptAt = settingsRepository::saveLastNotificationPermissionCustomPromptAt,
                            openAppSettings = ::launchAppSettings,
                            allowCustomPromptInCurrentSession = true,
                            onHomeSystemPermissionRejectedThisSession =
                                notificationPermissionSessionViewModel::markHomeCustomPromptDeferredUntilNextLaunch,
                            onPermissionGranted = onNotificationPermissionGranted,
                            onSplashPermissionActiveChange = { active ->
                                splashNotificationPermissionActive = active
                            },
                            onPermissionUiActiveChange = { active ->
                                notificationPermissionUiActive = active
                            },
                        )
                    },
                    homeNotificationPermissionPrompt = {
                        NotificationPermissionPrompt(
                            isSplashVisible = false,
                            isHomeVisible = true,
                            hasNotificationPermission = ::hasNotificationPermission,
                            hasRequestedNotificationPermissionBefore =
                                settingsRepository::hasRequestedNotificationRuntimePermissionBefore,
                            saveNotificationPermissionRequestedBefore =
                                settingsRepository::saveNotificationRuntimePermissionRequestedBefore,
                            shouldShowNotificationPermissionRationale =
                                ::shouldShowNotificationPermissionRationale,
                            readLastCustomPromptAt = settingsRepository::readLastNotificationPermissionCustomPromptAt,
                            saveLastCustomPromptAt = settingsRepository::saveLastNotificationPermissionCustomPromptAt,
                            openAppSettings = ::launchAppSettings,
                            allowCustomPromptInCurrentSession =
                                !notificationPermissionSessionViewModel.isHomeCustomPromptDeferredUntilNextLaunch,
                            onHomeSystemPermissionRejectedThisSession =
                                notificationPermissionSessionViewModel::markHomeCustomPromptDeferredUntilNextLaunch,
                            onPermissionGranted = onNotificationPermissionGranted,
                            onSplashPermissionActiveChange = { active ->
                                splashNotificationPermissionActive = active
                            },
                            onPermissionUiActiveChange = { active ->
                                notificationPermissionUiActive = active
                            },
                        )
                    },
                )

                if (pendingRequest is AppLaunchRequest.ForegroundReturn &&
                    currentRoute != Screen.Splash.route
                ) {
                    SplashScreen {
                        launchCoordinator.consumeRequestIfCurrent(pendingRequest)
                    }
                }
            }

            LaunchedEffect(navController, pendingRequest) {
                if (pendingRequest !is AppLaunchRequest.NotificationTarget) return@LaunchedEffect
                if (currentRoute == Screen.Splash.route) return@LaunchedEffect
                navController.navigate(Screen.Splash.route) {
                    launchSingleTop = true
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

internal fun NavHostController.navigateToNotificationTarget(route: String) {
    while (popBackStack()) {
        // Clear the existing stack before rebuilding Home -> target.
    }
    val currentRoute = currentDestination?.route
    navigate(Screen.Home.route) {
        currentRoute?.let { popUpTo(it) { inclusive = true } }
        launchSingleTop = true
    }
    if (route != Screen.Home.route) {
        navigate(route) { launchSingleTop = true }
    }
}
