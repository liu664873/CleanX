package com.quickcleanpro.phonecleaner.presentation.common.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.quickcleanpro.phonecleaner.config.VariantAdUnitIds
import com.quickcleanpro.phonecleaner.config.VariantConfigs

object AdPlacements {
    const val APP_OPEN = "app_open"
    const val NAVIGATION_SCAN = "navigation_scan"
    const val NAVIGATION_TOOL = "navigation_tool"
    const val SCAN_COMPLETE = "scan_complete"
    const val CLEAN_COMPLETE = "clean_complete"
    const val RESULT_INLINE = "result_inline"
}

interface AdManager {
    val adUnitIds: VariantAdUnitIds

    fun showAppOpen(onComplete: () -> Unit = {}) {
        onComplete()
    }

    fun showInterstitial(
        placement: String,
        onComplete: () -> Unit,
    ) {
        onComplete()
    }

    fun showAd(
        placement: String,
        onComplete: () -> Unit,
    ) {
        showInterstitial(placement, onComplete)
    }

    @Composable
    fun BannerAd(
        placement: String,
        modifier: Modifier,
    ) = Unit

    @Composable
    fun NativeAdSlot(
        placement: String,
        modifier: Modifier,
    ) = Unit
}

object NoOpAdManager : AdManager {
    override val adUnitIds: VariantAdUnitIds = VariantConfigs.current.adUnitIds
}
