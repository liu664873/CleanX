package com.pdffox.adv.remoteconfig

import android.content.Context
import com.pdffox.adv.Config
import com.pdffox.adv.R

/** Local fallback values for Firebase Remote Config. */
internal object RemoteConfigDefaults {
	fun values(context: Context): Map<String, Any> {
		val adMobConfig = Config.sdkConfig.adMob
		return mapOf(
			"ABTestName" to "",
			"update_version" to 0L,
			"OpenAdmobMediation" to true,
			"Admob_Banner" to adMobConfig.bannerId,
			"Admob_Interset" to adMobConfig.interstitialId,
			"Admob_Native" to adMobConfig.nativeId,
			"Admob_Open" to adMobConfig.openId,
			"openReview" to false,
			"guide_page_swap_time" to 2000L,
			"native_refresh_time" to 30L,
			"native_ad_ids" to adMobConfig.nativeIdsJson,
			"native_ad_policy" to readRaw(context, R.raw.remote_config_native_ad_policy),
			"log_time" to 48L,
			"ad_mapping" to readRaw(context, R.raw.remote_config_ad_mapping),
			"adload_config" to readRaw(context, R.raw.remote_config_adload_config),
			"adload_config_audit" to readRaw(context, R.raw.remote_config_adload_config_audit),
			"ad_policy" to readRaw(context, R.raw.remote_config_ad_policy),
			"ad_policy_audit" to readRaw(context, R.raw.remote_config_ad_policy_audit),
			"notification_config" to readRaw(context, R.raw.remote_config_notification_config),
			"notification_config_audit" to readRaw(context, R.raw.remote_config_notification_config_audit),
			"notification_content" to readRaw(context, R.raw.remote_config_notification_content),
			"Contextualized_Push" to readRaw(context, R.raw.remote_config_contextualized_push)
		)
	}

	private fun readRaw(context: Context, resId: Int): String {
		return context.resources.openRawResource(resId).bufferedReader(Charsets.UTF_8).use { it.readText() }
	}
}
