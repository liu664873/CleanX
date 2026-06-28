package com.quickcleanpro.phonecleaner.advertise

import android.util.Log

object AdEventLogger {
    private const val TAG = "CleanXAds"

    fun initialized() {
        Log.i(TAG, "advertise sdk initialized")
    }

    fun validationWarning(message: String) {
        Log.w(TAG, "validation warning: $message")
    }

    fun showRequested(areaKey: String) {
        Log.d(TAG, "show requested: $areaKey")
    }

    fun showSkipped(reason: String) {
        Log.d(TAG, "show skipped: $reason")
    }

    fun showFailed(
        areaKey: String?,
        throwable: Throwable,
    ) {
        Log.w(TAG, "show failed: ${areaKey.orEmpty()}", throwable)
    }

    fun closed(areaKey: String) {
        Log.d(TAG, "closed: $areaKey")
    }
}
