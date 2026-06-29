package com.quickcleanpro.phonecleaner.presentation.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

private val WhatsAppPackageNames = listOf("com.whatsapp", "com.whatsapp.w4b")

internal fun isWhatsAppInstalled(context: Context): Boolean =
    WhatsAppPackageNames.any { packageName ->
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
        }.isSuccess
    }
