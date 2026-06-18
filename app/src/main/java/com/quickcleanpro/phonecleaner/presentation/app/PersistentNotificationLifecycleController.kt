package com.quickcleanpro.phonecleaner.presentation.app

import android.content.Context
import com.quickcleanpro.phonecleaner.data.source.notification.PersistentNotificationService
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository

class PersistentNotificationLifecycleController(
    private val context: Context,
    private val appLockRepository: AppLockRepository,
    private val hasNotificationPermission: () -> Boolean,
) {
    fun onCreate() {
        startServiceWhenAllowed()
        syncMonitoringState()
    }

    fun onStart() {
        PersistentNotificationService.setAppInForeground(true)
    }

    fun onStop() {
        PersistentNotificationService.notifyAppBackground(context)
    }

    fun startServiceWhenAllowed() {
        if (hasNotificationPermission()) {
            PersistentNotificationService.start(context)
        }
    }

    private fun syncMonitoringState() {
        val canMonitor =
            runCatching {
                appLockRepository.isPinSet() &&
                    appLockRepository.isMonitoringEnabled() &&
                    appLockRepository.lockedAppCount() > 0 &&
                    appLockRepository.hasOverlayPermission() &&
                    appLockRepository.hasUsageAccess()
            }.getOrDefault(false)
        if (canMonitor) {
            runCatching { PersistentNotificationService.enableMonitoring(context) }
        } else {
            runCatching { PersistentNotificationService.disableMonitoring(context) }
        }
    }
}
