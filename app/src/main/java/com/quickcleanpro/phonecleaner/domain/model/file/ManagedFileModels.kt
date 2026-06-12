package com.quickcleanpro.phonecleaner.domain.model.file

import android.net.Uri
import com.quickcleanpro.phonecleaner.util.FileSizeFormatter

/**
 * 鏂囦欢绠＄悊妯″潡鏀寔鐨勬枃浠剁被鍨嬨€? */
enum class ManagedFileType { Image, Video, Audio, Document, Other }

/**
 * 鏂囦欢绠＄悊妯″潡璺ㄥ眰浼犻€掔殑鏂囦欢瀹炰綋銆? */
data class ManagedFileItem(
    val id: Long,
    val uri: Uri,
    val path: String?,
    val name: String,
    val sizeBytes: Long,
    val modifiedSeconds: Long,
    val mimeType: String?,
    val bucketName: String?,
    val type: ManagedFileType
) {
    /** 宸叉牸寮忓寲鐨勬枃浠跺ぇ灏忔枃妗堛€?*/
    val formattedSize: String get() = FileSizeFormatter.format(sizeBytes)
}
