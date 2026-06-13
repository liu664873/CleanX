package com.quickcleanpro.phonecleaner.data.scanner

import android.content.Context
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import java.io.File

class ResidualScanner(
    context: Context,
) : BaseFileScanner() {
    override val category: JunkCategory = JunkCategory.RESIDUAL
    private val appContext = context.applicationContext

    override fun getRootDirectories(): List<File> = ScanDirectoryHelper.prioritizedDirectories(appContext)

    override fun isJunkFile(file: File): Boolean {
        if (!file.isFile || file.length() < MIN_RESIDUAL_BYTES) return false
        val name = file.name.lowercase()
        return RESIDUAL_PATTERNS.any { name.contains(it) } ||
            name.matches(Regex(".*\\.(cache|temp|tmp|bak|backup)\\d*$", RegexOption.IGNORE_CASE))
    }

    override fun isJunkDirectory(directory: File): Boolean {
        val name = directory.name.lowercase()
        if (ScanDirectoryHelper.isHiddenOrSystemPath(directory)) return false
        return RESIDUAL_PATTERNS.any { name.contains(it) }
    }

    private companion object {
        const val MIN_RESIDUAL_BYTES = 1024L
        val RESIDUAL_PATTERNS =
            setOf(
                "cache",
                "temp",
                "tmp",
                "log",
                "backup",
                "trash",
                "trashes",
                ".temp",
                ".tmp",
                ".cache",
                "tmp_",
                "temp_",
            )
    }
}
