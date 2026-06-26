package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.provider.Settings
import com.quickcleanpro.phonecleaner.R
import com.trustlook.sdk.data.Error as TrustlookError
import com.trustlook.sdk.data.AppInfo
import java.io.File

internal class TrustlookConfigurationException : IllegalStateException("Trustlook API key is not configured")

internal fun scanErrorMessage(context: Context, code: Int, message: String?): String {
    return if (code in NETWORK_ERROR_CODES) {
        context.getString(R.string.scan_virus_network_failed)
    } else {
        context.getString(R.string.scan_virus_failed)
    }
}

internal fun logScanError(code: Int, message: String?) {
    Log.w(
        "VirusScan",
        "Trustlook scan error code=$code message=${message.orEmpty()}",
    )
}

internal fun scanStartErrorMessage(context: Context, error: Throwable): String =
    if (error is TrustlookConfigurationException) {
        context.getString(R.string.scan_virus_authorization_missing)
    } else {
        scanErrorMessage(context, TrustlookError.UNKNOWN_ERROR, error.message)
    }

internal fun hasInstalledAppsAccess(context: Context): Boolean {
    val packageManager = context.packageManager
    val currentPackageName = context.packageName
    val installedPackages = try {
        @Suppress("DEPRECATION")
        packageManager.getInstalledPackages(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                TRUSTLOOK_INSTALLED_PACKAGES_FLAGS_API_30_PLUS
            } else {
                TRUSTLOOK_INSTALLED_PACKAGES_FLAGS_LEGACY
            },
        )
    } catch (error: Exception) {
        return false
    }

    return installedPackages.any { packageInfo ->
        packageInfo.packageName.isNotBlank() && packageInfo.packageName != currentPackageName
    }
}

internal fun hasAdbRisk(context: Context): Boolean {
    val adbEnabled = try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    } catch (globalError: Exception) {
        runCatching {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.ADB_ENABLED, 0) == 1
        }.getOrDefault(false)
    }
    if (!adbEnabled) return false

    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
    return intent.resolveActivityInfo(context.packageManager, 0) != null
}

internal fun getAppLabelAndIcon(context: Context, packageName: String): Pair<String, Drawable?> {
    if (packageName.isBlank()) return "" to null
    return try {
        val packageManager = context.packageManager
        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        }
        packageManager.getApplicationLabel(appInfo).toString() to packageManager.getApplicationIcon(packageName)
    } catch (error: Exception) {
        packageName to null
    }
}

internal fun AppInfo.toThreat(context: Context, isFile: Boolean): VirusThreat {
    val path = apkPath?.takeIf { it.isNotBlank() }
    val packageName = packageName?.takeIf { it.isNotBlank() }
    val description = threatDescription(context)
    return if (isFile || appName.isNullOrBlank()) {
        VirusThreat(
            id = "file:${path ?: description}",
            packageName = packageName,
            apkPath = path,
            title = path?.let { File(it).name } ?: virusName.orEmpty().ifBlank {
                context.getString(R.string.threat_file)
            },
            description = description,
            isFile = true,
            icon = null
        )
    } else {
        val fallback = getAppLabelAndIcon(context, packageName.orEmpty())
        VirusThreat(
            id = "app:${packageName ?: appName}",
            packageName = packageName,
            apkPath = path,
            title = appName.takeIf { it.isNotBlank() } ?: fallback.first,
            description = description,
            isFile = false,
            icon = fallback.second
        )
    }
}

internal fun Application.getProtectionIcon(): Drawable? {
    return runCatching { getDrawable(R.mipmap.ic_protection) }.getOrNull()
}

private fun AppInfo.threatDescription(context: Context): String {
    val summaryText = runCatching {
        if (summary != null && summary.size > 1) summary[1] else null
    }.getOrNull()
    return summaryText?.takeIf { it.isNotBlank() }
        ?: virusName?.takeIf { it.isNotBlank() }
        ?: context.getString(R.string.high_risk)
}

private val NETWORK_ERROR_CODES =
    setOf(
        TrustlookError.NO_NETWORK,
        TrustlookError.SOCKET_TIMEOUT_EXCEPTION,
        TrustlookError.UNSTABLE_NETWORK,
    )

private const val TRUSTLOOK_INSTALLED_PACKAGES_FLAGS_API_30_PLUS = 131072
private const val TRUSTLOOK_INSTALLED_PACKAGES_FLAGS_LEGACY = 64
