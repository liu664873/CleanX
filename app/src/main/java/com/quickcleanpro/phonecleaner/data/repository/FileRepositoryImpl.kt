package com.quickcleanpro.phonecleaner.data.repository

import android.content.Context
import android.content.Intent
import com.quickcleanpro.phonecleaner.data.source.file.FileManagerDataSource
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository

class FileRepositoryImpl(
    context: Context,
) : FileRepository {
    private val appContext = context.applicationContext

    override suspend fun loadImages(): List<ManagedFileItem> = FileManagerDataSource.loadImages(appContext)

    override suspend fun loadVideos(): List<ManagedFileItem> = FileManagerDataSource.loadVideos(appContext)

    override suspend fun loadAudios(): List<ManagedFileItem> = FileManagerDataSource.loadAudios(appContext)

    override suspend fun loadScreenshots(): List<ManagedFileItem> = FileManagerDataSource.loadScreenshots(appContext)

    override suspend fun loadPrivacyImages(): List<ManagedFileItem> = FileManagerDataSource.loadPrivacyImages(appContext)

    override suspend fun loadDocuments(): List<ManagedFileItem> = FileManagerDataSource.loadDocuments(appContext)

    override suspend fun loadLargeFiles(minBytes: Long): List<ManagedFileItem> = FileManagerDataSource.loadLargeFiles(appContext, minBytes)

    override suspend fun loadDuplicateFiles(): List<List<ManagedFileItem>> = FileManagerDataSource.loadDuplicateFiles(appContext)

    override suspend fun loadWhatsAppFiles(): List<ManagedFileItem> = FileManagerDataSource.loadWhatsAppFiles(appContext)

    override suspend fun deleteFiles(items: List<ManagedFileItem>): Long = FileManagerDataSource.deleteFiles(appContext, items)

    override suspend fun removeLocationData(items: List<ManagedFileItem>): Int = FileManagerDataSource.removeLocationData(appContext, items)

    override fun hasAllFilesAccess(): Boolean = FileManagerDataSource.hasAllFilesAccess()

    override fun allFilesAccessIntent(): Intent = FileManagerDataSource.allFilesAccessIntent(appContext)

    override fun allFilesAccessFallbackIntent(): Intent = FileManagerDataSource.allFilesAccessFallbackIntent()
}
