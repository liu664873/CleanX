package com.pdffox.adv

import com.google.android.gms.ads.nativead.NativeAd

/**
 * 一组 Native 广告缓存。
 *
 * @property index 当前广告位组在远程配置队列中的索引。
 * @property hAd 高价广告位加载结果。
 * @property mAd 中价广告位加载结果。
 * @property lAd 兜底/低价广告位加载结果。
 */
data class NativeAdContent(
	val index: Int,
	val hAd: NativeAd?,
	val mAd: NativeAd?,
	val lAd: NativeAd?,
)
