package com.quickcleanpro.phonecleaner.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.quickcleanpro.phonecleaner.domain.model.applock.AppLockApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdvancedLockAppInfoHelper(
    private val context: Context,
) {
    suspend fun getApps(): List<AppLockApp> =
        withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val currentAppPackage = context.packageName
            getLauncherApps(packageManager, currentAppPackage)
                .distinctBy { it.packageName }
                .sortedBy { it.appName.lowercase() }
        }

    @Suppress("DEPRECATION")
    private fun getLauncherApps(
        packageManager: PackageManager,
        currentAppPackage: String,
    ): List<AppLockApp> {
        val mainIntent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
        val resolveInfos =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    mainIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
                )
            } else {
                packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
            }
        return resolveInfos.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            createLockAppInfoSafely(packageManager, packageName, currentAppPackage)
        }
    }

    private fun createLockAppInfoSafely(
        packageManager: PackageManager,
        packageName: String,
        currentAppPackage: String,
    ): AppLockApp? {
        if (packageName == currentAppPackage) return null
        return runCatching {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            AppLockApp(
                packageName = packageName,
                appName = applicationInfo.loadLabel(packageManager).toString(),
                isLocked = false,
            )
        }.getOrNull()
    }
}
