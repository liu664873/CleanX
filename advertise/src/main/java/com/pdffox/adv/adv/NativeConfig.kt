package com.pdffox.adv.adv

/**
 * Native 广告和引导页相关时间配置。
 *
 * Remote Config 更新后会写入这里，供宿主通过 AdvertiseSdk 读取。
 */
object NativeConfig {

	// Native 广告自动刷新间隔；本地单位毫秒，Remote Config 下发 native_refresh_time 时按秒转换。
	var native_refresh_time: Long = 30 * 1000

	// 引导页自动切换时间，单位毫秒。
	var guide_page_swap_time = 2000L

}
