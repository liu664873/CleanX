package com.quickcleanpro.phonecleaner.data.scanner

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object ScanDirectoryHelper {
    private val cache = ConcurrentHashMap<String, List<File>>()
    private val priorityPatterns =
        mapOf(
            "cache" to 1,
            "temp" to 1,
            "tmp" to 1,
            Environment.DIRECTORY_DOWNLOADS.lowercase() to 2,
            "apk" to 2,
            Environment.DIRECTORY_DOCUMENTS.lowercase() to 3,
            Environment.DIRECTORY_PICTURES.lowercase() to 3,
            Environment.DIRECTORY_DCIM.lowercase() to 3,
            Environment.DIRECTORY_MOVIES.lowercase() to 3,
            Environment.DIRECTORY_MUSIC.lowercase() to 3,
        )

    fun prioritizedDirectories(context: Context): List<File> {
        val key = "dirs_${Build.VERSION.SDK_INT}_${hasAllFilesAccess()}"
        return cache.getOrPut(key) {
            buildSet {
                runCatching { context.filesDir }.getOrNull()?.let(::add)
                runCatching { context.cacheDir }.getOrNull()?.let(::add)
                runCatching { context.externalCacheDir }.getOrNull()?.let(::add)
                runCatching { context.getExternalFilesDir(null) }.getOrNull()?.let(::add)
                runCatching { context.externalMediaDirs.toList() }.getOrDefault(emptyList()).forEach { add(it) }

                externalStorageDirectoryOrNull()?.let { external ->
                    listOf(
                        Environment.DIRECTORY_DOWNLOADS,
                        Environment.DIRECTORY_DOCUMENTS,
                        Environment.DIRECTORY_PICTURES,
                        Environment.DIRECTORY_DCIM,
                        Environment.DIRECTORY_MOVIES,
                        Environment.DIRECTORY_MUSIC,
                        "APK",
                        "apk",
                        "Temp",
                        "temp",
                        "Tmp",
                        "tmp",
                    ).forEach { add(File(external, it)) }

                    if (hasAllFilesAccess()) {
                        add(external)
                        add(File(external, "Android/data"))
                    }
                }
            }.filter { file ->
                runCatching {
                    file.exists() && file.isDirectory && file.canRead() && !isHiddenOrSystemPath(file)
                }.getOrDefault(false)
            }.distinctBy { it.absolutePath }
                .sortedBy { scanPriority(it) }
        }
    }

    fun commonPublicDirectories(): List<File> {
        val external = externalStorageDirectoryOrNull() ?: return emptyList()
        return listOf(
            File(external, Environment.DIRECTORY_DOWNLOADS),
            File(external, Environment.DIRECTORY_DOCUMENTS),
            File(external, Environment.DIRECTORY_PICTURES),
            File(external, Environment.DIRECTORY_DCIM),
            File(external, Environment.DIRECTORY_MOVIES),
            File(external, Environment.DIRECTORY_MUSIC),
            File(external, "Temp"),
            File(external, "temp"),
            File(external, "tmp"),
        ).filter { file ->
            runCatching { file.exists() && file.isDirectory && file.canRead() }.getOrDefault(false)
        }
    }

    fun isHiddenOrSystemPath(file: File): Boolean {
        val name = file.name.lowercase()
        val path = file.absolutePath.lowercase()
        if (name.startsWith(".") || name == "lost+found") return true

        val systemDirs =
            listOf(
                "/system",
                "/lost+found",
                "/preload",
                "/vendor",
                "/mnt",
                "/proc",
                "/sys",
                "/acct",
                "/dev",
                "/config",
                "/oem",
                "/firmware",
            )
        return systemDirs.any { path == it || path.startsWith("$it/") }
    }

    fun clearCache() {
        cache.clear()
    }

    private fun scanPriority(file: File): Int {
        val path = file.absolutePath.lowercase()
        val name = file.name.lowercase()
        return priorityPatterns.entries
            .firstOrNull { (pattern, _) ->
                path.contains(pattern) || name.contains(pattern)
            }?.value ?: 4
    }

    private fun hasAllFilesAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)

    private fun externalStorageDirectoryOrNull(): File? = runCatching { Environment.getExternalStorageDirectory() }.getOrNull()
}
