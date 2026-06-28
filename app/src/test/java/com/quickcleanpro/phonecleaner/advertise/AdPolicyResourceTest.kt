package com.quickcleanpro.phonecleaner.advertise

import com.quickcleanpro.phonecleaner.config.FeatureCatalog
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.config.defaultAdPlacements
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AdPolicyResourceTest {
    private val root = File(requireNotNull(System.getProperty("user.dir")))
    private val rawDir = File(root, "src/main/res/raw")

    @Test
    fun featureInterstitialAreaKeysExistInAdPolicy() {
        val policyKeys = areaKeysFrom(File(rawDir, "ad_policy.json"))
        val placements = defaultAdPlacements(FeatureKey.entries.toSet())
        val requiredKeys =
            FeatureCatalog.specs
                .flatMap { spec -> listOfNotNull(spec.entryAdKey, spec.finishAdKey) }
                .toSet() +
                placements.featureEntry.values +
                placements.featureCompletion.values +
                setOf(
                    AdAreaKeys.Open.OPEN_PAGE,
                    AdAreaKeys.Open.FOREGROUND,
                    AdAreaKeys.Interstitial.RETURN_HOME_PAGE,
                    AdAreaKeys.Interstitial.FILE_ACCESS_CANCEL,
                )

        val missing = requiredKeys - policyKeys

        assertTrue("Missing interstitial/open area keys in ad_policy.json: $missing", missing.isEmpty())
    }

    @Test
    fun nativeAreaKeysExistInNativeAdPolicy() {
        val policyKeys = areaKeysFrom(File(rawDir, "native_ad_policy.json"))
        val placements = defaultAdPlacements(FeatureKey.entries.toSet())
        val requiredKeys =
            placements.native.values.toSet() +
                setOf(
                    AdAreaKeys.Native.HOME,
                    AdAreaKeys.Native.HOME_BOTTOM,
                    AdAreaKeys.Native.TOOLBOX_BOTTOM,
                    AdAreaKeys.Native.FINISH_PAGE,
                    AdAreaKeys.Native.FILE_ACCESS_DIALOG,
                    AdAreaKeys.Native.QUIT_APP_DIALOG,
                )

        val missing = requiredKeys - policyKeys

        assertTrue("Missing native area keys in native_ad_policy.json: $missing", missing.isEmpty())
    }

    private fun areaKeysFrom(file: File): Set<String> {
        assertTrue("Missing raw ad policy file: $file", file.exists())
        val pattern = Regex("\"areakey\"\\s*:\\s*\"([^\"]+)\"")
        return pattern.findAll(file.readText())
            .map { match -> match.groupValues[1] }
            .toSet()
    }
}
