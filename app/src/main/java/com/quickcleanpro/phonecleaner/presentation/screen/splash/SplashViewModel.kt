package com.quickcleanpro.phonecleaner.presentation.screen.splash

import androidx.lifecycle.ViewModel
import com.quickcleanpro.phonecleaner.utils.SharedPreferencesUtils

class SplashViewModel : ViewModel() {
    fun shouldShowOnboardingScan(): Boolean =
        !SharedPreferencesUtils.getBoolean(
            key = SharedPreferencesUtils.KEY_ONBOARDING_SCAN_COMPLETED,
            defaultValue = false,
        )
}
