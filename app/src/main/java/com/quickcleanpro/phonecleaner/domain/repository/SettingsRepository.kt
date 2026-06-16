package com.quickcleanpro.phonecleaner.domain.repository

import android.content.Intent

interface SettingsRepository {
    fun readTemperatureUnit(): String

    fun saveTemperatureUnit(unit: String)

    fun readLastAutoRatePromptAt(): Long

    fun saveLastAutoRatePromptAt(timestampMillis: Long)

    fun hasShownNotificationBarExitPrompt(): Boolean

    fun saveNotificationBarExitPromptShown()

    fun hasCompletedOnboardingScan(): Boolean

    fun saveOnboardingScanCompleted()

    fun hasDeniedLocationRuntimePermission(): Boolean

    fun saveLocationRuntimePermissionDenied()

    fun hasDeniedNotificationRuntimePermission(): Boolean

    fun saveNotificationRuntimePermissionDenied()

    fun hasPostNotificationsPermission(): Boolean

    fun postNotificationsSettingsIntent(): Intent

    fun hasStoragePermission(): Boolean

    fun storagePermissionIntent(): Intent

    fun storagePermissionFallbackIntent(): Intent?

    fun hasAppUsageAccess(): Boolean

    fun resetAppUsagePermissionCache()

    fun appUsageSettingsIntent(): Intent

    fun hasLocationPermission(): Boolean

    fun appSettingsIntent(): Intent

    fun hasNotificationListenerAccess(): Boolean

    fun notificationListenerSettingsIntent(): Intent

    fun hasOverlayPermission(): Boolean

    fun overlayPermissionIntent(): Intent?
}
