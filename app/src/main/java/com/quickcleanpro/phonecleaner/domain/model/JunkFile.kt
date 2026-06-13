package com.quickcleanpro.phonecleaner.domain.model

/**
 * 涓绘竻鐞嗛摼璺腑鐨勫瀮鍦炬枃浠舵ā鍨嬨€? *
 * 璇ユā鍨嬭〃绀哄凡缁忚鎵弿鍣ㄨ瘑鍒嚭鐨勫彲娓呯悊鏂囦欢锛? * 浼氬湪浠撳簱銆佺敤渚嬨€佸叡浜姸鎬佸拰缁撴灉椤典箣闂翠紶閫掋€? */
data class JunkFile(
    val id: String,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val category: JunkCategory,
    val lastModified: Long = System.currentTimeMillis(),
) {
    /** 鏍煎紡鍖栧悗鐨勬枃浠跺ぇ灏忔枃妗堛€?*/
    val formattedSize: String
        get() = formatFileSize(fileSize)

    companion object {
        /**
         * 灏嗗瓧鑺傛暟鏍煎紡鍖栦负闈㈠悜 UI 鐨勭煭鏂囨湰銆?         */
        fun formatFileSize(bytes: Long): String =
            when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
                bytes < 1024 * 1024 * 1024 -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
                else -> "${"%.1f".format(bytes / (1024.0 * 1024 * 1024))} GB"
            }
    }
}
