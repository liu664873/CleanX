package com.quickcleanpro.phonecleaner.presentation.screen.files.common

import com.quickcleanpro.phonecleaner.QuickCleanApplication
import com.quickcleanpro.phonecleaner.R

internal const val FILE_DELETE_ANIMATION_MIN_MILLIS = 2000L

internal fun appString(resId: Int): String = QuickCleanApplication.instance.getString(resId)

internal fun fileScanFailedMessage(): String =
    runCatching { appString(R.string.file_scan_failed) }.getOrDefault("File scan failed.")

internal fun duplicateScanFailedMessage(): String =
    runCatching { appString(R.string.duplicate_scan_failed) }.getOrDefault("Duplicate file scan failed.")

internal fun deletionFailedMessage(): String =
    runCatching { appString(R.string.deletion_failed) }.getOrDefault("Deletion failed.")
