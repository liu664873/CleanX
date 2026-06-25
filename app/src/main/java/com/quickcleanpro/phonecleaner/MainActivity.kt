package com.quickcleanpro.phonecleaner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.quickcleanpro.phonecleaner.config.AppConfig
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.presentation.app.AppLaunchCoordinator
import com.quickcleanpro.phonecleaner.presentation.app.CleanXAppRoot
import com.quickcleanpro.phonecleaner.presentation.app.PersistentNotificationLifecycleController
import com.quickcleanpro.phonecleaner.presentation.theme.CleanXTheme
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {
    private val appLockRepository: AppLockRepository by inject()
    private val launchCoordinator = AppLaunchCoordinator()

    private val notificationLifecycleController by lazy {
        PersistentNotificationLifecycleController(
            context = this,
            appLockRepository = appLockRepository,
            hasNotificationPermission = { AppConfig.hasPostNotificationsPermission(this) },
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
                    onNotificationPermissionGranted = notificationLifecycleController::startServiceWhenAllowed,
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
}
