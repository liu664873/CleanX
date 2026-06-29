package com.quickcleanpro.phonecleaner.navigation

import com.quickcleanpro.phonecleaner.config.VariantAdProfile
import com.quickcleanpro.phonecleaner.config.VariantProfile

class RouteAdPolicy(
    private val adProfile: VariantAdProfile,
) {
    constructor(profile: VariantProfile) : this(
        adProfile = profile.adProfile,
    )

    val featureEntryAdPlacements: Map<String, String> =
        adProfile.placements.featureEntry

    fun entryPlacement(route: String): String? =
        featureEntryAdPlacements[route]

    fun completionPlacement(route: String): String? =
        adProfile.placements.completionFor(route)
}
