package com.quickcleanpro.phonecleaner.data.scanner

import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory

/**
 * 鍨冨溇鏂囦欢鎵弿鍣ㄦ帴鍙?- 绛栫暐妯″紡鐨勬牳蹇冩娊璞?
 * 
 * 姣忎竴绉嶅瀮鍦剧被鍨嬪搴斾竴涓叿浣撶殑鎵弿鍣ㄥ疄鐜帮紝瀹炵幇鏈帴鍙?
 * 
 * 鐜版湁鎵弿鍣ㄥ疄鐜帮細
 * - CacheScanner锛氭壂鎻忓簲鐢ㄥ拰绯荤粺缂撳瓨鐩綍
 * - TempFileScanner锛氭壂鎻忎复鏃舵枃浠?.tmp/.log绛?
 * - ApkScanner锛氭壂鎻忓凡涓嬭浇鐨凙PK瀹夎鍖?
 * - DuplicateFileScanner锛氶€氳繃鍝堝笇鍊兼娴嬮噸澶嶆枃浠?
 * 
 * 鎵弿娴佺▼锛歊epository閬嶅巻鎵€鏈塻canner -> 姣忎釜scanner鎵弿鐗瑰畾鐩綍 -> 杩斿洖JunkFile鍒楄〃
 */
interface JunkScanner {

    val category: JunkCategory

    suspend fun scan(): List<JunkFile>

    fun getProgress(): Float
}
