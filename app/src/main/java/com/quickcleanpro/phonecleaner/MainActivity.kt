package com.quickcleanpro.phonecleaner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.core.permission.appSettingsIntent
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchCoordinator
import com.quickcleanpro.phonecleaner.presentation.app.CleanXAppRoot
import com.quickcleanpro.phonecleaner.presentation.app.PersistentNotificationLifecycleController
import com.quickcleanpro.phonecleaner.presentation.theme.CleanXTheme
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val settingsRepository: SettingsRepository by inject()
    private val appLockRepository: AppLockRepository by inject()
    private val launchCoordinator = AppLaunchCoordinator()

    private val notificationLifecycleController by lazy {
        PersistentNotificationLifecycleController(
            context = this,
            appLockRepository = appLockRepository,
            hasNotificationPermission = ::hasNotificationPermission,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        launchCoordinator.onCreate(intent)

        setContent {
            CleanXTheme {
                CleanXAppRoot(
                    launchCoordinator = launchCoordinator,
                    settingsRepository = settingsRepository,
                    hasNotificationPermission = ::hasNotificationPermission,
                    openAppSettings = {
                        launchCoordinator.markExternalActivityLaunch()
                        runCatching { startActivity(appSettingsIntent(this)) }
                            .onFailure { launchCoordinator.cancelExternalActivityLaunch() }
                    },
                    onStartPersistentNotification = {
                        runCatching { notificationLifecycleController.startServiceWhenAllowed() }
                    },
                )
            }
        }

        notificationLifecycleController.onCreate()
    }

    override fun onStart() {
        super.onStart()
        launchCoordinator.onStart()
        notificationLifecycleController.onStart()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchCoordinator.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        launchCoordinator.onResume()
    }

    override fun onStop() {
        launchCoordinator.onStop()
        notificationLifecycleController.onStop()
        super.onStop()
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
