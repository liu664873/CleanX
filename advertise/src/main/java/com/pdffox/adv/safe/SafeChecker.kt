package com.pdffox.adv.safe

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Debug
import android.os.Process
import android.util.Log
import com.pdffox.adv.BuildConfig
import com.pdffox.adv.Config
import com.pdffox.adv.SafeConfig
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import kotlin.system.exitProcess

/**
 * 运行时安全校验器。
 *
 * 按配置检查包名、签名、debuggable 状态和调试器连接状态；失败时可结束页面并杀进程。
 */
object SafeChecker {

	private const val TAG = "SafeChecker"

	/** 执行安全检查，失败时按配置结束当前进程。 */
	fun checkAndShutDown(context: Context) {
		if (check(context)) {
			return
		}
		if (com.pdffox.adv.Config.isTest) {
			Log.e(TAG, "checkAndShutDown: application is not in a safe state")
		}
		(context as? Activity)?.finishAffinity()
		if (Config.sdkConfig.safe.killProcessOnFailure) {
			Process.killProcess(Process.myPid())
			exitProcess(0)
		}
	}

	/** 执行所有已启用的安全检查，全部通过返回 true。 */
	fun check(context: Context): Boolean {
		val safeConfig = Config.sdkConfig.safe
		if (!safeConfig.enabled) {
			return true
		}
		if (!checkPackageName(context, safeConfig)) {
			Log.e(TAG, "check: package name mismatch")
			return false
		}
		if (!checkSignature(context, safeConfig)) {
			Log.e(TAG, "check: signature mismatch")
			return false
		}
		if (safeConfig.rejectDebuggableBuilds && isDebuggableBuild(context)) {
			Log.e(TAG, "check: debuggable build rejected")
			return false
		}
		if (safeConfig.rejectDebuggerAttached && isDebuggerAttached()) {
			Log.e(TAG, "check: debugger attached")
			return false
		}
		return true
	}

	/** 校验当前运行包名是否等于配置的期望包名。 */
	private fun checkPackageName(context: Context, safeConfig: SafeConfig): Boolean {
		val expectedPackageName = safeConfig.expectedPackageName
			?: Config.packageName.takeIf { it.isNotBlank() }
			?: context.packageName
		return expectedPackageName == context.packageName
	}

	/** 校验当前 APK 签名 SHA-256 是否在允许列表中。 */
	private fun checkSignature(context: Context, safeConfig: SafeConfig): Boolean {
		val expectedSignatures = safeConfig.expectedSignatures
			.map { normalizeSignature(it) }
			.filter { it.isNotBlank() }
			.toSet()
		if (expectedSignatures.isEmpty()) {
			return true
		}
		return try {
			val packageInfo = context.packageManager.getPackageInfo(
				context.packageName,
				PackageManager.GET_SIGNING_CERTIFICATES,
			)
			val signatures = packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
			signatures.any { signature ->
				val digest = MessageDigest.getInstance("SHA-256").digest(signature.toByteArray())
				val hexString = digest.joinToString("") { "%02X".format(it) }
				expectedSignatures.contains(hexString)
			}
		} catch (throwable: Exception) {
			Log.e(TAG, "checkSignature: failed to read app signature", throwable)
			false
		}
	}

	/** 判断当前包是否为 debuggable 构建。 */
	private fun isDebuggableBuild(context: Context): Boolean {
		return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
	}

	/** 判断当前进程是否已连接调试器，包含 Android Debug API 和 /proc TracerPid 双重检测。 */
	private fun isDebuggerAttached(): Boolean {
		if (Debug.isDebuggerConnected()) {
			return true
		}
		return try {
			val tracerPid = File("/proc/self/status").useLines { lines ->
				lines.firstOrNull { it.startsWith("TracerPid:") }
					?.substringAfter(":")
					?.trim()
					?.toIntOrNull()
			}
			tracerPid != null && tracerPid > 0
		} catch (throwable: Exception) {
			false
		}
	}

	/** 规范化签名字符串，去掉分隔符并统一为大写。 */
	private fun normalizeSignature(value: String): String {
		return value
			.replace(":", "")
			.replace(" ", "")
			.uppercase(Locale.US)
	}
}
