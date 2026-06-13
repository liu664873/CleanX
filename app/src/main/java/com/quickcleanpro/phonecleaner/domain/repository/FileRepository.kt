package com.quickcleanpro.phonecleaner.domain.repository

import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem

/**
 * 鏂囦欢绠＄悊浠撳簱鎺ュ彛銆? *
 * 棰嗗煙灞傞€氳繃璇ユ帴鍙ｆ弿杩?File Manager 闇€瑕佺殑鏁版嵁鑳藉姏锛涘叿浣撶殑 MediaStore銆佹枃浠剁郴缁熸壂鎻? * 鍜岀郴缁熷垹闄ゆ巿鏉冪敱 data 灞傚疄鐜帮紝椤甸潰涓?ViewModel 涓嶇洿鎺ヤ緷璧栧簳灞傚伐鍏峰崟渚嬨€? */
interface FileRepository {
    /** 鍔犺浇璁惧涓殑鍥剧墖鏂囦欢銆?*/
    suspend fun loadImages(): List<ManagedFileItem>

    /** 鍔犺浇璁惧涓殑瑙嗛鏂囦欢銆?*/
    suspend fun loadVideos(): List<ManagedFileItem>

    /** 鍔犺浇璁惧涓殑闊抽鏂囦欢銆?*/
    suspend fun loadAudios(): List<ManagedFileItem>

    /** 鍔犺浇鎴浘鍥剧墖銆?*/
    suspend fun loadScreenshots(): List<ManagedFileItem>

    /** 鍔犺浇鍖呭惈瀹氫綅淇℃伅鐨勯殣绉佸浘鐗囥€?*/
    suspend fun loadPrivacyImages(): List<ManagedFileItem>

    /** 鍔犺浇鏂囨。绫绘枃浠躲€?*/
    suspend fun loadDocuments(): List<ManagedFileItem>

    /** 鍔犺浇瓒呰繃鎸囧畾澶у皬鐨勫ぇ鏂囦欢銆?*/
    suspend fun loadLargeFiles(minBytes: Long = 10L * 1024 * 1024): List<ManagedFileItem>

    /** 鍔犺浇閲嶅鏂囦欢鍒嗙粍锛屾瘡涓唴閮ㄥ垪琛ㄤ唬琛ㄤ竴缁勯噸澶嶆枃浠躲€?*/
    suspend fun loadDuplicateFiles(): List<List<ManagedFileItem>>

    /** 鍔犺浇 WhatsApp 鐩稿叧鍙竻鐞嗘枃浠躲€?*/
    suspend fun loadWhatsAppFiles(): List<ManagedFileItem>

    /** 鍒犻櫎鎸囧畾鏂囦欢骞惰繑鍥為噴鏀剧殑瀛楄妭鏁般€?*/
    suspend fun deleteFiles(items: List<ManagedFileItem>): Long

    /** 绉婚櫎鍥剧墖瀹氫綅淇℃伅骞惰繑鍥炴垚鍔熷鐞嗙殑鏂囦欢鏁伴噺銆?*/
    suspend fun removeLocationData(items: List<ManagedFileItem>): Int

    /** 鍒ゆ柇褰撳墠鏄惁鍏峰 Android 11+ 鎵€鏈夋枃浠惰闂潈闄愩€?*/
    fun hasAllFilesAccess(): Boolean

    /** 杩斿洖褰撳墠搴旂敤鐨勬墍鏈夋枃浠惰闂潈闄愯缃?Intent銆?*/
    fun allFilesAccessIntent(): Intent

    /** 杩斿洖鎵€鏈夋枃浠惰闂潈闄愯缃殑鍏滃簳 Intent銆?*/
    fun allFilesAccessFallbackIntent(): Intent
}
