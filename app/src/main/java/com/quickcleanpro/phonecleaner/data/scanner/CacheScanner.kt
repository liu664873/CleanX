package com.quickcleanpro.phonecleaner.data.scanner

import android.content.Context
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import java.io.File

class CacheScanner(
    context: Context,
) : BaseFileScanner() {
    override val category: JunkCategory = JunkCategory.CACHE
    private val appContext = context.applicationContext

    override fun getRootDirectories(): List<File> = ScanDirectoryHelper.prioritizedDirectories(appContext)

    override fun isJunkFile(file: File): Boolean {
        if (!file.isFile) return false
        val parent =
            file.parentFile
                ?.name
                ?.lowercase()
                .orEmpty()
        val name = file.name.lowercase()
        return parent.contains("cache") ||
            parent.contains("temp") ||
            parent.contains("tmp") ||
            parent.contains("ad") ||
            name.endsWith(".cache") ||
            name.endsWith(".cache.db") ||
            name.contains("adcache")
    }

    override fun isJunkDirectory(directory: File): Boolean {
        val name = directory.name.lowercase()
        return name == "cache" ||
            name == "code_cache" ||
            name == "temp" ||
            name == "tmp" ||
            name.contains("adcache") ||
            name.contains("ads_cache")
    }
}
