package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.model.applock.AppLockApp
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.utils.AdvancedLockAppInfoHelper
import com.quickcleanpro.phonecleaner.utils.AppLockPermissionUtils
import com.quickcleanpro.phonecleaner.utils.AppPrefsUtils

class AppLockRepositoryImpl(context: Context) : AppLockRepository {

    private val appContext = context.applicationContext
    private val appInfoHelper = AdvancedLockAppInfoHelper(appContext)

    override fun isPinSet(): Boolean =
        AppPrefsUtils.getBoolean(KEY_IS_SET_LOCK, false) &&
            AppPrefsUtils.getString(KEY_PIN, "").length == PIN_LENGTH

    override fun savePin(pin: String) {
        AppPrefsUtils.putString(KEY_PIN, pin.take(PIN_LENGTH))
        AppPrefsUtils.putBoolean(KEY_IS_SET_LOCK, true)
    }

    override fun verifyPin(pin: String): Boolean =
        pin.length == PIN_LENGTH && pin == AppPrefsUtils.getString(KEY_PIN, "")

    override fun lockedPackages(): Set<String> =
        decodePackages(AppPrefsUtils.getString(KEY_LOCKED_PACKAGES, ""))

    override fun lockedAppCount(): Int =
        if (isPinSet()) lockedPackages().size else 0

    override fun isPackageLocked(packageName: String): Boolean =
        packageName in lockedPackages()

    override fun setPackageLocked(packageName: String, locked: Boolean) {
        if (packageName.isBlank() || packageName == appContext.packageName) return
        val current = lockedPackages().toMutableSet()
        if (locked) current += packageName else current -= packageName
        setLockedPackages(current)
    }

    override fun setLockedPackages(packageNames: Set<String>) {
        AppPrefsUtils.putString(
            KEY_LOCKED_PACKAGES,
            packageNames
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() && it != appContext.packageName }
                .distinct()
                .joinToString(",")
        )
    }

    override suspend fun lockableApps(): List<AppLockApp> {
        val locked = if (isPinSet()) lockedPackages() else DEFAULT_LOCK_PACKAGES
        return appInfoHelper.getApps()
            .map { app ->
                app.copy(isLocked = app.packageName in locked)
            }
            .sortedWith(
                compareByDescending<AppLockApp> { it.isLocked }
                    .thenBy { it.appName.lowercase() }
                    .thenBy { it.packageName }
            )
    }

    override fun isMonitoringEnabled(): Boolean =
        AppPrefsUtils.getBoolean(KEY_MONITORING_ENABLED, true)

    override fun setMonitoringEnabled(enabled: Boolean) {
        AppPrefsUtils.putBoolean(KEY_MONITORING_ENABLED, enabled)
    }

    override fun isAutoLockEnabled(): Boolean =
        AppPrefsUtils.getBoolean(KEY_AUTO_LOCK, false)

    override fun setAutoLockEnabled(enabled: Boolean) {
        AppPrefsUtils.putBoolean(KEY_AUTO_LOCK, enabled)
    }

    override fun isVibrationEnabled(): Boolean =
        AppPrefsUtils.getBoolean(KEY_VIBRATE_ON_KEYPAD, true)

    override fun setVibrationEnabled(enabled: Boolean) {
        AppPrefsUtils.putBoolean(KEY_VIBRATE_ON_KEYPAD, enabled)
    }

    override fun hasUsageAccess(): Boolean =
        AppLockPermissionUtils.hasUsageStatsPermission(appContext)

    override fun hasOverlayPermission(): Boolean =
        AppLockPermissionUtils.canDrawOverlays(appContext)

    override fun overlayPermissionIntent(): Intent? =
        AppLockPermissionUtils.getOverlayPermissionIntent(appContext)

    override fun handlePackageAdded(packageName: String) {
        if (isPinSet() && isAutoLockEnabled() && isLaunchable(packageName)) {
            setPackageLocked(packageName, true)
        }
    }

    override fun handlePackageRemoved(packageName: String) {
        setPackageLocked(packageName, false)
    }

    private fun isLaunchable(packageName: String): Boolean =
        packageName.isNotBlank() &&
            packageName != appContext.packageName &&
            appContext.packageManager.getLaunchIntentForPackage(packageName) != null

    private fun decodePackages(value: String): Set<String> =
        value.split(',')
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    companion object {
        const val PREFS_NAME = "phone_cleaner_sp"
        const val KEY_PIN = "pin"
        const val KEY_IS_SET_LOCK = "is_set_lock"
        const val KEY_LOCKED_PACKAGES = "lockApp_packageNames"
        const val KEY_MONITORING_ENABLED = "app_lock_monitoring_enabled"
        const val KEY_AUTO_LOCK = "turn_on_auto_lock"
        const val KEY_VIBRATE_ON_KEYPAD = "vibrate_on_keypad"
        const val PIN_LENGTH = 4

        private val DEFAULT_LOCK_PACKAGES = setOf(
            "com.google.android.gm",
            "com.google.android.apps.photos",
            "com.facebook.katana",
            "com.instagram.android",
            "com.twitter.android",
            "com.whatsapp",
            "com.sec.android.gallery3d"
        )
    }
}
