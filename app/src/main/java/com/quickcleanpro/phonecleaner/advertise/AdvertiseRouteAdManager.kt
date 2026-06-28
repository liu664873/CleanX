package com.quickcleanpro.phonecleaner.advertise

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pdffox.adv.compose.BannerAd as AdvBannerAd
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.config.VariantAdUnitIds
import com.quickcleanpro.phonecleaner.config.VariantConfigs
import com.quickcleanpro.phonecleaner.presentation.common.route.AdManager
import com.quickcleanpro.phonecleaner.presentation.common.route.AdPlacements

class AdvertiseRouteAdManager(
    private val activity: Activity?,
    private val mediator: AdvertiseMediator = AdvertisePageMediator,
) : AdManager {
    private val profile = VariantConfigs.current
    override val adUnitIds: VariantAdUnitIds = profile.adUnitIds

    override fun showAppOpen(onComplete: () -> Unit) {
        AdvertisePageMediator.showSplashConsentThenOpenAd(activity, onComplete)
    }

    override fun showInterstitial(
        placement: String,
        onComplete: () -> Unit,
    ) {
        when (placement) {
            AdPlacements.APP_OPEN -> showAppOpen(onComplete)
            AdPlacements.RETURN_HOME -> mediator.showReturnHomeAd(activity, onComplete)
            AdPlacements.JUNK_CLEAN_FINISH -> mediator.showFinishAd(activity, FeatureKey.JUNK_CLEAN, onComplete)
            AdPlacements.FILE_MANAGE_FINISH -> showFileManageFinishAd(onComplete)
            AdPlacements.BATTERY_INFO_FINISH -> mediator.showFinishAd(activity, FeatureKey.BATTERY_INFO, onComplete)
            AdPlacements.WHATSAPP_CLEAN_FINISH -> mediator.showFinishAd(activity, FeatureKey.WHATSAPP_CLEANER, onComplete)
            else -> AdvertisePageMediator.showInterstitial(activity, placement, onComplete)
        }
    }

    @Composable
    override fun BannerAd(
        placement: String,
        modifier: Modifier,
    ) {
        val hostActivity = activity ?: return
        val areaKey = bannerAreaKey(placement) ?: return
        val banner = remember(hostActivity, areaKey) {
            AdvertisePageMediator.getBannerAd(hostActivity, areaKey)
        } ?: return
        AdvBannerAd(adView = banner, modifier = modifier)
    }

    @Composable
    override fun NativeAdSlot(
        placement: String,
        modifier: Modifier,
    ) = Unit

    private fun bannerAreaKey(placement: String): String? =
        when (placement) {
            AdPlacements.RESULT_INLINE,
            AdAreaKeys.Banner.CLEAN_PAGE_BOTTOM
            -> AdAreaKeys.Banner.CLEAN_PAGE_BOTTOM
            AdAreaKeys.Banner.HOME_BOTTOM -> AdAreaKeys.Banner.HOME_BOTTOM
            AdAreaKeys.Banner.TOOLBOX_BOTTOM -> AdAreaKeys.Banner.TOOLBOX_BOTTOM
            AdAreaKeys.Banner.FILE_PAGE_BOTTOM -> AdAreaKeys.Banner.FILE_PAGE_BOTTOM
            else -> placement.takeIf { it.endsWith("Adv") }
        }

    private fun showFileManageFinishAd(onComplete: () -> Unit) {
        AdvertisePageMediator.showInterstitial(
            activity,
            AdAreaKeys.Interstitial.FILE_MANAGE_FINISH.takeIf {
                profile.adProfile.placements.featureCompletion.containsValue(it)
            },
            onComplete,
        )
    }
}
