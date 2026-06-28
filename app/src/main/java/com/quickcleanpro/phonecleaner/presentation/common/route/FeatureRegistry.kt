package com.quickcleanpro.phonecleaner.presentation.common.route

import com.quickcleanpro.phonecleaner.config.FeatureGroup
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.config.VariantProfile
import com.quickcleanpro.phonecleaner.navigation.RouteAdPolicy

class FeatureRegistry(
    private val profile: VariantProfile,
) {
    val featureEntryAdPlacements: Map<String, String> =
        RouteAdPolicy(profile).featureEntryAdPlacements

    fun isEnabled(feature: FeatureKey): Boolean = profile.isEnabled(feature)

    fun hasEnabledFeatureIn(group: FeatureGroup): Boolean =
        profile.enabledFeatures.any { feature ->
            com.quickcleanpro.phonecleaner.config.FeatureCatalog.byKey[feature]?.group == group
        }
}
