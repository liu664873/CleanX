package com.quickcleanpro.phonecleaner.domain.model

/**
 * 娓呯悊缁撴灉棰嗗煙妯″瀷
 */
data class CleanResult(
    val cleanedFiles: List<JunkFile>,
    val freedSpace: Long,
    val failedFiles: List<JunkFile> = emptyList(),
) {
    val successCount: Int
        get() = cleanedFiles.size

    val failedCount: Int
        get() = failedFiles.size

    val formattedFreedSpace: String
        get() = JunkFile.formatFileSize(freedSpace)

    val isAllSuccess: Boolean
        get() = failedFiles.isEmpty()
}
