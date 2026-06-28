package com.pdffox.adv.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

// 允许的安装来源包名：Google Play 与 Google 反馈/系统安装来源。
private val trustedInstallerPackages = setOf(
	"com.android.vending",
	"com.google.android.feedback",
)

/** 安装来源校验工具。 */
object InstallSourceUtil {
	/** 判断当前 App 是否来自受信任商店安装。 */
	fun isTrustedStoreInstall(context: Context): Boolean {
		val packageManager = context.packageManager
		val installerPackage = try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				packageManager.getInstallSourceInfo(context.packageName).installingPackageName
			} else {
				@Suppress("DEPRECATION")
				packageManager.getInstallerPackageName(context.packageName)
			}
		} catch (_: IllegalArgumentException) {
			null
		} catch (_: PackageManager.NameNotFoundException) {
			null
		}
		return installerPackage in trustedInstallerPackages
	}
}
