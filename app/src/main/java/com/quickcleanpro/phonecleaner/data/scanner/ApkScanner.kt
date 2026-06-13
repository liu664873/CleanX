package com.quickcleanpro.phonecleaner.data.scanner

import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import java.io.File

class ApkScanner : BaseFileScanner() {
    override val category: JunkCategory = JunkCategory.APK

    override fun getRootDirectories(): List<File> = ScanDirectoryHelper.commonPublicDirectories()

    override fun isJunkFile(file: File): Boolean {
        if (!file.isFile || !file.extension.equals("apk", ignoreCase = true)) return false
        if (isSystemApk(file)) return false

        val name = file.name.lowercase()
        val isDuplicate =
            name.contains("(1)") ||
                name.contains("(2)") ||
                name.contains("copy") ||
                name.contains("duplicate") ||
                name.contains("backup") ||
                name.matches(Regex(".*\\s\\(\\d+\\).*\\.apk$", RegexOption.IGNORE_CASE)) ||
                name.matches(Regex(".*_\\d+\\.apk$", RegexOption.IGNORE_CASE))

        return file.length() >= MIN_APK_BYTES && (isDuplicate || isOldDownloadedApk(file) || isFileTooOld(file))
    }

    override fun isJunkDirectory(directory: File): Boolean {
        val name = directory.name.lowercase()
        return name.contains("apk") &&
            (name.contains("cache") || name.contains("temp") || name.contains("download"))
    }

    private fun isSystemApk(file: File): Boolean {
        val path = file.absolutePath.lowercase()
        return path.contains("/system/") ||
            path.contains("/preload/") ||
            path.contains("/system_app/") ||
            path.contains("/priv-app/")
    }

    private fun isFileTooOld(file: File): Boolean = file.lastModified() < System.currentTimeMillis() - THIRTY_DAYS_MS

    private fun isOldDownloadedApk(file: File): Boolean {
        val path = file.absolutePath.lowercase()
        if (!path.contains("/download")) return false
        return file.lastModified() < System.currentTimeMillis() - SEVEN_DAYS_MS
    }

    private companion object {
        const val MIN_APK_BYTES = 1024L
        const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000
        const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    }
}
