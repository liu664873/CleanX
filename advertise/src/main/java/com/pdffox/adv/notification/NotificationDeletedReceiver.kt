package com.pdffox.adv.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pdffox.adv.Config

/** 通知删除广播接收器，用户移除持久通知后尝试重新拉起服务。 */
class NotificationDeletedReceiver : BroadcastReceiver() {
	companion object {
		const val TAG = "NotificationDeletedRece"
	}

	/** 收到删除事件后按配置启动 [CommonService]。 */
	override fun onReceive(context: Context?, intent: Intent?) {
		if (!Config.sdkConfig.push.enabled || !Config.sdkConfig.push.notificationDeletedReceiverEnabled) {
			return
		}
		Log.e(TAG, "onReceive: ", )
		context?.let(CommonService::start)
	}
}
