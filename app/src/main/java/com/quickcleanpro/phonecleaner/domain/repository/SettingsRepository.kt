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

    fun hasDeniedNotificationRuntimePermission(): Boolean

    fun saveNotificationRuntimePermissionDenied()
}

