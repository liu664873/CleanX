package com.pdffox.adv.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pdffox.adv.Config

/** 开机广播接收器，用于设备启动后延迟拉起持久通知服务。 */
class BootReceiver : BroadcastReceiver() {
	companion object {
		private const val TAG = "BootReceiver"
	}

	/** 收到 BOOT_COMPLETED 后按 Push 配置决定是否启动 [CommonService]。 */
	override fun onReceive(context: Context, intent: Intent) {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.bootReceiverEnabled) {
			return
		}
		if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
			Log.e(TAG, "onReceive: 启动完成")
			android.os.Handler(context.mainLooper).postDelayed({
				try {
					CommonService.start(context)
				} catch (e: Exception) {
					Log.e(TAG, "onReceive: 启动服务失败", e)
				}
			}, 2000)
		}
	}

}
