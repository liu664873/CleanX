package com.quickcleanpro.phonecleaner.domain.model

/**
 * 鍨冨溇鎵弿缁撴灉銆? *
 * 缁熶竴鎵胯浇鎵弿寰楀埌鐨勬枃浠跺垪琛ㄣ€佹€诲ぇ灏忋€佹€绘暟閲忓拰鎸夊垎绫昏仛鍚堢殑鏄庣粏銆? */
data class ScanResult(
    val junkFiles: List<JunkFile>,
    val totalSize: Long,
    val totalCount: Int,
    val categoryBreakdown: Map<JunkCategory, List<JunkFile>>,
) {
    /** 鏍煎紡鍖栧悗鐨勬€诲ぇ灏忔枃妗堛€?*/
    val formattedTotalSize: String
        get() = JunkFile.formatFileSize(totalSize)

    companion object {
        /** 绌烘壂鎻忕粨鏋溿€?*/
        val EMPTY =
            ScanResult(
                junkFiles = emptyList(),
                totalSize = 0,
                totalCount = 0,
                categoryBreakdown = emptyMap(),
            )
    }
}
