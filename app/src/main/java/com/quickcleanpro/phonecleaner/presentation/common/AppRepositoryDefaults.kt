package com.quickcleanpro.phonecleaner.presentation.common

import android.content.Intent
import com.quickcleanpro.phonecleaner.domain.model.applock.AppLockApp
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import org.koin.core.context.GlobalContext

internal fun fileRepositoryOrPreview(): FileRepository =
    runCatching { GlobalContext.get().get<FileRepository>() }
        .getOrElse { PreviewFileRepository }

internal fun appLockRepositoryOrPreview(): AppLockRepository =
    runCatching { GlobalContext.get().get<AppLockRepository>() }
        .getOrElse { PreviewAppLockRepository }

private object PreviewFileRepository : FileRepository {
    override suspend fun loadImages(): List<ManagedFileItem> = emptyList()

    override suspend fun loadVideos(): List<ManagedFileItem> = emptyList()

    override suspend fun loadAudios(): List<ManagedFileItem> = emptyList()

    override suspend fun loadScreenshots(): List<ManagedFileItem> = emptyList()

    override suspend fun loadPrivacyImages(): List<ManagedFileItem> = emptyList()

    override suspend fun loadDocuments(): List<ManagedFileItem> = emptyList()

    override suspend fun loadLargeFiles(minBytes: Long): List<ManagedFileItem> = emptyList()

    override suspend fun loadDuplicateFiles(): List<List<ManagedFileItem>> = emptyList()

    override suspend fun loadWhatsAppFiles(): List<ManagedFileItem> = emptyList()

    override suspend fun deleteFiles(items: List<ManagedFileItem>): Long = items.sumOf { it.sizeBytes }

    override suspend fun removeLocationData(items: List<ManagedFileItem>): Int = items.size

    override fun hasAllFilesAccess(): Boolean = true

    override fun allFilesAccessIntent(): Intent = Intent()

    override fun allFilesAccessFallbackIntent(): Intent = Intent()
}

private object PreviewAppLockRepository : AppLockRepository {
    override fun isPinSet(): Boolean = false

    override fun savePin(pin: String) = Unit

    override fun verifyPin(pin: String): Boolean = pin == "1234"

    override fun lockedPackages(): Set<String> = emptySet()

    override fun lockedAppCount(): Int = 0

    override fun isPackageLocked(packageName: String): Boolean = false

    override fun setPackageLocked(
        packageName: String,
        locked: Boolean,
    ) = Unit

    override fun setLockedPackages(packageNames: Set<String>) = Unit

    override suspend fun lockableApps(): List<AppLockApp> =
        listOf(
            AppLockApp("com.preview.mail", "Mail", false),
            AppLockApp("com.preview.photos", "Photos", false),
        )

    override fun isMonitoringEnabled(): Boolean = true

    override fun setMonitoringEnabled(enabled: Boolean) = Unit

    override fun isAutoLockEnabled(): Boolean = false

    override fun setAutoLockEnabled(enabled: Boolean) = Unit

    override fun isVibrationEnabled(): Boolean = true

    override fun setVibrationEnabled(enabled: Boolean) = Unit

    override fun hasUsageAccess(): Boolean = true

    override fun hasOverlayPermission(): Boolean = true

    override fun overlayPermissionIntent(): Intent = Intent()

    override fun handlePackageAdded(packageName: String) = Unit

    override fun handlePackageRemoved(packageName: String) = Unit
}

