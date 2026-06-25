package com.quickcleanpro.phonecleaner.data.scanner

import android.util.Log
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Template scanner for file-system based junk categories.
 */
abstract class BaseFileScanner : JunkScanner {
    @Volatile
    private var progress: Float = 0f

    protected abstract fun getRootDirectories(): List<File>

    protected abstract fun isJunkFile(file: File): Boolean

    protected open fun isJunkDirectory(directory: File): Boolean = false

    override suspend fun scan(): List<JunkFile> =
        withContext(Dispatchers.IO) {
            progress = 0f
            val junkFiles = mutableListOf<JunkFile>()

            val validDirs =
                runCatchingLogged("getRootDirectories(${this@BaseFileScanner.javaClass.simpleName})") { getRootDirectories() }
                    .getOrDefault(emptyList())
                    .filter { file -> runCatchingLogged("exists+dir:${file.absolutePath}") { file.exists() && file.isDirectory }.getOrDefault(false) }
            if (validDirs.isEmpty()) {
                progress = 100f
                return@withContext junkFiles
            }

            var totalFiles = 0
            for (dir in validDirs) {
                totalFiles += countFiles(dir)
            }
            if (totalFiles == 0) {
                progress = 100f
                return@withContext junkFiles
            }

            var processedFiles = 0
            for (dir in validDirs) {
                collectJunkFiles(dir, junkFiles) {
                    processedFiles++
                    progress = (processedFiles * 100f / totalFiles).coerceIn(0f, 99f)
                    coroutineContext.ensureActive()
                }
            }

            progress = 100f
            junkFiles
        }

    override fun getProgress(): Float = progress

    private fun countFiles(directory: File): Int {
        var count = 0
        safeListFiles(directory).forEach { file ->
            val isDirectory = runCatching { file.isDirectory }.getOrDefault(false)
            val isJunkDirectory = isDirectory && runCatching { isJunkDirectory(file) }.getOrDefault(false)
            if (isDirectory && !isJunkDirectory) {
                count += countFiles(file)
            } else {
                count++
            }
        }
        return count
    }

    private fun collectJunkFiles(
        directory: File,
        result: MutableList<JunkFile>,
        onFileProcessed: () -> Unit,
    ) {
        val files = safeListFiles(directory)
        for (file in files) {
            val isDirectory = runCatching { file.isDirectory }.getOrDefault(false)
            if (isDirectory) {
                val isJunkDirectory = runCatching { isJunkDirectory(file) }.getOrDefault(false)
                if (isJunkDirectory) {
                    buildJunkFile(file)?.let(result::add)
                } else {
                    collectJunkFiles(file, result, onFileProcessed)
                }
            } else if (runCatching { isJunkFile(file) }.getOrDefault(false)) {
                buildJunkFile(file)?.let(result::add)
            }
            onFileProcessed()
        }
    }

    protected fun buildJunkFile(file: File): JunkFile? =
        runCatching {
            val lastModified = file.lastModified()
            val fileSize = if (file.isFile) file.length() else directorySize(file)
            JunkFile(
                id = file.absolutePath.hashCode().toString() + "_" + lastModified,
                filePath = file.absolutePath,
                fileName = file.name,
                fileSize = fileSize,
                category = category,
                lastModified = lastModified,
            )
        }.getOrNull()

    private fun directorySize(directory: File): Long =
        runCatching {
            directory
                .walkTopDown()
                .filter { it.isFile }
                .take(500)
                .sumOf { it.length() }
        }.getOrDefault(0L)

    private fun safeListFiles(directory: File): List<File> =
        runCatchingLogged("listFiles:${directory.absolutePath}") { directory.listFiles()?.toList() }
            .getOrNull().orEmpty()

    companion object {
        private fun <T> runCatchingLogged(tag: String, block: () -> T): Result<T> =
            runCatching(block).onFailure { e ->
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.w("BaseFileScanner", "$tag failed: ${e.message}")
                }
            }
    }
}
