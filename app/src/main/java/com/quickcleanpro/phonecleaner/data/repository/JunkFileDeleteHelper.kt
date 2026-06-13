package com.quickcleanpro.phonecleaner.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import java.io.File

data class JunkDeleteOutcome(
    val junkFile: JunkFile,
    val deleted: Boolean,
    val freedBytes: Long,
    val authorizationUri: Uri? = null,
)

data class JunkAuthorizedDeleteResult(
    val cleanedFiles: List<JunkFile>,
    val failedFiles: List<JunkFile>,
    val freedBytes: Long,
)

object JunkFileDeleteHelper {
    fun delete(
        context: Context,
        junkFile: JunkFile,
    ): JunkDeleteOutcome {
        val file = File(junkFile.filePath)
        if (!file.exists()) {
            return JunkDeleteOutcome(
                junkFile = junkFile,
                deleted = true,
                freedBytes = junkFile.fileSize,
            )
        }

        val beforeSize = sizeOf(file).takeIf { it > 0L } ?: junkFile.fileSize
        val deleted =
            if (file.isDirectory) {
                deleteDirectory(context, file)
            } else {
                deleteFile(context, file)
            }

        return JunkDeleteOutcome(
            junkFile = junkFile,
            deleted = deleted && !file.exists(),
            freedBytes = if (deleted && !file.exists()) beforeSize else 0L,
            authorizationUri = if (!deleted && file.isFile) findMediaUri(context.contentResolver, file) else null,
        )
    }

    fun collectAuthorizationUris(outcomes: List<JunkDeleteOutcome>): List<Uri> = outcomes.mapNotNull { it.authorizationUri }.distinct()

    fun finalizeAuthorizedDeletes(pendingOutcomes: List<JunkDeleteOutcome>): JunkAuthorizedDeleteResult {
        val cleanedFiles = mutableListOf<JunkFile>()
        val failedFiles = mutableListOf<JunkFile>()
        var freedBytes = 0L

        for (outcome in pendingOutcomes) {
            val file = File(outcome.junkFile.filePath)
            if (!file.exists()) {
                cleanedFiles += outcome.junkFile
                freedBytes += outcome.junkFile.fileSize
            } else {
                failedFiles += outcome.junkFile
            }
        }

        return JunkAuthorizedDeleteResult(
            cleanedFiles = cleanedFiles,
            failedFiles = failedFiles,
            freedBytes = freedBytes,
        )
    }

    private fun deleteDirectory(
        context: Context,
        directory: File,
    ): Boolean {
        val children = directory.listFiles().orEmpty()
        var allChildrenDeleted = true
        for (child in children) {
            val deleted =
                if (child.isDirectory) {
                    deleteDirectory(context, child)
                } else {
                    deleteFile(context, child)
                }
            if (!deleted || child.exists()) allChildrenDeleted = false
        }

        val ownDeleted = runCatching { directory.delete() }.getOrDefault(false)
        return (ownDeleted || !directory.exists()) && allChildrenDeleted
    }

    private fun deleteFile(
        context: Context,
        file: File,
    ): Boolean {
        if (!file.exists()) return true

        val directDeleted = runCatching { file.delete() }.getOrDefault(false)
        if (directDeleted && !file.exists()) return true

        val mediaDeleted = deleteViaMediaStore(context.contentResolver, file)
        return mediaDeleted && !file.exists()
    }

    private fun deleteViaMediaStore(
        contentResolver: ContentResolver,
        file: File,
    ): Boolean {
        val uri = findMediaUri(contentResolver, file) ?: return false
        return runCatching {
            contentResolver.delete(uri, null, null) > 0
        }.getOrDefault(false)
    }

    fun findMediaUri(
        contentResolver: ContentResolver,
        file: File,
    ): Uri? {
        val path = file.absolutePath
        val collections =
            buildList {
                add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                add(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                add(MediaStore.Files.getContentUri("external"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    add(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
                }
            }

        for (collection in collections) {
            val uri = queryMediaUri(contentResolver, collection, path)
            if (uri != null) return uri
        }
        return null
    }

    private fun queryMediaUri(
        contentResolver: ContentResolver,
        collection: Uri,
        path: String,
    ): Uri? {
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DATA} = ?"

        return runCatching {
            contentResolver.query(collection, projection, selection, arrayOf(path), null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    Uri.withAppendedPath(collection, id.toString())
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    private fun sizeOf(file: File): Long {
        if (!file.exists()) return 0L
        if (file.isFile) return file.length()
        return runCatching {
            file
                .walkTopDown()
                .filter { it.isFile }
                .sumOf { it.length() }
        }.getOrDefault(0L)
    }
}
