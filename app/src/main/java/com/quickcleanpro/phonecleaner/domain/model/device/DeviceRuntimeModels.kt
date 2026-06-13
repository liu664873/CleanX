package com.quickcleanpro.phonecleaner.domain.model.device

import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

data class BatteryInfo(
    val levelPercent: Int,
    val health: String,
    val temperature: Float,
    val voltage: Int,
    val technology: String,
    val capacity: Int,
    val availableTime: String = "Unknown",
)

data class MemoryInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val isTotalValid: Boolean,
) {
    val formattedTotal: String get() = FileSizeFormatter.format(totalBytes)

    val formattedAvailable: String get() = FileSizeFormatter.format(availableBytes)

    val formattedUsed: String get() = FileSizeFormatter.format(usedBytes)
}

data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
) {
    val formattedTotal: String get() = FileSizeFormatter.format(totalBytes)

    val formattedAvailable: String get() = FileSizeFormatter.format(availableBytes)

    val formattedUsed: String get() = FileSizeFormatter.format(usedBytes)

    val usagePercent: Int get() = if (totalBytes > 0) ((usedBytes.toFloat() / totalBytes) * 100).toInt() else 0

    val isLowStorage: Boolean get() = usagePercent > 80
}
