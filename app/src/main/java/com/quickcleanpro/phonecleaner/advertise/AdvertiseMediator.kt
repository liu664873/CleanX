package com.quickcleanpro.phonecleaner.advertise

import android.app.Activity
import com.quickcleanpro.phonecleaner.config.FeatureKey

interface AdvertiseMediator {
    fun showEntryAd(
        activity: Activity?,
        feature: FeatureKey,
        onClosed: () -> Unit,
    )

    fun showFinishAd(
        activity: Activity?,
        feature: FeatureKey,
        onClosed: () -> Unit,
    )

    fun showReturnHomeAd(
        activity: Activity?,
        onClosed: () -> Unit,
    )

    fun showOperationAd(
        activity: Activity?,
        areaKey: String?,
        onClosed: () -> Unit,
    ) {
        onClosed()
    }

    fun suppressNextAppOpenAd()
}
