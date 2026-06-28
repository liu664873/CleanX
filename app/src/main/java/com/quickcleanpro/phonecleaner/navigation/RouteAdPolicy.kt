package com.quickcleanpro.phonecleaner.navigation

import com.quickcleanpro.phonecleaner.config.VariantAdProfile
import com.quickcleanpro.phonecleaner.config.VariantProfile

class RouteAdPolicy(
    private val adProfile: VariantAdProfile,
    private val isRouteEnabled: (String) -> Boolean,
) {
    constructor(profile: VariantProfile) : this(
        adProfile = profile.adProfile,
        isRouteEnabled = profile::isRouteEnabled,
    )

    val featureEntryAdPlacements: Map<String, String> =
        adProfile.placements.featureEntry.filterKeys(isRouteEnabled)

    fun entryPlacement(route: String): String? =
        featureEntryAdPlacements[route]

    fun completionPlacement(route: String): String? =
        adProfile.placements.completionFor(route).takeIf { isRouteEnabled(route) }
}
