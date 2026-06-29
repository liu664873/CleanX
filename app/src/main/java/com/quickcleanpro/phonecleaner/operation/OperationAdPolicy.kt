package com.quickcleanpro.phonecleaner.operation

import com.quickcleanpro.phonecleaner.advertise.AdAreaKeys
import com.quickcleanpro.phonecleaner.config.FeatureCatalog
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.config.VariantProfile
import com.quickcleanpro.phonecleaner.config.fileFeatures

class OperationAdPolicy(
    private val profile: VariantProfile,
) {
    fun adKeyFor(event: FeatureOperationEvent): String? {
        return when (event) {
            is FeatureOperationEvent.OperationFinished ->
                if (event.success) finishAdFor(event.feature) else null
            is FeatureOperationEvent.PermissionRejected ->
                AdAreaKeys.Interstitial.FILE_ACCESS_CANCEL.takeIf {
                    event.feature in fileFeatures || event.feature == FeatureKey.JUNK_CLEAN
                }
            is FeatureOperationEvent.ReturnHome ->
                finishAdFor(event.feature) ?: AdAreaKeys.Interstitial.RETURN_HOME_PAGE
            is FeatureOperationEvent.ActionRequested,
            is FeatureOperationEvent.OperationStarted,
            is FeatureOperationEvent.ResultShown,
            is FeatureOperationEvent.ScanFinished,
            is FeatureOperationEvent.ScanStarted,
            -> null
        }
    }

    private fun finishAdFor(feature: FeatureKey): String? {
        val route = FeatureCatalog.routeFor(feature)?.value ?: return null
        return profile.adProfile.placements.completionFor(route)
    }
}
