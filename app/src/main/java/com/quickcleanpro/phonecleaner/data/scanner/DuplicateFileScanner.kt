package com.quickcleanpro.phonecleaner.data.scanner

import android.os.Environment
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale

class DuplicateFileScanner : JunkScanner {

    override val category: JunkCategory = JunkCategory.DUPLICATE

    private var progress: Float = 0f

    override suspend fun scan(): List<JunkFile> = withContext(Dispatchers.IO) {
        progress = 0f
        val files = scanPublicFilesFromFileSystem()
        progress = 35f

        val groups = buildDuplicateFileGroups(
            files = files,
            contentHash = { item -> computeFileHash(item.path) }
        )
        progress = 100f

        groups
            .flatMap { group -> group.sortedByDescending { it.modifiedSeconds }.drop(1) }
            .map(::toJunkFile)
    }

    override fun getProgress(): Float = progress

    private fun toJunkFile(file: DuplicateCandidate): JunkFile =
        JunkFile(
            id = file.path.hashCode().toString(),
            filePath = file.path,
            fileName = file.name,
            fileSize = file.sizeBytes,
            category = JunkCategory.DUPLICATE,
            lastModified = file.modifiedSeconds
        )

    private fun buildDuplicateFileGroups(
        files: List<DuplicateCandidate>,
        contentHash: (DuplicateCandidate) -> String?
    ): List<List<DuplicateCandidate>> {
        val uniqueFiles = files
            .filter { it.sizeBytes > 0L }
            .distinctBy { it.path }

        val hashGroups = mutableListOf<List<DuplicateCandidate>>()
        val fallbackCandidates = mutableListOf<DuplicateCandidate>()

        uniqueFiles
            .groupBy { it.sizeBytes }
            .values
            .filter { it.size > 1 }
            .forEach { sameSizeFiles ->
                val hashed = sameSizeFiles.map { file -> file to contentHash(file) }
                if (hashed.any { it.second == null }) {
                    fallbackCandidates += sameSizeFiles
                }
                hashed
                    .filter { it.second != null }
                    .groupBy { it.second.orEmpty() }
                    .values
                    .map { group -> group.map { it.first } }
                    .filter { it.size > 1 }
                    .forEach { group ->
                        hashGroups += group.sortedByDescending { it.modifiedSeconds }
                    }
            }

        val fallbackGroups = fallbackCandidates
            .groupBy { "${it.name.lowercase(Locale.US)}#${it.sizeBytes}" }
            .values
            .filter { it.size > 1 }
            .map { group -> group.sortedByDescending { it.modifiedSeconds } }

        return (hashGroups + fallbackGroups)
            .distinctBy { group -> group.joinToString("|") { it.path } }
            .sortedByDescending(::duplicateReclaimableBytes)
    }

    private fun scanPublicFilesFromFileSystem(): List<DuplicateCandidate> {
        val root = externalStorageDirectoryOrNull() ?: return emptyList()
        if (!root.isReadableDirectory()) return emptyList()

        val results = mutableListOf<DuplicateCandidate>()
        collectPublicFiles(root, results, maxDepth = 1)
        PUBLIC_SCAN_DIRECTORIES
            .map { File(root, it) }
            .filter { it.isReadableDirectory() }
            .distinctBy { runCatching { it.canonicalPath }.getOrDefault(it.absolutePath) }
            .forEach { directory -> collectPublicFiles(directory, results, maxDepth = 12) }

        return results.distinctBy { it.path }
    }

    private fun collectPublicFiles(
        directory: File,
        results: MutableList<DuplicateCandidate>,
        maxDepth: Int
    ) {
        if (maxDepth < 0 || results.size >= MAX_FILE_SYSTEM_RESULTS) return
        val children = runCatching { directory.listFiles() }.getOrNull().orEmpty()
        children.forEach { file ->
            if (results.size >= MAX_FILE_SYSTEM_RESULTS) return
            if (shouldSkipPath(file.absolutePath, file.name)) return@forEach
            if (runCatching { file.isDirectory }.getOrDefault(false)) {
                collectPublicFiles(file, results, maxDepth - 1)
            } else if (runCatching { file.isFile }.getOrDefault(false)) {
                val sizeBytes = runCatching { file.length() }.getOrDefault(0L)
                if (sizeBytes <= 0L) return@forEach
                results += DuplicateCandidate(
                    path = runCatching { file.canonicalPath }.getOrDefault(file.absolutePath),
                    name = file.name,
                    sizeBytes = sizeBytes,
                    modifiedSeconds = runCatching { file.lastModified() }.getOrDefault(0L)
                )
            }
        }
    }

    private fun computeFileHash(path: String): String? =
        runCatching {
            val file = File(path)
            if (!file.isFile) return@runCatching null
            val digest = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }.getOrNull()

    private fun externalStorageDirectoryOrNull(): File? =
        runCatching { Environment.getExternalStorageDirectory() }.getOrNull()

    private fun File.isReadableDirectory(): Boolean =
        runCatching { exists() && isDirectory && canRead() }.getOrDefault(false)

    private fun shouldSkipPath(path: String?, name: String): Boolean {
        val lowerName = name.lowercase(Locale.US)
        val lowerPath = path?.lowercase(Locale.US).orEmpty()
        if (lowerName.startsWith(".") || lowerName == "lost+found") return true
        if (lowerPath.isBlank()) return false

        val systemDirs = listOf(
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
            "/cache"
        )
        return systemDirs.any { lowerPath == it || lowerPath.startsWith("$it/") }
    }

    private fun duplicateReclaimableBytes(group: List<DuplicateCandidate>): Long =
        group.sortedByDescending { it.modifiedSeconds }.drop(1).sumOf { it.sizeBytes }

    private data class DuplicateCandidate(
        val path: String,
        val name: String,
        val sizeBytes: Long,
        val modifiedSeconds: Long
    )

    private companion object {
        const val MAX_FILE_SYSTEM_RESULTS = 10_000

        val PUBLIC_SCAN_DIRECTORIES = listOf(
            "Download", "Downloads", "Documents", "DCIM", "Pictures", "Movies", "Music",
            "WhatsApp", "Telegram", "Android/media"
        )
    }
}
