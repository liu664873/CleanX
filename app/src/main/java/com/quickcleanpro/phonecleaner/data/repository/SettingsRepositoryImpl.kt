package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import com.quickcleanpro.phonecleaner.data.local.hasCompletedOnboardingScan
import com.quickcleanpro.phonecleaner.data.local.hasDeniedLocationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.hasDeniedNotificationRuntimePermission
import com.quickcleanpro.phonecleaner.data.local.hasRequestedLocationRuntimePermissionBefore
import com.quickcleanpro.phonecleaner.data.local.hasRequestedNotificationRuntimePermissionBefore
import com.quickcleanpro.phonecleaner.data.local.hasShownNotificationBarExitPrompt
import com.quickcleanpro.phonecleaner.data.local.readLastNotificationPermissionCustomPromptAt
import com.quickcleanpro.phonecleaner.data.local.readLastAutoRatePromptAt
import com.quickcleanpro.phonecleaner.data.local.readTemperatureUnit
import com.quickcleanpro.phonecleaner.data.local.saveLastNotificationPermissionCustomPromptAt
import com.quickcleanpro.phonecleaner.data.local.saveLastAutoRatePromptAt
import com.quickcleanpro.phonecleaner.data.local.saveLocationRuntimePermissionDenied
import com.quickcleanpro.phonecleaner.data.local.saveLocationRuntimePermissionRequestedBefore
import com.quickcleanpro.phonecleaner.data.local.saveNotificationBarExitPromptShown
import com.quickcleanpro.phonecleaner.data.local.saveNotificationRuntimePermissionRequestedBefore
import com.quickcleanpro.phonecleaner.data.local.saveNotificationRuntimePermissionDenied
import com.quickcleanpro.phonecleaner.data.local.saveOnboardingScanCompleted
import com.quickcleanpro.phonecleaner.data.local.saveTemperatureUnit
import com.quickcleanpro.phonecleaner.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    context: Context,
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

    override fun hasRequestedLocationRuntimePermissionBefore(): Boolean =
        hasRequestedLocationRuntimePermissionBefore(appContext)

    override fun saveLocationRuntimePermissionRequestedBefore() {
        saveLocationRuntimePermissionRequestedBefore(appContext)
    }

    override fun hasDeniedNotificationRuntimePermission(): Boolean = hasDeniedNotificationRuntimePermission(appContext)

    override fun saveNotificationRuntimePermissionDenied() {
        saveNotificationRuntimePermissionDenied(appContext)
    }

    override fun hasRequestedNotificationRuntimePermissionBefore(): Boolean =
        hasRequestedNotificationRuntimePermissionBefore(appContext)

    override fun saveNotificationRuntimePermissionRequestedBefore() {
        saveNotificationRuntimePermissionRequestedBefore(appContext)
    }

    override fun readLastNotificationPermissionCustomPromptAt(): Long =
        readLastNotificationPermissionCustomPromptAt(appContext)

    override fun saveLastNotificationPermissionCustomPromptAt(timestampMillis: Long) {
        saveLastNotificationPermissionCustomPromptAt(appContext, timestampMillis)
    }
}
