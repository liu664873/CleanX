package com.quickcleanpro.phonecleaner.data.scanner

import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import java.io.File

class TempFileScanner : BaseFileScanner() {
    override val category: JunkCategory = JunkCategory.TEMP_FILE

    override fun getRootDirectories(): List<File> = ScanDirectoryHelper.commonPublicDirectories() + File("/data/local/tmp")

    override fun isJunkFile(file: File): Boolean {
//        if (!file.isFile || file.length() < MIN_TEMP_BYTES) return false
        if (!file.isFile) return false
        val name = file.name.lowercase()
        val extension = file.extension.lowercase()
        return extension in TEMP_EXTENSIONS ||
            name.contains(".tmp") ||
            name.contains(".temp") ||
            name.startsWith("~") ||
            name.startsWith("temp_") ||
            name.startsWith("tmp_")
    }

    override fun isJunkDirectory(directory: File): Boolean {
        val name = directory.name.lowercase()
        return name == "temp" ||
            name == "tmp" ||
            name.startsWith("temp_") ||
            name.startsWith("tmp_")
    }

    private companion object {
        const val MIN_TEMP_BYTES = 1024L
        val TEMP_EXTENSIONS = setOf("tmp", "temp", "log", "cache", "bak", "backup")
    }
}
