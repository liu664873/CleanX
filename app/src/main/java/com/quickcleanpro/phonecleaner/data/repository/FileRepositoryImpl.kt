package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.data.source.file.FileManagerDataSource

class FileRepositoryImpl(context: Context) : FileRepository {

    private val appContext = context.applicationContext

    override suspend fun loadImages(): List<ManagedFileItem> =
        FileManagerDataSource.loadImages(appContext)

    /** 鍔犺浇璁惧瑙嗛銆?*/
    override suspend fun loadVideos(): List<ManagedFileItem> =
        FileManagerDataSource.loadVideos(appContext)

    /** 鍔犺浇璁惧闊抽銆?*/
    override suspend fun loadAudios(): List<ManagedFileItem> =
        FileManagerDataSource.loadAudios(appContext)

    /** 鍔犺浇鎴浘鍥剧墖銆?*/
    override suspend fun loadScreenshots(): List<ManagedFileItem> =
        FileManagerDataSource.loadScreenshots(appContext)

    /** 鍔犺浇鍖呭惈瀹氫綅淇℃伅鐨勫浘鐗囥€?*/
    override suspend fun loadPrivacyImages(): List<ManagedFileItem> =
        FileManagerDataSource.loadPrivacyImages(appContext)

    /** 鍔犺浇鏂囨。鏂囦欢銆?*/
    override suspend fun loadDocuments(): List<ManagedFileItem> =
        FileManagerDataSource.loadDocuments(appContext)

    /** 鍔犺浇澶ф枃浠躲€?*/
    override suspend fun loadLargeFiles(minBytes: Long): List<ManagedFileItem> =
        FileManagerDataSource.loadLargeFiles(appContext, minBytes)

    /** 鍔犺浇閲嶅鏂囦欢鍒嗙粍銆?*/
    override suspend fun loadDuplicateFiles(): List<List<ManagedFileItem>> =
        FileManagerDataSource.loadDuplicateFiles(appContext)

    /** 鍔犺浇 WhatsApp 鐩稿叧鏂囦欢銆?*/
    override suspend fun loadWhatsAppFiles(): List<ManagedFileItem> =
        FileManagerDataSource.loadWhatsAppFiles(appContext)

    /** 鍒犻櫎鎸囧畾鏂囦欢銆?*/
    override suspend fun deleteFiles(items: List<ManagedFileItem>): Long =
        FileManagerDataSource.deleteFiles(appContext, items)

    /** 绉婚櫎鍥剧墖瀹氫綅淇℃伅銆?*/
    override suspend fun removeLocationData(items: List<ManagedFileItem>): Int =
        FileManagerDataSource.removeLocationData(appContext, items)

    /** 鍒ゆ柇鏄惁鍏峰鎵€鏈夋枃浠惰闂潈闄愩€?*/
    override fun hasAllFilesAccess(): Boolean =
        FileManagerDataSource.hasAllFilesAccess()

    /** 鍒涘缓褰撳墠搴旂敤鐨勬墍鏈夋枃浠惰闂潈闄?Intent銆?*/
    override fun allFilesAccessIntent(): Intent =
        FileManagerDataSource.allFilesAccessIntent(appContext)

    /** 鍒涘缓鎵€鏈夋枃浠惰闂潈闄愬厹搴?Intent銆?*/
    override fun allFilesAccessFallbackIntent(): Intent =
        FileManagerDataSource.allFilesAccessFallbackIntent()
}
