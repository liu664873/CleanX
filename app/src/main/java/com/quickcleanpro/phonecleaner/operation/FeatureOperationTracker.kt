package com.quickcleanpro.phonecleaner.operation

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import com.quickcleanpro.phonecleaner.advertise.AdvertiseMediator
import com.quickcleanpro.phonecleaner.advertise.AdvertisePageMediator
import com.quickcleanpro.phonecleaner.config.VariantConfigs

interface FeatureOperationTracker {
    fun track(event: FeatureOperationEvent)

    fun trackWithAd(
        event: FeatureOperationEvent,
        onComplete: () -> Unit,
    ) {
        track(event)
        onComplete()
    }
}

object NoOpFeatureOperationTracker : FeatureOperationTracker {
    override fun track(event: FeatureOperationEvent) = Unit
}

class DefaultFeatureOperationTracker(
    private val activityProvider: () -> Activity?,
    private val adPolicy: OperationAdPolicy = OperationAdPolicy(VariantConfigs.current),
    private val mediator: AdvertiseMediator = AdvertisePageMediator,
) : FeatureOperationTracker {
    override fun track(event: FeatureOperationEvent) {
        Log.d(TAG, "operation event: $event")
    }

    override fun trackWithAd(
        event: FeatureOperationEvent,
        onComplete: () -> Unit,
    ) {
        track(event)
        val adKey = adPolicy.adKeyFor(event)
        mediator.showOperationAd(activityProvider(), adKey, onComplete)
    }

    private companion object {
        const val TAG = "CleanXOperation"
    }
}

val LocalFeatureOperationTracker =
    staticCompositionLocalOf<FeatureOperationTracker> {
        NoOpFeatureOperationTracker
    }
