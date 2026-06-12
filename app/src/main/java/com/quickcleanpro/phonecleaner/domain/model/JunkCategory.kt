package com.quickcleanpro.phonecleaner.domain.model

/**
 * 鍨冨溇鏂囦欢涓氬姟鍒嗙被銆? *
 * 鐢ㄤ簬鎵弿銆佺粨鏋滈瑙堝拰娓呯悊姹囨€荤瓑涓绘竻鐞嗛摼璺紝
 * 鍥犳鏀惧湪 domain 灞備綔涓鸿法 data 涓?presentation 鐨勭ǔ瀹氭ā鍨嬨€? */
enum class JunkCategory(
    val displayName: String,
    val description: String
) {
    CACHE("Cache Files", "App and system cache"),
    TEMP_FILE("Temp Files", "Temporary files in temp directories"),
    RESIDUAL("Residual Files", "Leftover files from uninstalled apps"),
    APK("APK Files", "Downloaded APK installers"),
    DUPLICATE("Duplicate Files", "Files with duplicate content"),
    LARGE_FILE("Large Files", "Files taking up too much space")
}
