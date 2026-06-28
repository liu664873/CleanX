package com.pdffox.adv.adv.policy.data

import kotlinx.serialization.Serializable

/**
 * 单条广告播放记录。
 *
 * @property areakey 广告区域 key。
 * @property adFormat 广告格式，例如 open/interstitial/banner/native。
 * @property showAdPlatform 实际展示平台，例如 admob。
 * @property timestamp 展示时间戳，单位毫秒。
 */
@Serializable
data class AdRecord(
	val areakey: String,
	val adFormat: String,
	val showAdPlatform: String,
	val timestamp: Long,
)
