package com.quickcleanpro.phonecleaner.utils

import android.content.Context
import android.content.SharedPreferences
import com.quickcleanpro.phonecleaner.di.QuickCleanApplication
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object AppPrefsUtils {
    private const val PREFS_NAME = "phone_cleaner_sp"

    private val sp: SharedPreferences
        get() = QuickCleanApplication.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).commit()
    }

    fun getBoolean(key: String): Boolean =
        sp.getBoolean(key, false)

    fun putString(key: String, value: String) {
        sp.edit().putString(key, value).commit()
    }

    fun getString(key: String): String =
        sp.getString(key, "") ?: ""

    fun putLong(key: String, value: Long) {
        sp.edit().putLong(key, value).commit()
    }

    fun getLong(key: String): Long =
        sp.getLong(key, 0L)

    fun putInt(key: String, value: Int) {
        sp.edit().putInt(key, value).commit()
    }

    fun getInt(key: String): Int =
        sp.getInt(key, 0)

    fun getDouble(key: String): Double {
        val strValue = sp.getString(key, "")
        return if (strValue.isNullOrEmpty()) 0.0 else strValue.toDouble()
    }

    fun commitString(key: String?, value: String?) {
        sp.edit().putString(key, value).commit()
    }

    fun getString(key: String, defaultValue: String = ""): String =
        sp.getString(key, defaultValue) ?: defaultValue

    fun commitInt(key: String?, value: Int) {
        sp.edit().putInt(key, value).commit()
    }

    fun getInt(key: String?, failValue: Int): Int =
        sp.getInt(key, failValue)

    fun commitLong(key: String?, value: Long) {
        sp.edit().putLong(key, value).commit()
    }

    fun getLong(key: String?, failValue: Long): Long =
        sp.getLong(key, failValue)

    fun commitBoolean(key: String?, value: Boolean) {
        sp.edit().putBoolean(key, value).commit()
    }

    fun getBoolean(key: String?, failValue: Boolean): Boolean =
        sp.getBoolean(key, failValue)

    fun commitDouble(key: String?, value: Double) {
        sp.edit().putString(key, value.toString()).commit()
    }

    fun getDouble(key: String?, failValue: Double): Double {
        val strValue = sp.getString(key, "")
        return if (strValue.isNullOrEmpty()) failValue else strValue.toDouble()
    }

    fun commitFloat(key: String?, value: Float) {
        sp.edit().putFloat(key, value).commit()
    }

    fun getFloat(key: String?, failValue: Float): Float =
        sp.getFloat(key, failValue)

    class PreferenceDelegate<T>(
        private val key: String,
        private val defaultValue: T
    ) : ReadWriteProperty<Any?, T> {
        @Suppress("UNCHECKED_CAST")
        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            when (defaultValue) {
                is Int -> getInt(key, defaultValue) as T
                is Long -> getLong(key, defaultValue) as T
                is Boolean -> getBoolean(key, defaultValue) as T
                is Float -> getFloat(key, defaultValue) as T
                is String -> getString(key, defaultValue) as T
                is Double -> getDouble(key, defaultValue) as T
                else -> throw IllegalArgumentException("Unsupported type.")
            }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            when (value) {
                is Int -> commitInt(key, value)
                is Long -> commitLong(key, value)
                is Boolean -> commitBoolean(key, value)
                is Float -> commitFloat(key, value)
                is String -> commitString(key, value)
                is Double -> commitDouble(key, value)
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        }
    }

    inline fun <reified T> preference(key: String, defaultValue: T): ReadWriteProperty<Any?, T> =
        PreferenceDelegate(key, defaultValue)
}
