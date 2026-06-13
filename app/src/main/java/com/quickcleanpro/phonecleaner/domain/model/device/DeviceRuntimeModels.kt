package com.quickcleanpro.phonecleaner.domain.model.device

import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

/**
 * 鐢垫睜鍩虹淇℃伅銆? */
data class BatteryInfo(
    val levelPercent: Int,
    val health: String,
    val temperature: Float,
    val voltage: Int,
    val technology: String,
    val capacity: Int,
    val availableTime: String = "Unknown",
)

/**
 * 鍐呭瓨浣跨敤淇℃伅銆? */
data class MemoryInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usagePercent: Int,
    val isTotalValid: Boolean,
) {
    /** 宸叉牸寮忓寲鐨勬€诲唴瀛樺ぇ灏忋€?*/
    val formattedTotal: String get() = FileSizeFormatter.format(totalBytes)

    /** 宸叉牸寮忓寲鐨勫彲鐢ㄥ唴瀛樺ぇ灏忋€?*/
    val formattedAvailable: String get() = FileSizeFormatter.format(availableBytes)

    /** 宸叉牸寮忓寲鐨勫凡鐢ㄥ唴瀛樺ぇ灏忋€?*/
    val formattedUsed: String get() = FileSizeFormatter.format(usedBytes)
}

/**
 * 瀛樺偍绌洪棿浣跨敤淇℃伅銆? */
data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
) {
    /** 宸叉牸寮忓寲鐨勬€荤┖闂村ぇ灏忋€?*/
    val formattedTotal: String get() = FileSizeFormatter.format(totalBytes)

    /** 宸叉牸寮忓寲鐨勫彲鐢ㄧ┖闂村ぇ灏忋€?*/
    val formattedAvailable: String get() = FileSizeFormatter.format(availableBytes)

    /** 宸叉牸寮忓寲鐨勫凡鐢ㄧ┖闂村ぇ灏忋€?*/
    val formattedUsed: String get() = FileSizeFormatter.format(usedBytes)

    /** 瀛樺偍浣跨敤鐧惧垎姣斻€?*/
    val usagePercent: Int get() = if (totalBytes > 0) ((usedBytes.toFloat() / totalBytes) * 100).toInt() else 0

    /** 鏄惁澶勪簬浣庡瓨鍌ㄧ┖闂寸姸鎬併€?*/
    val isLowStorage: Boolean get() = usagePercent > 80
}
