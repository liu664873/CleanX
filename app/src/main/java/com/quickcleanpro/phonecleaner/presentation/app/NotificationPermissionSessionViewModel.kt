package com.quickcleanpro.phonecleaner.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class NotificationPermissionSessionViewModel : ViewModel() {
    var isHomeCustomPromptDeferredUntilNextLaunch by mutableStateOf(false)
        private set

    fun markHomeCustomPromptDeferredUntilNextLaunch() {
        isHomeCustomPromptDeferredUntilNextLaunch = true
    }
}
