package com.quickcleanpro.phonecleaner.config

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.BuildConfig

object AppConfig {
    val TERMS_OF_SERVICE_URL: String = BuildConfig.TERMS_OF_SERVICE_URL
    val PRIVACY_POLICY_URL: String = BuildConfig.PRIVACY_POLICY_URL

    // ================ Notification Timing ================
    /** Triggered-notification push window duration (24 hours). */
    const val PUSH_WINDOW_MS = 24L * 60L * 60L * 1000L
    /** Maximum triggered notifications per push window. */
    const val MAX_TRIGGERED_NOTIFICATIONS_PER_DAY = 8
    /** Minimum interval between any two triggered notifications (30 min). */
    const val GLOBAL_TRIGGER_INTERVAL_MS = 30L * 60L * 1000L
    /** Minimum interval for the same trigger scene (2 hours). */
    const val DEFAULT_TRIGGER_INTERVAL_MS = 2L * 60L * 60L * 1000L
    /** Interval after app goes to background (1 hour). */
    const val BACKGROUND_TRIGGER_INTERVAL_MS = 60L * 60L * 1000L
    /** Interval for power-connected/disconnected triggers (3 hours). */
    const val POWER_TRIGGER_INTERVAL_MS = 3L * 60L * 60L * 1000L
    /** Delay after screen-on before sending a notification (8 seconds). */
    const val SCREEN_ON_TRIGGER_DELAY_MS = 8_000L

    // ================ Helpers ================
    fun hasPostNotificationsPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            runCatching {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
}
