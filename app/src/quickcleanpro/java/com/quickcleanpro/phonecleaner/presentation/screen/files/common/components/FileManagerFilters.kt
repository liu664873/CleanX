package com.quickcleanpro.phonecleaner.presentation.screen.files.common.components

import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileImageDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileListDisplayItem

internal fun filterMediaGridItems(
    tabTitle: String,
    items: List<FileImageDisplayItem>,
): List<FileImageDisplayItem> {
    fun FileImageDisplayItem.matchesFolder(name: String): Boolean =
        bucketName?.contains(name, ignoreCase = true) == true ||
            path?.contains("/$name/", ignoreCase = true) == true

    return when (tabTitle) {
        "DCIM" -> items.filter { it.matchesFolder("DCIM") || it.matchesFolder("Camera") }
        "Download" -> items.filter { it.matchesFolder("Download") }
        "Music" -> items.filter { it.matchesFolder("Music") || it.matchesFolder("Audio") }.ifEmpty { items }
        "Other" -> items.filterNot {
            it.matchesFolder("DCIM") ||
                it.matchesFolder("Camera") ||
                it.matchesFolder("Download")
        }
        else -> items
    }
}

internal fun filterFileManagerListItems(
    tabTitle: String,
    items: List<FileListDisplayItem>,
): List<FileListDisplayItem> {
    fun FileListDisplayItem.matchesFolder(name: String): Boolean =
        bucketName?.contains(name, ignoreCase = true) == true ||
            path?.contains("/$name/", ignoreCase = true) == true

    return when (tabTitle) {
        "Download" -> items.filter { it.matchesFolder("Download") }
        "Other" -> items.filterNot { it.matchesFolder("Download") }
        else -> items
    }
}
