package com.quickcleanpro.phonecleaner.data.local

import android.content.Context

internal const val SETTINGS_PREFS = "quick_clean_settings"
internal const val KEY_TEMPERATURE_UNIT = "temperature_unit"
internal const val KEY_LAST_AUTO_RATE_PROMPT_AT = "last_auto_rate_prompt_at"
internal const val KEY_NOTIFICATION_BAR_EXIT_PROMPT_SHOWN = "notification_bar_exit_prompt_shown"
internal const val KEY_ONBOARDING_SCAN_COMPLETED = "onboarding_scan_completed"
internal const val KEY_LOCATION_RUNTIME_PERMISSION_DENIED = "location_runtime_permission_denied"
internal const val KEY_NOTIFICATION_RUNTIME_PERMISSION_DENIED = "notification_runtime_permission_denied"

/** 璇诲彇褰撳墠淇濆瓨鐨勬俯搴﹀崟浣嶅亸濂姐€?*/
internal fun readTemperatureUnit(context: Context): String =
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getString(KEY_TEMPERATURE_UNIT, "C")
        ?: "C"

/** 淇濆瓨鐢ㄦ埛閫夋嫨鐨勬俯搴﹀崟浣嶅亸濂姐€?*/
internal fun saveTemperatureUnit(
    context: Context,
    unit: String,
) {
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_TEMPERATURE_UNIT, unit)
        .apply()
}

internal fun readLastAutoRatePromptAt(context: Context): Long =
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getLong(KEY_LAST_AUTO_RATE_PROMPT_AT, 0L)

internal fun saveLastAutoRatePromptAt(
    context: Context,
    timestampMillis: Long,
) {
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putLong(KEY_LAST_AUTO_RATE_PROMPT_AT, timestampMillis)
        .apply()
}

internal fun hasShownNotificationBarExitPrompt(context: Context): Boolean =
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_NOTIFICATION_BAR_EXIT_PROMPT_SHOWN, false)

internal fun saveNotificationBarExitPromptShown(context: Context) {
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_NOTIFICATION_BAR_EXIT_PROMPT_SHOWN, true)
        .apply()
}

internal fun hasCompletedOnboardingScan(context: Context): Boolean =
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_ONBOARDING_SCAN_COMPLETED, false)

internal fun saveOnboardingScanCompleted(context: Context) {
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_ONBOARDING_SCAN_COMPLETED, true)
        .apply()
}

internal fun hasDeniedLocationRuntimePermission(context: Context): Boolean =
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_LOCATION_RUNTIME_PERMISSION_DENIED, false)

internal fun saveLocationRuntimePermissionDenied(context: Context) {
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_LOCATION_RUNTIME_PERMISSION_DENIED, true)
        .apply()
}

internal fun hasDeniedNotificationRuntimePermission(context: Context): Boolean =
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_NOTIFICATION_RUNTIME_PERMISSION_DENIED, false)

internal fun saveNotificationRuntimePermissionDenied(context: Context) {
    context
        .getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_NOTIFICATION_RUNTIME_PERMISSION_DENIED, true)
        .apply()
}
