package com.quickcleanpro.phonecleaner.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.data.local.hasCompletedOnboardingScan
import com.quickcleanpro.phonecleaner.data.local.hasDeniedLocationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.hasDeniedNotificationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.hasShownNotificationBarExitPrompt
import com.quickcleanpro.phonecleaner.data.local.readLastAutoRatePromptAt
import com.quickcleanpro.phonecleaner.data.local.readTemperatureUnit
import com.quickcleanpro.phonecleaner.data.local.saveLastAutoRatePromptAt
import com.quickcleanpro.phonecleaner.data.local.saveLocationRuntimePermissionDenied
import com.quickcleanpro.phonecleaner.data.local.saveNotificationBarExitPromptShown
import com.quickcleanpro.phonecleaner.data.local.saveNotificationRuntimePermissionDenied
import com.quickcleanpro.phonecleaner.data.local.saveOnboardingScanCompleted
import com.quickcleanpro.phonecleaner.data.local.saveTemperatureUnit
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository
import com.quickcleanpro.phonecleaner.domain.repository.ToolboxRepository
import com.quickcleanpro.phonecleaner.utils.AppLockPermissionUtils

class SettingsRepositoryImpl(
    context: Context,
    private val fileRepository: FileRepository,
    private val toolboxRepository: ToolboxRepository,
) : SettingsRepository {
    private val appContext = context.applicationContext

    override fun readTemperatureUnit(): String = readTemperatureUnit(appContext)

    override fun saveTemperatureUnit(unit: String) {
        saveTemperatureUnit(appContext, unit)
    }

    override fun readLastAutoRatePromptAt(): Long = readLastAutoRatePromptAt(appContext)

    override fun saveLastAutoRatePromptAt(timestampMillis: Long) {
        saveLastAutoRatePromptAt(appContext, timestampMillis)
    }

    override fun hasShownNotificationBarExitPrompt(): Boolean = hasShownNotificationBarExitPrompt(appContext)

    override fun saveNotificationBarExitPromptShown() {
        saveNotificationBarExitPromptShown(appContext)
    }

    override fun hasCompletedOnboardingScan(): Boolean = hasCompletedOnboardingScan(appContext)

    override fun saveOnboardingScanCompleted() {
        saveOnboardingScanCompleted(appContext)
    }

    override fun hasDeniedLocationRuntimePermission(): Boolean = hasDeniedLocationRuntimePermission(appContext)

    override fun saveLocationRuntimePermissionDenied() {
        saveLocationRuntimePermissionDenied(appContext)
    }

    override fun hasDeniedNotificationRuntimePermission(): Boolean = hasDeniedNotificationRuntimePermission(appContext)

    override fun saveNotificationRuntimePermissionDenied() {
        saveNotificationRuntimePermissionDenied(appContext)
    }

    override fun hasStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return runCatching { fileRepository.hasAllFilesAccess() }.getOrDefault(false)
        }
        return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun storagePermissionIntent(): Intent =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fileRepository.allFilesAccessIntent()
            } else {
                appSettingsIntent()
            }
        }.getOrElse { storagePermissionFallbackIntent() ?: appSettingsIntent() }

    override fun storagePermissionFallbackIntent(): Intent? =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fileRepository.allFilesAccessFallbackIntent()
            } else {
                null
            }
        }.getOrNull()

    override fun hasAppUsageAccess(): Boolean = runCatching { toolboxRepository.hasAppUsageAccess() }.getOrDefault(false)

    override fun resetAppUsagePermissionCache() {
        runCatching { toolboxRepository.resetAppUsagePermissionCache() }
    }

    override fun appUsageSettingsIntent(): Intent =
        runCatching { toolboxRepository.appUsageSettingsIntent() }
            .getOrElse { appSettingsIntent() }

    override fun hasLocationPermission(): Boolean =
        runCatching { hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) }.getOrDefault(false)

    override fun appSettingsIntent(): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", appContext.packageName, null)
        }

    override fun hasNotificationListenerAccess(): Boolean =
        runCatching { toolboxRepository.hasNotificationListenerAccess() }.getOrDefault(false)

    override fun notificationListenerSettingsIntent(): Intent =
        runCatching { toolboxRepository.notificationListenerSettingsIntent() }
            .getOrElse { appSettingsIntent() }

    override fun hasOverlayPermission(): Boolean = runCatching { AppLockPermissionUtils.canDrawOverlays(appContext) }.getOrDefault(false)

    override fun overlayPermissionIntent(): Intent? =
        runCatching { AppLockPermissionUtils.getOverlayPermissionIntent(appContext) }.getOrNull()

    private fun hasPermission(permission: String): Boolean =
        runCatching {
            ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
}
