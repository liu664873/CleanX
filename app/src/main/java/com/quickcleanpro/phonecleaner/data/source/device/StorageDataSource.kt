package com.quickcleanpro.phonecleaner.data.source.device

import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import com.quickcleanpro.phonecleaner.domain.model.device.StorageInfo
import com.quickcleanpro.phonecleaner.util.FileSizeFormatter
import java.io.File

/**
 * 存储空间数据源。
 *
 * 提供获取内部存储、外部存储（包括主外部存储和 SD 卡）的总容量、可用容量和已用容量的方法。
 * 使用 [StatFs] 对文件系统进行统计。
 */
object StorageDataSource {
    /**
     * 将字节数格式化为可读的字符串（如 "12.5 MB"）。
     *
     * @param bytes 文件大小（字节）
     * @return 格式化后的字符串
     */
    fun formatFileSize(bytes: Long): String = FileSizeFormatter.format(bytes)

    /**
     * 获取内部存储（/data 分区）的存储信息。
     *
     * @return [StorageInfo] 包含总字节、可用字节和已用字节。
     */
    fun getInternalStorageInfo(): StorageInfo {
        val path = Environment.getDataDirectory()
        return getStorageInfo(path)
    }

    /**
     * 获取外部存储的总存储信息。
     *
     * 遍历所有可用的外部存储目录（主外部存储 + 其他挂载点如 SD 卡），
     * 累加总容量和可用容量，计算总已用容量。
     *
     * @return [StorageInfo] 所有外部存储聚合后的存储信息。
     */
    fun getExternalStorageInfo(): StorageInfo {
        val dirs = getExternalStorageDirectories()
        if (dirs.isEmpty()) return StorageInfo(0, 0, 0)
        var totalBytes = 0L
        var availableBytes = 0L
        for (dir in dirs) {
            val info = getStorageInfo(dir)
            totalBytes += info.totalBytes
            availableBytes += info.availableBytes
        }
        return StorageInfo(totalBytes, availableBytes, totalBytes - availableBytes)
    }

    /**
     * 获取总已用存储空间（内部存储已用 + 外部存储已用）。
     *
     * @return 已用总字节数
     */
    fun getTotalUsedStorage(): Long = getInternalStorageInfo().usedBytes + getExternalStorageInfo().usedBytes

    /**
     * 获取总可用存储空间（内部存储可用 + 外部存储可用）。
     *
     * @return 可用总字节数
     */
    fun getTotalAvailableStorage(): Long = getInternalStorageInfo().availableBytes + getExternalStorageInfo().availableBytes

    /**
     * 获取所有可用的外部存储目录（兼容 Android 11+）。
     *
     * 包括主外部存储（如 /storage/emulated/0）以及其他挂载的存储卷（如 SD 卡）。
     * 注意：要获取完整的可移动存储卷列表，通常需要 [StorageManager] 和 Context，
     * 此方法通过扫描 /storage 目录作为补充，但可能不覆盖所有设备。
     *
     * @return 外部存储目录列表
     */
    fun getExternalStorageDirectories(): List<File> {
        val dirs = mutableListOf<File>()
        // 主外部存储（通常为内置共享存储）
        val primaryExternal = runCatching { Environment.getExternalStorageDirectory() }.getOrNull()
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED && primaryExternal != null) {
            dirs.add(primaryExternal)
        }
        // 其他挂载点（如 SD 卡），尝试扫描 /storage 目录
        runCatching { File("/storage").listFiles() }
            .getOrNull()
            .orEmpty()
            .forEach { candidate ->
                val primaryPath = primaryExternal?.absolutePath
                if (candidate.isDirectory &&
                    candidate.canRead() &&
                    candidate.absolutePath != primaryPath
                ) {
                    dirs.add(candidate)
                }
            }
        return dirs.distinctBy { it.absolutePath }
    }

    /**
     * 根据指定路径获取存储信息。
     *
     * @param path 要查询的目录路径（如 /data 或 /storage/emulated/0）
     * @return [StorageInfo] 包含总容量、可用容量和已用容量；若异常则返回零值。
     */
    private fun getStorageInfo(path: File): StorageInfo =
        try {
            val stat = StatFs(path.absolutePath)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            val totalBytes = blockSize * totalBlocks
            val availableBytes = blockSize * availableBlocks
            StorageInfo(totalBytes, availableBytes, totalBytes - availableBytes)
        } catch (e: Exception) {
            StorageInfo(0, 0, 0)
        }
}
