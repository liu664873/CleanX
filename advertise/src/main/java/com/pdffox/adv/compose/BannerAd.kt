package com.pdffox.adv.compose

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView

/**
 * Compose 中展示已有 Banner AdView 的包装组件。
 *
 * AdView 由广告库外部创建，这里只负责嵌入 AndroidView 并在离开组合时销毁。
 */
@Composable
fun BannerAd(adView: ViewGroup, modifier: Modifier = Modifier) {
	if (LocalInspectionMode.current) {
		return
	}

	AndroidView(
		modifier = modifier,
		factory = { adView },
	)

	DisposableEffect(adView) {
		onDispose {
			// Banner AdView 离开 Compose 树后需要主动 destroy，避免继续持有广告资源。
			(adView as? AdView)?.destroy()
		}
	}
}
