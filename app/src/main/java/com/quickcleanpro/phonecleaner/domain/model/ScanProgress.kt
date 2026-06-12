package com.quickcleanpro.phonecleaner.domain.model

/**
 * 鍨冨溇鎵弿杩涘害鐘舵€併€? *
 * Repository 閫氳繃璇ユā鍨嬪悜鎵弿椤甸潰鎸佺画姹囨姤杩涘害锛? * ViewModel 鍐嶈浆鎹负椤甸潰灞曠ず鐘舵€併€? */
data class ScanProgress(
    val percent: Float = 0f,
    val currentCategory: JunkCategory? = null,
    val foundCount: Int = 0,
    val foundSize: Long = 0
) {
    companion object {
        /** 灏氭湭寮€濮嬫壂鎻忔椂鐨勭┖杩涘害銆?*/
        val IDLE = ScanProgress(0f)

        /** 鎵弿瀹屾垚鍓嶇敤浜庡悎骞舵渶缁堢粨鏋滅殑杩涘害鍓嶇紑銆?*/
        val COMPLETE_PREFIX = ScanProgress(99f)
    }
}
