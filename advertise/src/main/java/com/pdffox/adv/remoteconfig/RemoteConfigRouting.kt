package com.pdffox.adv.remoteconfig

import android.os.Build
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.pdffox.adv.Ads
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.adv.AdConfig
import com.pdffox.adv.adv.policy.AdPolicyManager
import com.pdffox.adv.adv.policy.data.AdMapping
import com.pdffox.adv.adv.policy.data.Config as MappingConfig
import com.pdffox.adv.adv.policy.data.parseAdMapping
import com.pdffox.adv.notification.NotificationManager
import com.pdffox.adv.util.PreferenceUtil
import java.util.Locale

/** 远程配置路由选择所需的运行时上下文。 */
internal data class RoutingContext(
	val useNatureConfig: Boolean,
	val country: String,
	val brand: String
)

/** 从 ad_mapping 中选出的配置标签。 */
internal data class RoutingSelection(
	val topic: String?,
	val adTag: String?,
	val notificationTag: String?,
	val preloadTag: String?
)

/** Remote Config 中默认的广告、通知和预加载配置 JSON。 */
internal data class RoutingDefaults(
	val adPolicy: String,
	val notificationConfig: String,
	val preloadConfig: String
)

/** 最终解析后的配置 JSON 和来源标签。 */
internal data class ResolvedRouting(
	val topic: String?,
	val adPolicy: String,
	val notificationConfig: String,
	val preloadConfig: String,
	val adTag: String?,
	val notificationTag: String?,
	val preloadTag: String?
) {
	val cacheKey: String = buildString {
		append(topic.orEmpty())
		append('|')
		append(adPolicy.hashCode())
		append('|')
		append(notificationConfig.hashCode())
		append('|')
		append(preloadConfig.hashCode())
	}
}

/**
 * 判断是否应该走自然量/Google IP/paid_0 特殊路由。
 *
 * 自然量必须等待 Singular 结果；Google IP 和 paid_0 则依赖各自检查结果。
 */
internal fun shouldUseNatureRouting(
	singularHasResult: Boolean = Config.singularHasResult,
	openReview: Boolean = Config.openReview,
	isNature: Boolean = Config.isNature,
	isGoogleIP: Boolean = Config.isGoogleIP,
	ipCheckHasResult: Boolean = Config.ipCheckHasResult,
	paid0HasResult: Boolean = Config.paid0HasResult,
	paid0: Boolean = Config.paid_0
): Boolean {
	if (com.pdffox.adv.Config.isTest) {
		Log.e("TAG", "shouldUseNatureRouting: \n openReview = $openReview" +
				"\n isNature = $isNature " +
				"\n singularHasResult = $singularHasResult" +
				"\n isGoogleIP = $isGoogleIP" +
				"\n ipCheckHasResult = $ipCheckHasResult" +
				"\n paid0HasResult = $paid0HasResult" +
				"\n paid0 = $paid0 " )

		return false
	}
	return (openReview && isNature && singularHasResult) ||
		(isGoogleIP && ipCheckHasResult) ||
		(paid0HasResult && paid0)
}

/** 构造路由选择上下文，默认读取系统国家码和设备品牌。 */
internal fun buildRoutingContext(
	useNatureConfig: Boolean = shouldUseNatureRouting(),
	country: String = Locale.getDefault().country,
	brand: String = Build.BRAND
): RoutingContext {
	return RoutingContext(
		useNatureConfig = useNatureConfig,
		country = country,
		brand = brand
	)
}

/** 根据 ad_mapping 和上下文选择目标配置标签。 */
internal fun selectRouting(
	adMapping: AdMapping?,
	context: RoutingContext,
	allowTargetedSelection: Boolean = true
): RoutingSelection? {
	if (!allowTargetedSelection || adMapping == null) {
		return null
	}
	if (com.pdffox.adv.Config.isTest) {
		Log.e("TAG", "selectRouting: ${context.useNatureConfig}" )
	}
	val selectedConfig = if (context.useNatureConfig) {
		adMapping.nature_config
	} else {
		adMapping.configs.firstOrNull { item ->
			item.countrys.any { it.equals(context.country, ignoreCase = true) } &&
				item.brands.any { it.equals(context.brand, ignoreCase = true) }
		}?.config
	}
	return selectedConfig?.toRoutingSelection()
}

/** 将标签解析为实际 JSON；标签为空或查不到值时回退默认配置。 */
internal fun resolveRouting(
	selection: RoutingSelection?,
	defaults: RoutingDefaults,
	lookup: (String) -> String
): ResolvedRouting {
	fun resolveValue(tag: String?, fallback: String): String {
		if (tag.isNullOrBlank()) {
			return fallback
		}
		val value = lookup(tag)
		return if (value.isNotBlank()) value else fallback
	}

	return ResolvedRouting(
		topic = selection?.topic?.takeIf { it.isNotBlank() },
		adPolicy = resolveValue(selection?.adTag, defaults.adPolicy),
		notificationConfig = resolveValue(selection?.notificationTag, defaults.notificationConfig),
		preloadConfig = resolveValue(selection?.preloadTag, defaults.preloadConfig),
		adTag = selection?.adTag,
		notificationTag = selection?.notificationTag,
		preloadTag = selection?.preloadTag
	)
}

/** 将 ad_mapping 中的一组配置标签转换为路由选择结果。 */
private fun MappingConfig.toRoutingSelection(): RoutingSelection {
	return RoutingSelection(
		topic = fcm_topic,
		adTag = ad,
		notificationTag = notification,
		preloadTag = preload
	)
}

/**
 * Remote Config 路由应用器。
 *
 * 将 ad_mapping 的选择结果应用到 FCM topic、广告预加载策略、通知策略和广告展示策略。
 */
object RemoteConfigRouting {
	private const val TAG = "RemoteConfigRouting"

	// 避免同一组解析结果被 Singular/IP/paid_0 多个异步来源重复应用。
	@Volatile
	private var lastAppliedKey: String? = null

	/** 解析并应用当前 Remote Config 路由。 */
	@Synchronized
	fun apply(
		remoteConfig: FirebaseRemoteConfig,
		adMapping: String,
		source: String,
		allowTargetedSelection: Boolean = true
	) {
		val selection = selectRouting(
			adMapping = parseAdMapping(adMapping),
			context = buildRoutingContext(),
			allowTargetedSelection = allowTargetedSelection
		)
		val resolved = resolveRouting(
			selection = selection,
			defaults = RoutingDefaults(
				adPolicy = remoteConfig.getString("ad_policy"),
				notificationConfig = remoteConfig.getString("notification_config"),
				preloadConfig = remoteConfig.getString("adload_config")
			),
			lookup = remoteConfig::getString
		)
		if (resolved.cacheKey == lastAppliedKey) {
			if (com.pdffox.adv.Config.isTest) {
				Log.e(TAG, "apply[$source]: skipped duplicate routing")
			}
			return
		}
		lastAppliedKey = resolved.cacheKey
		if (com.pdffox.adv.Config.isTest) {
			PreferenceUtil.commitString("routing_source", source)
			PreferenceUtil.commitString("adTag", resolved.adTag.orEmpty())
			PreferenceUtil.commitString("notificationTag", resolved.notificationTag.orEmpty())
			PreferenceUtil.commitString("preloadTag", resolved.preloadTag.orEmpty())
			Log.e(
				TAG,
				"apply[$source]: topic=${resolved.topic}, adTag=${resolved.adTag}, " +
					"notificationTag=${resolved.notificationTag}, preloadTag=${resolved.preloadTag}"
			)
		}
		// topic 和策略应用顺序保持一致：先切 FCM topic，再更新本地广告/通知策略。
		resolved.topic?.let(Ads::changeTopic)
		AdConfig.updateConfigFromJson(resolved.preloadConfig)
		if (Config.sdkConfig.notifications.enabled) {
			NotificationManager.updateNotificationConfig(resolved.notificationConfig)
		}
		AdPolicyManager.setPolicyFromJson(resolved.adPolicy)
	}
}
