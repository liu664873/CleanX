package com.quickcleanpro.phonecleaner.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {
    const val KEY_ONBOARDING_SCAN_COMPLETED = "onboarding_scan_completed"
    const val KEY_VIRUS_SCAN_NOTICE_ACCEPTED = "virus_scan_notice_accepted"
    const val KEY_VIRUS_INSTALLED_APPS_ACCESS_FAILED_ONCE = "virus_installed_apps_access_failed_once"

    private const val DEFAULT_PREFS_NAME = "quick_clean_settings"

    private lateinit var preferences: SharedPreferences

    fun init(
        context: Context,
        prefsName: String = DEFAULT_PREFS_NAME,
    ) {
        preferences = context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    fun putBoolean(
        key: String,
        value: Boolean,
        commit: Boolean = false,
    ) {
        ifReady { edit(commit) { putBoolean(key, value) } }
    }

    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean = ifReady { prefs.getBoolean(key, defaultValue) } ?: defaultValue

    fun putString(
        key: String,
        value: String?,
        commit: Boolean = false,
    ) {
        ifReady { edit(commit) { putString(key, value) } }
    }

    fun getString(
        key: String,
        defaultValue: String = "",
    ): String = ifReady { prefs.getString(key, defaultValue) } ?: defaultValue

    fun putInt(
        key: String,
        value: Int,
        commit: Boolean = false,
    ) {
        ifReady { edit(commit) { putInt(key, value) } }
    }

    fun getInt(
        key: String,
        defaultValue: Int = 0,
    ): Int = ifReady { prefs.getInt(key, defaultValue) } ?: defaultValue

    fun putLong(
        key: String,
        value: Long,
        commit: Boolean = false,
    ) {
        ifReady { edit(commit) { putLong(key, value) } }
    }

    fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long = ifReady { prefs.getLong(key, defaultValue) } ?: defaultValue

    fun putFloat(
        key: String,
        value: Float,
        commit: Boolean = false,
    ) {
        ifReady { edit(commit) { putFloat(key, value) } }
    }

    fun getFloat(
        key: String,
        defaultValue: Float = 0f,
    ): Float = ifReady { prefs.getFloat(key, defaultValue) } ?: defaultValue

    fun putStringSet(
        key: String,
        value: Set<String>,
        commit: Boolean = false,
    ) {
        ifReady { edit(commit) { putStringSet(key, value) } }
    }

    fun getStringSet(
        key: String,
        defaultValue: Set<String> = emptySet(),
    ): Set<String> = ifReady { prefs.getStringSet(key, defaultValue)?.toSet() } ?: defaultValue

    fun contains(key: String): Boolean = ifReady { prefs.contains(key) } ?: false

    fun remove(
        key: String,
        commit: Boolean = false,
    ) {
        ifReady { edit(commit) { remove(key) } }
    }

    fun clear(commit: Boolean = false) {
        ifReady { edit(commit) { clear() } }
    }

    private inline fun <T> ifReady(block: () -> T): T? =
        runCatching {
            check(::preferences.isInitialized) { "SharedPreferencesUtils not initialized" }
            block()
        }.getOrNull()

    private val prefs: SharedPreferences
        get() = preferences

    private inline fun edit(
        commit: Boolean,
        block: SharedPreferences.Editor.() -> Unit,
    ) {
        val editor = prefs.edit().apply(block)
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }
}
