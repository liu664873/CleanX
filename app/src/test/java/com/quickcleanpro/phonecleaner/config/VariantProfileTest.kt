package com.quickcleanpro.phonecleaner.config

import com.quickcleanpro.phonecleaner.advertise.AdAreaKeys
import com.quickcleanpro.phonecleaner.navigation.AppRoute
import com.quickcleanpro.phonecleaner.navigation.RouteAdPolicy
import com.quickcleanpro.phonecleaner.presentation.common.route.FeatureRegistry
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VariantProfileTest {
    @Test
    fun orderedFeaturesOnlyReturnEnabledFeatures() {
        val profile =
            testProfile(
                enabled =
                    setOf(
                        VariantFeature.JUNK_CLEAN,
                        VariantFeature.PHOTOS,
                        VariantFeature.BATTERY_INFO,
                    ),
                homeFeatureOrder = listOf(VariantFeature.JUNK_CLEAN, VariantFeature.ANTI_VIRUS),
                fileFeatureOrder = listOf(VariantFeature.PHOTOS, VariantFeature.VIDEOS),
                toolboxFeatureOrder = listOf(VariantFeature.BATTERY_INFO, VariantFeature.APP_USAGE),
            )

        assertEquals(listOf(VariantFeature.JUNK_CLEAN), profile.orderedHomeFeatures())
        assertEquals(listOf(VariantFeature.PHOTOS), profile.orderedFileFeatures())
        assertEquals(listOf(VariantFeature.BATTERY_INFO), profile.orderedToolboxFeatures())
    }

    @Test
    fun routeAvailabilityComesFromEnabledFeatures() {
        val profile =
            testProfile(
                enabled =
                    setOf(
                        VariantFeature.JUNK_CLEAN,
                        VariantFeature.PHOTOS,
                    ),
            )

        assertTrue(profile.isRouteEnabled(Screen.Home.route))
        assertTrue(profile.isRouteEnabled(Screen.PhotosManager.route))
        assertFalse(profile.isRouteEnabled(Screen.VideosManager.route))
        assertFalse(profile.isRouteEnabled(Screen.NetworkScanDevices.route))
    }

    @Test
    fun defaultAdPlacementsOnlyIncludeEnabledFeatureRoutes() {
        val placements =
            defaultAdPlacements(
                setOf(
                    VariantFeature.JUNK_CLEAN,
                    VariantFeature.PHOTOS,
                    VariantFeature.DEVICE_INFO,
                ),
            )

        assertEquals(AdAreaKeys.Interstitial.ENTER_JUNK_CLEAN, placements.entryFor(Screen.Scan.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_FILE_MANAGE, placements.entryFor(Screen.PhotosManager.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_DEVICE_INFO, placements.entryFor(Screen.DeviceInfo.route))
        assertNull(placements.entryFor(Screen.WhatsAppCleaner.route))

        assertEquals(AdAreaKeys.Interstitial.JUNK_CLEAN_FINISH, placements.completionFor(Screen.Scan.route))
        assertEquals(AdAreaKeys.Interstitial.FILE_MANAGE_FINISH, placements.completionFor(Screen.PhotosManager.route))
        assertNull(placements.completionFor(Screen.DeviceInfo.route))
    }

    @Test
    fun featureRegistryFiltersEntryAdsForDisabledRoutes() {
        val profile =
            testProfile(
                enabled =
                    setOf(
                        VariantFeature.JUNK_CLEAN,
                        VariantFeature.PHOTOS,
                    ),
            )

        val registry = FeatureRegistry(profile)

        assertTrue(Screen.Scan.route in registry.featureEntryAdPlacements)
        assertTrue(Screen.PhotosManager.route in registry.featureEntryAdPlacements)
        assertFalse(Screen.VideosManager.route in registry.featureEntryAdPlacements)
    }

    @Test
    fun routeAdPolicyFiltersEntryAdsForDisabledRoutes() {
        val profile =
            testProfile(
                enabled =
                    setOf(
                        VariantFeature.JUNK_CLEAN,
                        VariantFeature.PHOTOS,
                    ),
            )

        val policy = RouteAdPolicy(profile)

        assertEquals(AdAreaKeys.Interstitial.ENTER_JUNK_CLEAN, policy.entryPlacement(Screen.Scan.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_FILE_MANAGE, policy.entryPlacement(Screen.PhotosManager.route))
        assertNull(policy.entryPlacement(Screen.VideosManager.route))
    }

    @Test
    fun appRouteEncodesQueryArgs() {
        val route = AppRoute("detail").withArgs(mapOf("name" to "a b&c", "path" to "/a/b"))

        assertEquals("detail?name=a+b%26c&path=%2Fa%2Fb", route)
    }

    private fun testProfile(
        enabled: Set<VariantFeature>,
        homeFeatureOrder: List<VariantFeature> = VariantFeature.entries.toList(),
        fileFeatureOrder: List<VariantFeature> = fileFeatures.toList(),
        toolboxFeatureOrder: List<VariantFeature> = toolboxFeatures.toList(),
    ): VariantProfile =
        VariantProfile(
            variantKey = "test",
            appName = "Test",
            themeKey = "test",
            primaryFeature = VariantFeature.JUNK_CLEAN,
            enabledFeatures = enabled,
            homeFeatureOrder = homeFeatureOrder,
            fileFeatureOrder = fileFeatureOrder,
            toolboxFeatureOrder = toolboxFeatureOrder,
            adProfile =
                VariantAdProfile(
                    unitIds =
                        VariantAdUnitIds(
                            appId = "",
                            appOpen = "",
                            interstitial = "",
                            banner = "",
                            native = "",
                        ),
                    placements = defaultAdPlacements(enabled),
                ),
            legalProfile =
                LegalProfile(
                    termsOfServiceUrl = "",
                    privacyPolicyUrl = "",
                ),
            notificationProfile = NotificationProfile(),
            serviceProfile = VariantServiceProfile(trustlookApiKey = ""),
        )
}
