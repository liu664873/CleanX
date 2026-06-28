package com.pdffox.adv.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.nativead.NativeAd
import com.pdffox.adv.Ads
import com.pdffox.adv.NativeAdContent
import kotlinx.coroutines.delay

/**
 * Compose Native 广告状态持有者。
 *
 * 负责从广告池获取 NativeAd、在状态变更时销毁旧广告，并在 Composable 销毁时释放广告资源。
 */
private class NativeAdState(
	private val context: Context,
	private val areaKey: String,
) {
	private val nativeAdState = mutableStateOf<NativeAd?>(null)
	private var nativeAdContent: NativeAdContent? = null
	private var disposed = false

	val nativeAd: State<NativeAd?>
		get() = nativeAdState

	/** 主动刷新 Native 广告；如果当前池为空，会触发异步加载并等待回调再取池。 */
	fun refresh() {
		if (disposed) {
			return
		}
		val adGroup = Ads.getNativeAd(context, areaKey) {
			refreshFromPool()
		}
		if (adGroup != null) {
			setAdGroup(adGroup)
		}
	}

	/** 标记状态已释放，并销毁当前持有的 NativeAd。 */
	fun dispose() {
		disposed = true
		destroyCurrentAd()
	}

	/** 广告池异步填充完成后的二次取池逻辑。 */
	private fun refreshFromPool() {
		if (disposed) {
			return
		}
		val adGroup = Ads.getNativeAd(context, areaKey) {}
		if (adGroup != null) {
			setAdGroup(adGroup)
		}
	}

	/** 替换当前 Native 广告组，并优先展示高价广告位。 */
	private fun setAdGroup(adGroup: NativeAdContent) {
		destroyCurrentAd()
		nativeAdContent = adGroup
		nativeAdState.value = adGroup.hAd ?: adGroup.mAd ?: adGroup.lAd
	}

	/** 销毁当前广告组中所有可能已加载的 NativeAd，避免 Compose 重组/离开页面后泄漏。 */
	private fun destroyCurrentAd() {
		nativeAdState.value?.destroy()
		nativeAdContent?.hAd?.destroy()
		nativeAdContent?.mAd?.destroy()
		nativeAdContent?.lAd?.destroy()
		nativeAdState.value = null
		nativeAdContent = null
	}
}

/**
 * 在 Compose 中记住并自动刷新 Native 广告。
 *
 * @param areaKey 广告区域 key。
 * @param refreshImmediately 是否首次进入时立即刷新。
 * @param shouldRefreshImmediately 首次刷新前的业务条件。
 * @param shouldAutoRefresh 后续定时刷新前的业务条件。
 */
@Composable
fun rememberNativeAd(
	areaKey: String,
	refreshImmediately: Boolean = true,
	shouldRefreshImmediately: () -> Boolean = { true },
	shouldAutoRefresh: () -> Boolean = shouldRefreshImmediately,
): State<NativeAd?> {
	val context = LocalContext.current
	val nativeAdState = remember(context, areaKey) {
		NativeAdState(context, areaKey)
	}
	val currentShouldRefreshImmediately = rememberUpdatedState(shouldRefreshImmediately)
	val currentShouldAutoRefresh = rememberUpdatedState(shouldAutoRefresh)

	LaunchedEffect(nativeAdState, refreshImmediately) {
		// 首次刷新和后续自动刷新共用 NativeAdState，避免每次重组重复创建广告对象。
		if (refreshImmediately && currentShouldRefreshImmediately.value()) {
			nativeAdState.refresh()
		}
		while (true) {
			delay(Ads.nativeRefreshTime)
			if (currentShouldAutoRefresh.value()) {
				nativeAdState.refresh()
			}
		}
	}

	DisposableEffect(nativeAdState) {
		onDispose {
			// Composable 离开组合时必须销毁广告对象，符合 AdMob NativeAd 生命周期要求。
			nativeAdState.dispose()
		}
	}

	return nativeAdState.nativeAd
}
