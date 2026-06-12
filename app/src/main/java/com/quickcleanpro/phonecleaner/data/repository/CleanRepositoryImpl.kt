package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import com.quickcleanpro.phonecleaner.domain.model.ScanProgress
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.data.scanner.AdJunkScanner
import com.quickcleanpro.phonecleaner.data.scanner.ApkScanner
import com.quickcleanpro.phonecleaner.data.scanner.CacheScanner
import com.quickcleanpro.phonecleaner.data.scanner.DuplicateFileScanner
import com.quickcleanpro.phonecleaner.data.scanner.JunkScanner
import com.quickcleanpro.phonecleaner.data.scanner.ResidualScanner
import com.quickcleanpro.phonecleaner.data.scanner.ScanDirectoryHelper
import com.quickcleanpro.phonecleaner.data.scanner.TempFileScanner
import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.CleanResult
import com.quickcleanpro.phonecleaner.domain.repository.CleanRepository
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult
import com.quickcleanpro.phonecleaner.data.source.clean.MemoryCleaner
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class CleanRepositoryImpl(
    private val context: Context,
    private val sharedState: SharedScanState
) : CleanRepository {

    private val cacheScanner = CacheScanner(context)
    private val tempFileScanner = TempFileScanner()
    private val apkScanner = ApkScanner()
    private val residualScanner = ResidualScanner(context)
    private val adJunkScanner = AdJunkScanner(context)
    private val duplicateFileScanner = DuplicateFileScanner()
    private val memoryCleaner = MemoryCleaner

    private val scanners = listOf(
        cacheScanner,
        tempFileScanner,
        residualScanner,
        apkScanner,
        adJunkScanner,
        duplicateFileScanner
    )

    override val scanProgress: Flow<ScanProgress> get() = sharedState.scanProgress

    override suspend fun performFullScan(): ScanResult = withContext(Dispatchers.IO) {
        ScanDirectoryHelper.clearCache()
        val allJunkFiles = mutableListOf<JunkFile>()
        sharedState.setScanProgress(ScanProgress(percent = 0f, foundCount = 0, foundSize = 0L))

        val weightPerScanner = 100f / scanners.size

        for ((index, scanner) in scanners.withIndex()) {
            sharedState.setScanProgress(
                ScanProgress(
                    percent = index * weightPerScanner,
                    currentCategory = scanner.category,
                    foundCount = allJunkFiles.size,
                    foundSize = allJunkFiles.sumOf { it.fileSize }
                )
            )
            delay(150)

            val files = scanSafely(scanner)
            allJunkFiles.addAll(files)

            sharedState.setScanProgress(
                ScanProgress(
                    percent = (index + 1) * weightPerScanner,
                    currentCategory = scanner.category,
                    foundCount = allJunkFiles.size,
                    foundSize = allJunkFiles.sumOf { it.fileSize }
                )
            )
        }

        val uniqueJunkFiles = dedupeJunkFiles(allJunkFiles)
        val totalJunkSize = uniqueJunkFiles.sumOf { it.fileSize }
        val categoryBreakdown = uniqueJunkFiles.groupBy { it.category }
        sharedState.setScanProgress(
            ScanProgress(
                percent = 100f,
                foundCount = uniqueJunkFiles.size,
                foundSize = totalJunkSize
            )
        )

        val result = ScanResult(
            junkFiles = uniqueJunkFiles,
            totalSize = totalJunkSize,
            totalCount = uniqueJunkFiles.size,
            categoryBreakdown = categoryBreakdown
        )
        sharedState.setScanResult(result)
        result
    }

    override suspend fun cleanFiles(selectedItems: List<CleanItem>): CleanResult =
        withContext(Dispatchers.IO) {
            val cleanedFiles = mutableListOf<JunkFile>()
            val failedFiles = mutableListOf<JunkFile>()
            var freedSpace = 0L

            for (item in selectedItems) {
                val outcome = JunkFileDeleteHelper.delete(context, item.junkFile)
                if (outcome.deleted) {
                    cleanedFiles.add(item.junkFile)
                    freedSpace += outcome.freedBytes
                } else {
                    failedFiles.add(item.junkFile)
                }
            }

            val result = CleanResult(
                cleanedFiles = cleanedFiles,
                freedSpace = freedSpace,
                failedFiles = failedFiles
            )
            sharedState.removeCleanedFiles(cleanedFiles)
            sharedState.setCleanResult(result)
            result
        }

    override suspend fun cleanMemory(): MemoryCleanResult = withContext(Dispatchers.IO) {
        val result = memoryCleaner.clean(context.applicationContext)
        sharedState.setMemoryResult(result)
        result
    }

    private fun dedupeJunkFiles(files: List<JunkFile>): List<JunkFile> {
        val sorted = files.sortedWith(
            compareBy<JunkFile> { normalizedPath(it.filePath).length }
                .thenBy { it.category.ordinal }
        )
        val kept = mutableListOf<JunkFile>()
        val keptPaths = mutableSetOf<String>()

        for (file in sorted) {
            val path = normalizedPath(file.filePath)
            if (path in keptPaths) continue
            if (keptPaths.any { parent -> path != parent && path.startsWith("$parent/") }) continue

            kept += file
            keptPaths += path
        }

        return kept
    }

    private fun normalizedPath(path: String): String =
        runCatching { File(path).canonicalPath }
            .getOrElse { path }
            .replace('\\', '/')
            .trimEnd('/')

    private suspend fun scanSafely(scanner: JunkScanner): List<JunkFile> =
        try {
            scanner.scan()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            emptyList()
        }
}
