package com.quickcleanpro.phonecleaner.domain.repository

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

    fun hasRequestedLocationRuntimePermissionBefore(): Boolean

    fun saveLocationRuntimePermissionRequestedBefore()

    fun hasDeniedNotificationRuntimePermission(): Boolean

    fun saveNotificationRuntimePermissionDenied()

    fun hasRequestedNotificationRuntimePermissionBefore(): Boolean

    fun saveNotificationRuntimePermissionRequestedBefore()

    fun readLastNotificationPermissionCustomPromptAt(): Long

    fun saveLastNotificationPermissionCustomPromptAt(timestampMillis: Long)
}
