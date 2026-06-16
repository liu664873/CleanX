package com.quickcleanpro.phonecleaner.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesUtils {
    const val KEY_ONBOARDING_SCAN_COMPLETED = "onboarding_scan_completed"

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
        edit(commit) { putBoolean(key, value) }
    }

    fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean = prefs.getBoolean(key, defaultValue)

    fun putString(
        key: String,
        value: String?,
        commit: Boolean = false,
    ) {
        edit(commit) { putString(key, value) }
    }

    fun getString(
        key: String,
        defaultValue: String = "",
    ): String = prefs.getString(key, defaultValue) ?: defaultValue

    fun putInt(
        key: String,
        value: Int,
        commit: Boolean = false,
    ) {
        edit(commit) { putInt(key, value) }
    }

    fun getInt(
        key: String,
        defaultValue: Int = 0,
    ): Int = prefs.getInt(key, defaultValue)

    fun putLong(
        key: String,
        value: Long,
        commit: Boolean = false,
    ) {
        edit(commit) { putLong(key, value) }
    }

    fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long = prefs.getLong(key, defaultValue)

    fun putFloat(
        key: String,
        value: Float,
        commit: Boolean = false,
    ) {
        edit(commit) { putFloat(key, value) }
    }

    fun getFloat(
        key: String,
        defaultValue: Float = 0f,
    ): Float = prefs.getFloat(key, defaultValue)

    fun putStringSet(
        key: String,
        value: Set<String>,
        commit: Boolean = false,
    ) {
        edit(commit) { putStringSet(key, value) }
    }

    fun getStringSet(
        key: String,
        defaultValue: Set<String> = emptySet(),
    ): Set<String> = prefs.getStringSet(key, defaultValue)?.toSet() ?: defaultValue

    fun contains(key: String): Boolean = prefs.contains(key)

    fun remove(
        key: String,
        commit: Boolean = false,
    ) {
        edit(commit) { remove(key) }
    }

    fun clear(commit: Boolean = false) {
        edit(commit) { clear() }
    }

    private val prefs: SharedPreferences
        get() {
            check(::preferences.isInitialized) {
                "SharedPreferencesUtils.init(context) must be called before use."
            }
            return preferences
        }

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
