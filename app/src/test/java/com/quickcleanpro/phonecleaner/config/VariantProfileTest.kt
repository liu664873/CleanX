package com.quickcleanpro.phonecleaner.config

import com.quickcleanpro.phonecleaner.advertise.AdAreaKeys
import com.quickcleanpro.phonecleaner.navigation.AppRoute
import com.quickcleanpro.phonecleaner.navigation.RouteAdPolicy
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VariantProfileTest {
    @Test
    fun defaultAdPlacementsComeFromFeatureCatalog() {
        val placements = defaultAdPlacements()

        assertEquals(AdAreaKeys.Interstitial.ENTER_JUNK_CLEAN, placements.entryFor(Screen.Scan.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_FILE_MANAGE, placements.entryFor(Screen.PhotosManager.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_DEVICE_INFO, placements.entryFor(Screen.DeviceInfo.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_WHATSAPP_CLEANER, placements.entryFor(Screen.WhatsAppCleaner.route))

        assertEquals(AdAreaKeys.Interstitial.JUNK_CLEAN_FINISH, placements.completionFor(Screen.Scan.route))
        assertEquals(AdAreaKeys.Interstitial.FILE_MANAGE_FINISH, placements.completionFor(Screen.PhotosManager.route))
        assertNull(placements.completionFor(Screen.DeviceInfo.route))
    }

    @Test
    fun routeAdPolicyReturnsConfiguredPlacementsWithoutFeatureFiltering() {
        val policy = RouteAdPolicy(testProfile())

        assertEquals(AdAreaKeys.Interstitial.ENTER_JUNK_CLEAN, policy.entryPlacement(Screen.Scan.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_FILE_MANAGE, policy.entryPlacement(Screen.PhotosManager.route))
        assertEquals(AdAreaKeys.Interstitial.ENTER_WHATSAPP_CLEANER, policy.entryPlacement(Screen.WhatsAppCleaner.route))
    }

    @Test
    fun featureCatalogStillMapsNestedRoutesToOwningFeature() {
        assertEquals(FeatureKey.ANTI_VIRUS, FeatureCatalog.featureForRoute(AppRoute.VirusQuickScan.value))
        assertEquals(FeatureKey.NETWORK_SCAN, FeatureCatalog.featureForRoute(AppRoute.NetworkScanDevices.value))
    }

    @Test
    fun appRouteEncodesQueryArgs() {
        val route = AppRoute("detail").withArgs(mapOf("name" to "a b&c", "path" to "/a/b"))

        assertEquals("detail?name=a+b%26c&path=%2Fa%2Fb", route)
    }

    @Test
    fun variantProfileKeepsOnlyNonUiVariantConfiguration() {
        val profile = testProfile()

        assertEquals("storagecleaner", profile.variantKey)
        assertEquals("storage_cleaner", profile.themeKey)
        assertEquals("https://terms.example", profile.termsOfServiceUrl)
        assertEquals("https://privacy.example", profile.privacyPolicyUrl)
        assertEquals("trustlook-key", profile.trustlookApiKey)
        assertTrue(profile.notificationProfile.enabledToolRoutes.contains(AppRoute.BatteryInfo.value))
    }

    private fun testProfile(): VariantProfile =
        VariantProfile(
            variantKey = "storagecleaner",
            appName = "Storage Cleaner",
            themeKey = "storage_cleaner",
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
                    placements = defaultAdPlacements(),
                ),
            legalProfile =
                LegalProfile(
                    termsOfServiceUrl = "https://terms.example",
                    privacyPolicyUrl = "https://privacy.example",
                ),
            notificationProfile = storageCleanerNotificationProfile(),
            serviceProfile = VariantServiceProfile(trustlookApiKey = "trustlook-key"),
        )
}
