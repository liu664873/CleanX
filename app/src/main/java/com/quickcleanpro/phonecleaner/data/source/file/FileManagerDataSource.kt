package com.quickcleanpro.phonecleaner.data.source.file

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

object FileManagerDataSource {

    suspend fun loadImages(context: Context) = withContext(Dispatchers.IO) {
        queryMedia(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ManagedFileType.Image)
    }

    suspend fun loadVideos(context: Context) = withContext(Dispatchers.IO) {
        queryMedia(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, ManagedFileType.Video)
    }

    suspend fun loadAudios(context: Context) = withContext(Dispatchers.IO) {
        queryMedia(context.contentResolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, ManagedFileType.Audio)
    }

    suspend fun loadScreenshots(context: Context) = withContext(Dispatchers.IO) {
        loadImages(context).filter {
            it.bucketName?.contains("screenshot", true) == true ||
                it.path?.contains("screenshot", true) == true
        }
    }

    suspend fun loadPrivacyImages(context: Context) = withContext(Dispatchers.IO) {
        loadImages(context).filter { hasGpsLocation(context, it) }
    }

    suspend fun loadDocuments(context: Context) = withContext(Dispatchers.IO) {
        (queryFiles(context.contentResolver) + discoverDocumentFilesFromFileSystemIfAllowed())
            .filter { item -> isAllowedDocumentCandidate(item.name, item.mimeType) }
            .distinctBy(::fileIdentity)
            .sortedByDescending { it.modifiedSeconds }
    }

    suspend fun loadLargeFiles(context: Context, minBytes: Long = 10L * 1024 * 1024) = withContext(Dispatchers.IO) {
        discoverFiles(context)
            .filter { it.sizeBytes >= minBytes }
            .sortedByDescending { it.sizeBytes }
    }

    suspend fun loadDuplicateFiles(context: Context): List<List<ManagedFileItem>> = withContext(Dispatchers.IO) {
        buildDuplicateFileGroups(
            files = discoverFiles(context),
            contentHash = { item -> computeContentHash(context, item) }
        )
    }

    suspend fun loadWhatsAppFiles(context: Context) = withContext(Dispatchers.IO) {
        runCatching {
            (queryFiles(context.contentResolver).filter(::isWhatsAppCandidate) +
                scanWhatsAppFilesFromFileSystem())
                .filter { it.sizeBytes > 0L }
                .distinctBy(::fileIdentity)
                .sortedByDescending { it.modifiedSeconds }
        }.getOrElse { emptyList() }
    }

    suspend fun deleteFiles(context: Context, items: List<ManagedFileItem>): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        items.forEach { item ->
            val deleted = runCatching {
                context.contentResolver.delete(item.uri, null, null) > 0
            }.getOrDefault(false)
            val deletedFromPath = !deleted && item.path != null && runCatching {
                File(item.path).delete()
            }.getOrDefault(false)
            if (deleted || deletedFromPath || !fileStillExists(context, item)) {
                freed += item.sizeBytes
            }
        }
        freed
    }

    suspend fun removeLocationData(context: Context, items: List<ManagedFileItem>): Int = withContext(Dispatchers.IO) {
        var changed = 0
        items.forEach { item ->
            val path = item.path
            if (!path.isNullOrBlank()) {
                val removed = runCatching {
                    val exif = ExifInterface(path)
                    clearGpsAttributes(exif)
                    exif.saveAttributes()
                    true
                }.getOrDefault(false)
                if (removed) changed++
            }
        }
        changed
    }

    fun isDocumentFile(name: String, mimeType: String?): Boolean {
        if (isMediaFileName(name, mimeType)) return false
        val lowerName = name.lowercase(Locale.US)
        val extension = lowerName.substringAfterLast('.', missingDelimiterValue = "")
        val mime = mimeType.orEmpty().lowercase(Locale.US)
        return extension in DOCUMENT_EXTENSIONS ||
            mime.startsWith("text/") ||
            DOCUMENT_MIME_KEYWORDS.any { keyword -> mime.contains(keyword) }
    }

    fun isMediaFileName(name: String, mimeType: String?): Boolean {
        val extension = name.lowercase(Locale.US).substringAfterLast('.', missingDelimiterValue = "")
        val mime = mimeType.orEmpty().lowercase(Locale.US)
        return extension in MEDIA_EXTENSIONS ||
            mime.startsWith("image/") ||
            mime.startsWith("video/") ||
            mime.startsWith("audio/")
    }

    fun isAllowedDocumentCandidate(name: String, mimeType: String?): Boolean =
        isDocumentFile(name, mimeType) && !isMediaFileName(name, mimeType)

    fun hasAllFilesAccess(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
            runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)

    fun allFilesAccessIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }

    fun allFilesAccessFallbackIntent(): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }

    fun buildDuplicateFileGroups(
        files: List<ManagedFileItem>,
        contentHash: (ManagedFileItem) -> String?
    ): List<List<ManagedFileItem>> {
        val uniqueFiles = files
            .filter { it.sizeBytes > 0L }
            .distinctBy(::fileIdentity)

        val hashGroups = mutableListOf<List<ManagedFileItem>>()
        val fallbackCandidates = mutableListOf<ManagedFileItem>()

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
            .distinctBy { group -> group.joinToString("|") { fileIdentity(it) } }
            .sortedByDescending(::duplicateReclaimableBytes)
    }

    fun scanPublicFilesFromFileSystem(): List<ManagedFileItem> {
        val root = externalStorageDirectoryOrNull() ?: return emptyList()
        if (!root.exists() || !root.isDirectory) return emptyList()

        val results = mutableListOf<ManagedFileItem>()
        collectPublicFiles(root, results, maxDepth = 1)
        PUBLIC_SCAN_DIRECTORIES
            .map { File(root, it) }
            .filter { it.exists() && it.isDirectory && it.canRead() }
            .distinctBy { runCatching { it.canonicalPath }.getOrDefault(it.absolutePath) }
            .forEach { directory -> collectPublicFiles(directory, results, maxDepth = 12) }

        return results.distinctBy(::fileIdentity)
    }

    internal fun scanDocumentFilesFromFileSystem(): List<ManagedFileItem> {
        val root = externalStorageDirectoryOrNull() ?: return emptyList()
        if (!root.exists() || !root.isDirectory) return emptyList()

        val results = mutableListOf<ManagedFileItem>()
        collectDocumentFiles(root, results, maxDepth = 2)
        DOCUMENT_SCAN_DIRECTORIES
            .map { File(root, it) }
            .filter { it.exists() && it.isDirectory && it.canRead() }
            .distinctBy { runCatching { it.canonicalPath }.getOrDefault(it.absolutePath) }
            .forEach { directory -> collectDocumentFiles(directory, results, maxDepth = 16) }

        return results.distinctBy(::fileIdentity)
    }

    internal fun scanWhatsAppFilesFromFileSystem(): List<ManagedFileItem> {
        val root = externalStorageDirectoryOrNull() ?: return emptyList()
        if (!root.exists() || !root.isDirectory) return emptyList()

        val results = mutableListOf<ManagedFileItem>()
        WHATSAPP_SCAN_DIRECTORIES
            .map { File(root, it) }
            .filter { it.exists() && it.isDirectory && it.canRead() }
            .distinctBy { runCatching { it.canonicalPath }.getOrDefault(it.absolutePath) }
            .forEach { directory -> collectWhatsAppFiles(directory, results, maxDepth = 18) }

        return results.distinctBy(::fileIdentity)
    }

    private fun discoverFiles(context: Context): List<ManagedFileItem> =
        (runCatching { queryFiles(context.contentResolver) }.getOrElse { emptyList() } +
            discoverPublicFilesFromFileSystemIfAllowed())
            .distinctBy(::fileIdentity)

    private fun discoverPublicFilesFromFileSystemIfAllowed(): List<ManagedFileItem> =
        if (hasAllFilesAccess()) {
            runCatching { scanPublicFilesFromFileSystem() }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

    private fun discoverDocumentFilesFromFileSystemIfAllowed(): List<ManagedFileItem> =
        if (hasAllFilesAccess()) {
            runCatching { scanDocumentFilesFromFileSystem() }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

    private fun externalStorageDirectoryOrNull(): File? =
        runCatching { Environment.getExternalStorageDirectory() }.getOrNull()

    private fun queryMedia(resolver: ContentResolver, collection: Uri, type: ManagedFileType): List<ManagedFileItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        )
        return runCatching {
            resolver.query(collection, projection, null, null, "${MediaStore.MediaColumns.DATE_MODIFIED} DESC")
            ?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val modCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                val pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val bucketCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                if (idCol < 0 || nameCol < 0) return@use emptyList()
                buildList {
                    while (cursor.moveToNext()) {
                        val path = cursor.getStringOrNull(pathCol)
                        val name = cursor.getStringOrNull(nameCol) ?: "Unknown"
                        if (shouldSkipPath(path, name)) continue

                        add(
                            ManagedFileItem(
                                id = cursor.getLongOrZero(idCol),
                                uri = ContentUris.withAppendedId(collection, cursor.getLongOrZero(idCol)),
                                path = path,
                                name = name,
                                sizeBytes = cursor.getLongOrZero(sizeCol),
                                modifiedSeconds = cursor.getLongOrZero(modCol),
                                mimeType = cursor.getStringOrNull(mimeCol),
                                bucketName = cursor.getStringOrNull(bucketCol),
                                type = type
                            )
                        )
                    }
                }
            } ?: emptyList()
        }.getOrElse { emptyList() }
    }

    private fun queryFiles(resolver: ContentResolver): List<ManagedFileItem> {
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE
        )
        return runCatching {
            resolver.query(collection, projection, null, null, "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC")
            ?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
                val modCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val mimeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
                val pathCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
                val mediaTypeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
                if (idCol < 0 || nameCol < 0) return@use emptyList()
                buildList {
                    while (cursor.moveToNext()) {
                        val path = cursor.getStringOrNull(pathCol)
                        val name = cursor.getStringOrNull(nameCol) ?: "Unknown"
                        if (shouldSkipPath(path, name)) continue

                        add(
                            ManagedFileItem(
                                id = cursor.getLongOrZero(idCol),
                                uri = ContentUris.withAppendedId(collection, cursor.getLongOrZero(idCol)),
                                path = path,
                                name = name,
                                sizeBytes = cursor.getLongOrZero(sizeCol),
                                modifiedSeconds = cursor.getLongOrZero(modCol),
                                mimeType = cursor.getStringOrNull(mimeCol),
                                bucketName = path?.substringBeforeLast('/'),
                                type = when (cursor.getIntOrZero(mediaTypeCol)) {
                                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> ManagedFileType.Image
                                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> ManagedFileType.Video
                                    MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> ManagedFileType.Audio
                                    else -> ManagedFileType.Document
                                }
                            )
                        )
                    }
                }
            } ?: emptyList()
        }.getOrElse { emptyList() }
    }

    private fun collectPublicFiles(directory: File, results: MutableList<ManagedFileItem>, maxDepth: Int) {
        if (maxDepth < 0 || results.size >= MAX_FILE_SYSTEM_RESULTS) return
        val children = runCatching { directory.listFiles() }.getOrNull().orEmpty()
        children.forEach { file ->
            if (results.size >= MAX_FILE_SYSTEM_RESULTS) return
            if (shouldSkipPath(file.absolutePath, file.name)) return@forEach
            if (file.isDirectory) {
                collectPublicFiles(file, results, maxDepth - 1)
            } else if (file.isFile && file.length() > 0L) {
                results += file.toManagedFileItem()
            }
        }
    }

    private fun collectDocumentFiles(directory: File, results: MutableList<ManagedFileItem>, maxDepth: Int) {
        if (maxDepth < 0 || results.size >= MAX_DOCUMENT_FILE_SYSTEM_RESULTS) return
        val children = runCatching { directory.listFiles() }.getOrNull().orEmpty()
        children.forEach { file ->
            if (results.size >= MAX_DOCUMENT_FILE_SYSTEM_RESULTS) return
            if (shouldSkipPath(file.absolutePath, file.name)) return@forEach
            if (file.isDirectory) {
                collectDocumentFiles(file, results, maxDepth - 1)
            } else if (file.isFile && file.length() > 0L && isAllowedDocumentCandidate(file.name, null)) {
                results += file.toManagedFileItem()
            }
        }
    }

    private fun collectWhatsAppFiles(directory: File, results: MutableList<ManagedFileItem>, maxDepth: Int) {
        if (maxDepth < 0 || results.size >= MAX_WHATSAPP_FILE_SYSTEM_RESULTS) return
        val children = runCatching { directory.listFiles() }.getOrNull().orEmpty()
        children.forEach { file ->
            if (results.size >= MAX_WHATSAPP_FILE_SYSTEM_RESULTS) return
            if (shouldSkipPath(file.absolutePath, file.name)) return@forEach
            if (file.isDirectory) {
                collectWhatsAppFiles(file, results, maxDepth - 1)
            } else if (file.isFile && file.length() > 0L) {
                results += file.toManagedFileItem()
            }
        }
    }

    private fun File.toManagedFileItem(): ManagedFileItem =
        ManagedFileItem(
            id = absolutePath.hashCode().toLong(),
            uri = Uri.fromFile(this),
            path = absolutePath,
            name = name,
            sizeBytes = length(),
            modifiedSeconds = lastModified(),
            mimeType = null,
            bucketName = parentFile?.name,
            type = fileTypeForName(name)
        )

    private fun fileStillExists(context: Context, item: ManagedFileItem): Boolean {
        item.path?.takeIf { it.isNotBlank() }?.let { path ->
            if (File(path).exists()) return true
        }
        return runCatching {
            context.contentResolver.openFileDescriptor(item.uri, "r")?.use { true } ?: false
        }.getOrDefault(false)
    }

    private fun fileTypeForName(name: String): ManagedFileType {
        val extension = name.lowercase(Locale.US).substringAfterLast('.', missingDelimiterValue = "")
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif" -> ManagedFileType.Image
            "mp4", "mkv", "mov", "avi", "webm", "3gp" -> ManagedFileType.Video
            "mp3", "wav", "m4a", "aac", "ogg", "flac" -> ManagedFileType.Audio
            else -> ManagedFileType.Document
        }
    }

    private fun computeContentHash(context: Context, item: ManagedFileItem): String? =
        runCatching {
            val stream = when {
                !item.path.isNullOrBlank() && File(item.path).isFile -> FileInputStream(item.path)
                else -> context.contentResolver.openInputStream(item.uri)
            }
            stream?.use(::hashStream)
        }.getOrNull()

    private fun hashStream(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun hasGpsLocation(context: Context, item: ManagedFileItem): Boolean {
        return runCatching {
            if (!item.path.isNullOrBlank()) {
                val exif = ExifInterface(item.path)
                FloatArray(2).let { exif.getLatLong(it) } ||
                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null
            } else {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.setRequireOriginal(item.uri)
                } else {
                    item.uri
                }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    FloatArray(2).let { exif.getLatLong(it) } ||
                        exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null
                } ?: false
            }
        }.getOrDefault(false)
    }

    private fun clearGpsAttributes(exif: ExifInterface) {
        listOf(
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_GPS_PROCESSING_METHOD
        ).forEach { exif.setAttribute(it, null) }
    }

    private fun android.database.Cursor.getStringOrNull(index: Int): String? =
        if (index >= 0 && !isNull(index)) getString(index) else null

    private fun android.database.Cursor.getLongOrZero(index: Int): Long =
        if (index >= 0 && !isNull(index)) getLong(index) else 0L

    private fun android.database.Cursor.getIntOrZero(index: Int): Int =
        if (index >= 0 && !isNull(index)) getInt(index) else 0

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

    private fun isWhatsAppCandidate(item: ManagedFileItem): Boolean {
        val source = listOfNotNull(item.path, item.bucketName, item.name)
            .joinToString("/")
            .lowercase(Locale.US)
        return WHATSAPP_PATH_MARKERS.any { marker -> marker in source }
    }

    private fun fileIdentity(item: ManagedFileItem): String =
        item.path
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { File(it).canonicalPath }.getOrDefault(it) }
            ?: item.uri.toString()

    private fun duplicateReclaimableBytes(group: List<ManagedFileItem>): Long =
        group.sortedByDescending { it.modifiedSeconds }.drop(1).sumOf { it.sizeBytes }

    private val DOCUMENT_EXTENSIONS = setOf(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf",
        "xml", "json", "md", "markdown", "epub", "mobi", "azw", "azw3", "fb2",
        "zip", "rar", "7z", "tar", "gz", "tgz", "bz2", "xz",
        "odt", "ods", "odp", "ott", "ots", "otp",
        "wps", "et", "dps", "pages", "numbers", "key", "xps",
        "log", "ini", "conf", "cfg", "yaml", "yml", "toml", "properties",
        "sql", "db", "sqlite", "sqlite3",
        "kt", "kts", "java", "js", "ts", "html", "htm", "css", "py", "sh", "bat",
        "gradle", "c", "cpp", "h", "hpp", "cs", "go", "rs", "php", "rb", "swift"
    )

    private val DOCUMENT_MIME_KEYWORDS = listOf(
        "pdf", "document", "spreadsheet", "presentation", "msword", "officedocument",
        "opendocument", "word", "excel", "powerpoint", "wps", "csv", "rtf", "epub",
        "ebook", "markdown", "javascript", "xhtml", "html", "yaml", "json", "xml",
        "sqlite", "database", "archive", "compressed", "gzip", "tar", "zip", "rar", "7z"
    )

    private val MEDIA_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif", "tif", "tiff", "svg",
        "mp4", "mkv", "mov", "avi", "webm", "3gp", "m4v", "wmv", "flv", "mpeg", "mpg",
        "mp3", "wav", "m4a", "aac", "ogg", "oga", "flac", "amr", "wma", "opus"
    )

    private val PUBLIC_SCAN_DIRECTORIES = listOf(
        "Download", "Downloads", "Documents", "DCIM", "Pictures", "Movies", "Music",
        "WhatsApp", "Telegram", "Android/media"
    )

    private val DOCUMENT_SCAN_DIRECTORIES = listOf(
        "Download", "Downloads", "Documents", "Desktop", "Android/media", "Android/data",
        "WhatsApp", "WhatsApp/Media/WhatsApp Documents", "Telegram", "Telegram/Telegram Documents",
        "Tencent", "Tencent/QQfile_recv", "Tencent/MicroMsg", "WeChat", "DingTalk",
        "Feishu", "Lark", "BaiduNetdisk", "UCDownloads", "Browser", "Chrome", "QQBrowser",
        "WPS", "Kingsoft", "Office", "Documents/WeChat Files"
    )

    private val WHATSAPP_SCAN_DIRECTORIES = listOf(
        "WhatsApp",
        "WhatsApp Business",
        "Android/media/com.whatsapp/WhatsApp",
        "Android/media/com.whatsapp.w4b/WhatsApp Business",
        "Android/media/com.whatsapp.w4b/WhatsApp",
        "Android/media/com.gbwhatsapp/GBWhatsApp",
        "Android/media/com.yowhatsapp/YoWhatsApp",
        "Android/media/com.fmwhatsapp/FMWhatsApp"
    )

    private val WHATSAPP_PATH_MARKERS = listOf(
        "/whatsapp/",
        "/whatsapp business/",
        "/gbwhatsapp/",
        "/yowhatsapp/",
        "/fmwhatsapp/",
        "com.whatsapp",
        "com.whatsapp.w4b"
    )

    private const val MAX_FILE_SYSTEM_RESULTS = 10_000
    private const val MAX_DOCUMENT_FILE_SYSTEM_RESULTS = 30_000
    private const val MAX_WHATSAPP_FILE_SYSTEM_RESULTS = 30_000
}
