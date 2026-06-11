package com.clean.cleanx.domain.model

import com.clean.cleanx.utils.FileSizeFormatter

/**
 * 电池基础信息。
 */
data class BatteryInfo(
    val levelPercent: Int,
    val health: String,
    val temperature: Float,
    val voltage: Int,
    val technology: String,
    val capacity: Int,
    val availableTime: String = "Unknown"
)

/**
 * 内存使用信息。
 */
data class MemoryInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val isTotalValid: Boolean
) {
    /** 已格式化的总内存大小。 */
    val formattedTotal: String get() = FileSizeFormatter.format(totalBytes)

    /** 已格式化的可用内存大小。 */
    val formattedAvailable: String get() = FileSizeFormatter.format(availableBytes)

    /** 已格式化的已用内存大小。 */
    val formattedUsed: String get() = FileSizeFormatter.format(usedBytes)
}

/**
 * 存储空间使用信息。
 */
data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long
) {
    /** 已格式化的总空间大小。 */
    val formattedTotal: String get() = FileSizeFormatter.format(totalBytes)

    /** 已格式化的可用空间大小。 */
    val formattedAvailable: String get() = FileSizeFormatter.format(availableBytes)

    /** 已格式化的已用空间大小。 */
    val formattedUsed: String get() = FileSizeFormatter.format(usedBytes)

    /** 存储使用百分比。 */
    val usagePercent: Int get() = if (totalBytes > 0) ((usedBytes.toFloat() / totalBytes) * 100).toInt() else 0

    /** 是否处于低存储空间状态。 */
    val isLowStorage: Boolean get() = usagePercent > 80
}

