package com.quickcleanpro.phonecleaner.presentation.common

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.cleanXDebouncedClick(
    enabled: Boolean = true,
    debounceMillis: Long = 500L,
    onClick: () -> Unit
): Modifier = composed {
    val lastClickTime = remember { LongArray(1) }
    clickable(enabled = enabled) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastClickTime[0] >= debounceMillis) {
            lastClickTime[0] = now
            onClick()
        }
    }
}
