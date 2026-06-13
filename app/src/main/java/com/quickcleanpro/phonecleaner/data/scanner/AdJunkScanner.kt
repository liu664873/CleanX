package com.quickcleanpro.phonecleaner.data.scanner

import android.content.Context
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import java.io.File

class AdJunkScanner(
    context: Context,
) : BaseFileScanner() {
    override val category: JunkCategory = JunkCategory.CACHE
    private val appContext = context.applicationContext

    override fun getRootDirectories(): List<File> = ScanDirectoryHelper.prioritizedDirectories(appContext)

    override fun isJunkFile(file: File): Boolean {
        if (!file.isFile || file.length() < MIN_AD_BYTES) return false
        return isAdName(file.name) ||
            AD_FILE_PATTERNS.any { it.matches(file.name) } ||
            file.absolutePath.lowercase().let { path ->
                path.contains("/adcache/") ||
                    path.contains("/advertising/") ||
                    path.contains("/ads/") ||
                    path.contains("/ad_data/") ||
                    path.contains("/ad_images/") ||
                    path.contains("/ad_videos/")
            }
    }

    override fun isJunkDirectory(directory: File): Boolean {
        val name = directory.name.lowercase()
        val path = directory.absolutePath.lowercase()
        return AD_PATTERNS.any { name.contains(it) } ||
            AD_DIR_PATTERNS.any { name.contains(it) } ||
            name.startsWith(".ad_") ||
            name.startsWith("ad_") ||
            name.startsWith(".ads_") ||
            name.startsWith("ads_") ||
            path.contains("/adcache/") ||
            path.contains("/advertising/") ||
            path.contains("/ads/") ||
            path.contains("/ad_data/")
    }

    private fun isAdName(name: String): Boolean {
        val lowerName = name.lowercase()
        return AD_PATTERNS.any { lowerName.contains(it) } ||
            lowerName.startsWith("ad_") ||
            lowerName.endsWith("_ad") ||
            lowerName.contains("_ad_") ||
            lowerName.contains(".ad.") ||
            fileExtensionIsAd(lowerName)
    }

    private fun fileExtensionIsAd(lowerName: String): Boolean {
        val extension = lowerName.substringAfterLast('.', missingDelimiterValue = "")
        return extension in setOf("ad", "ads", "adcache", "advert", "advertisement")
    }

    private companion object {
        const val MIN_AD_BYTES = 1024L
        val AD_PATTERNS =
            setOf(
                "ad",
                "ads",
                "advert",
                "advertisement",
                "adcache",
                "admob",
                "mopub",
                "facebookad",
                "unityad",
                "advertising",
                "adsense",
                "gad",
                "iad",
                "adcolony",
                "vungle",
                "chartboost",
                "ironsource",
                "applovin",
                "tapjoy",
                "inmobi",
            )
        val AD_DIR_PATTERNS =
            setOf(
                "adcache",
                "adcaches",
                "advertising",
                "ad_data",
                "ads_data",
                "ad_images",
                "ad_videos",
                "ad_logs",
                "ads_cache",
                "advertising_cache",
                "ad_sdk",
                "ads_sdk",
            )
        val AD_FILE_PATTERNS =
            listOf(
                Regex(".*[._\\-]ad[._\\-].*", RegexOption.IGNORE_CASE),
                Regex(".*ads?\\d*\\.(cache|data|tmp|log|db)$", RegexOption.IGNORE_CASE),
                Regex("^ad_.*", RegexOption.IGNORE_CASE),
                Regex(".*_ad_.*", RegexOption.IGNORE_CASE),
                Regex(".*\\.ad\\..*", RegexOption.IGNORE_CASE),
                Regex("ads?_.*\\.(jpg|png|gif|mp4|webp)$", RegexOption.IGNORE_CASE),
            )
    }
}
