package com.quickcleanpro.phonecleaner.presentation.screen.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.QuickCleanApplication
import com.quickcleanpro.phonecleaner.domain.repository.FileRepository
import com.quickcleanpro.phonecleaner.presentation.common.fileRepositoryOrPreview
import com.quickcleanpro.phonecleaner.domain.model.file.ManagedFileItem
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 删除动画的最小展示时长（毫秒），保证动画不至于因删除太快而闪烁。 */
private const val FILE_DELETE_ANIMATION_MIN_MILLIS = 2000L

/**
 * 文件集合类型枚举。
 *
 * 定义了应用中所有可管理的文件/媒体类型。
 */
internal enum class FileCollectionKind {
    Photos,           // 普通照片
    Screenshots,      // 截图
    Videos,           // 视频
    Audios,           // 音频
    SimilarPhotos,    // 相似照片（按相似度分组）
    PhotoPrivacy,     // 隐私照片（需要清除位置信息）
    LargeFiles,       // 大文件
    Documents         // 文档
}

/**
 * 文件集合页面的 UI 状态。
 *
 * 根据不同的 [kind] 使用不同的字段来存储数据（照片类使用 photoTabs/photoConfig，
 * 大文件/文档使用 managedConfig）。
 */
internal data class FileCollectionUiState(
    val kind: FileCollectionKind? = null,                     // 当前集合类型
    val phase: PhotosState = PhotosState.Scanning,            // 当前阶段（扫描、浏览、删除等）
    val photoTabs: List<PhotoTabInfo> = emptyList(),          // 照片按日期分组后的 Tab 列表
    val photoConfig: FileCollectionConfig? = null,            // 照片类集合的配置（标题、文案、布局等）
    val managedConfig: ManagedFileListConfig? = null,         // 文档/大文件列表的配置
    val selectedIds: Set<Int> = emptySet(),                   // 已选中的文件 ID 集合
    val selectedTabIndex: Int = 0,                            // 当前选中的主 Tab 索引
    val selectedMediaTabIndex: Int = 0,                       // 媒体分类 Tab 索引（如 All/Download/Other）
    val detailStartIndex: Int? = null,                        // 详情页打开的起始索引，null 表示未打开
    val deletedBytes: Long = 0L,                              // 本次删除释放的字节数
    val removedLocationCount: Int = 0,                        // 移除位置信息的照片张数（仅用于 PhotoPrivacy）
    val errorMessage: String? = null                          // 错误信息
) {
    /** 是否为普通照片类型。 */
    val isPhotos: Boolean get() = kind == FileCollectionKind.Photos

    /** 是否为隐私照片类型。 */
    val isPhotoPrivacy: Boolean get() = kind == FileCollectionKind.PhotoPrivacy

    /** 当前主 Tab 下的照片列表（仅普通照片）。 */
    val currentPhotos: List<PhotoItem>
        get() = photoTabs.getOrNull(selectedTabIndex)?.items.orEmpty()

    /**
     * 详情页（大图浏览）使用的项目列表。
     *
     * 根据不同的布局类型返回对应的数据：
     * - 截图：直接返回 items
     * - 相似照片：展平所有分组中的照片
     * - 媒体网格：根据 selectedMediaTabIndex 过滤
     * - 隐私照片：返回空（隐私照片没有详情预览页）
     */
    val collectionDetailItems: List<PhotoItem>
        get() {
            val config = photoConfig ?: return emptyList()
            return when (config.layout) {
                CollectionLayout.Screenshots -> config.items
                CollectionLayout.SimilarPhotos -> config.groups.flatMap { it.items }
                CollectionLayout.MediaGrid, CollectionLayout.AudioList -> filterMediaGridItems(
                    config.tabs.getOrNull(selectedMediaTabIndex)?.title.orEmpty(),
                    config.items
                )
                CollectionLayout.PhotoPrivacy -> emptyList()
            }
        }

    /** 文档/大文件当前可见的项目列表（根据选中的 Tab 过滤）。 */
    val visibleManagedItems: List<ManagedFileUiItem>
        get() {
            val config = managedConfig ?: return emptyList()
            return filterManagedFileUiItems(
                config.tabs.getOrNull(selectedTabIndex)?.title.orEmpty(),
                config.items
            )
        }

    /** 当前所有可见项的 ID 集合（用于全选逻辑）。 */
    val visibleIds: Set<Int>
        get() = when {
            isPhotos -> currentPhotos.map { it.id }.toSet()
            managedConfig != null -> visibleManagedItems.map { it.id }.toSet()
            photoConfig?.layout == CollectionLayout.MediaGrid ||
                photoConfig?.layout == CollectionLayout.AudioList -> collectionDetailItems.map { it.id }.toSet()
            else -> photoConfig?.items.orEmpty().map { it.id }.toSet()
        }

    /** 是否全选了当前所有可见项。 */
    val allSelected: Boolean
        get() = visibleIds.isNotEmpty() && selectedIds.containsAll(visibleIds)

    /** 当前选中的真实文件对象列表（用于删除）。 */
    val selectedFiles: List<ManagedFileItem>
        get() = when {
            isPhotos -> currentPhotos.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            managedConfig != null -> managedConfig.items.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            else -> photoConfig?.items.orEmpty().filter { it.id in selectedIds }.mapNotNull { it.realFile }
        }

    /** 选中的文件总大小（字节）。 */
    val selectedSizeBytes: Long get() = selectedFiles.sumOf { it.sizeBytes }

    /** 选中的文件 Uri 列表（用于系统删除授权）。 */
    val selectedUris: List<Uri> get() = selectedFiles.map { it.uri }

    /** 结果页面显示的“数量”和“单位”，例如 (12.5, MB) 或 (5, Photos)。 */
    val resultSize: Pair<String, String>
        get() = if (isPhotoPrivacy) {
            removedLocationCount.toString() to "Photos"
        } else {
            FileSizeFormatter.format(deletedBytes).splitSizeLabel()
        }
}

/**
 * 文件集合 ViewModel（基类）。
 *
 * 负责多种文件类型（照片、视频、音频、大文件、文档等）的扫描、选择、删除等通用逻辑。
 * 子类可以继承并扩展特定行为。
 */
internal open class FileCollectionViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,                     // 扫描后动画延迟
    private val deleteDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS, // 删除动画最短时长
    private val completeDelayMillis: Long = 700L                  // 完成动画后进入结果页的延迟
) : ViewModel() {

    constructor(
        repository: FileRepository,
        ioDispatcher: CoroutineDispatcher
    ) : this(
        repository = repository,
        ioDispatcher = ioDispatcher,
        testLoader = null,
        scanDelayMillis = 900L,
        deleteDelayMillis = FILE_DELETE_ANIMATION_MIN_MILLIS,
        completeDelayMillis = 700L
    )

    private val _uiState = MutableStateFlow(FileCollectionUiState())
    val uiState: StateFlow<FileCollectionUiState> = _uiState.asStateFlow()

    // -------------------- 数据加载 --------------------

    /**
     * 加载指定类型的文件集合。
     *
     * 如果当前已加载相同类型且数据不为空，则直接返回，避免重复扫描。
     */
    fun load(kind: FileCollectionKind) {
        if (_uiState.value.kind == kind && currentItemsLoaded()) return
        loadInternal(kind)
    }

    /** 强制刷新当前集合（重新扫描）。 */
    fun refresh() {
        val kind = _uiState.value.kind ?: return
        loadInternal(kind)
    }

    /** 切换主 Tab（例如照片按日期分组的不同 Tab）。 */
    fun selectTab(index: Int) {
        _uiState.update { state ->
            state.copy(
                selectedTabIndex = index.coerceAtLeast(0),
                selectedIds = if (state.isPhotos) emptySet() else state.selectedIds,
                detailStartIndex = null
            )
        }
    }

    /** 切换媒体分类 Tab（如 All/Download/Other）。 */
    fun selectMediaTab(index: Int) {
        _uiState.update {
            it.copy(selectedMediaTabIndex = index.coerceAtLeast(0), detailStartIndex = null)
        }
    }

    // -------------------- 选择操作 --------------------

    /** 切换单个文件的选中状态。 */
    fun toggleSelection(id: Int) {
        _uiState.update {
            it.copy(selectedIds = if (id in it.selectedIds) it.selectedIds - id else it.selectedIds + id)
        }
    }

    /** 批量切换指定 ID 集合的选中状态。 */
    fun toggleIds(ids: Set<Int>) {
        if (ids.isEmpty()) return
        _uiState.update {
            it.copy(
                selectedIds = if (it.selectedIds.containsAll(ids)) {
                    it.selectedIds - ids
                } else {
                    it.selectedIds + ids
                }
            )
        }
    }

    /** 全选/取消全选当前可见的所有文件。 */
    fun toggleAllVisible() {
        toggleIds(_uiState.value.visibleIds)
    }

    /** 切换相似照片分组中所有照片的选中状态。 */
    fun toggleGroup(group: PhotoGroup) {
        toggleIds(group.items.map { it.id }.toSet())
    }

    // -------------------- 详情页控制 --------------------

    /** 打开详情页，并指定起始索引。 */
    fun openDetail(index: Int?) {
        _uiState.update { it.copy(detailStartIndex = index?.takeIf { value -> value >= 0 }) }
    }

    /** 关闭详情页。 */
    fun closeDetail() {
        _uiState.update { it.copy(detailStartIndex = null) }
    }

    // -------------------- 删除流程 --------------------

    /** 进入删除确认阶段（弹出确认对话框）。 */
    fun requestDelete() {
        if (_uiState.value.selectedIds.isNotEmpty()) {
            _uiState.update { it.copy(phase = PhotosState.ConfirmDelete) }
        }
    }

    /** 取消删除，回到浏览阶段。 */
    fun cancelDelete() {
        _uiState.update { it.copy(phase = PhotosState.Browsing) }
    }

    /** 系统删除授权被拒绝时调用，回到浏览阶段。 */
    fun rejectSystemDelete() {
        cancelDelete()
    }

    /** 清除错误信息。 */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 执行删除操作（或移除位置信息）。
     *
     * 根据当前类型调用仓库的对应方法，并在删除后更新 UI 状态，依次经历：
     * Deleting → CompleteAnimation → Result。
     */
    fun deleteSelectedFiles() {
        val state = _uiState.value
        val selectedFiles = state.selectedFiles
        if (selectedFiles.isEmpty()) {
            _uiState.update { it.copy(phase = PhotosState.Browsing) }
            return
        }

        val kind = state.kind ?: return
        _uiState.update { it.copy(phase = PhotosState.Deleting, detailStartIndex = null) }
        launchLoad {
            runCatching {
                if (state.isPhotoPrivacy) {
                    // 隐私照片：移除位置信息，而不是删除文件
                    val removed = repository.removeLocationData(selectedFiles)
                    delayIfNeeded(deleteDelayMillis)
                    rebuildAfterMutation(kind)
                    _uiState.update {
                        it.copy(
                            phase = PhotosState.CompleteAnimation,
                            selectedIds = emptySet(),
                            removedLocationCount = removed
                        )
                    }
                } else {
                    // 普通文件：删除文件
                    val freedBytes = repository.deleteFiles(selectedFiles)
                    if (freedBytes <= 0L) {
                        error(deletionFailedMessage())
                    }
                    delayIfNeeded(deleteDelayMillis)
                    rebuildAfterMutation(kind)
                    _uiState.update {
                        it.copy(
                            phase = PhotosState.CompleteAnimation,
                            selectedIds = emptySet(),
                            deletedBytes = freedBytes
                        )
                    }
                }
                delayIfNeeded(completeDelayMillis)
                _uiState.update { it.copy(phase = PhotosState.Result) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        phase = PhotosState.Browsing,
                        errorMessage = error.message ?: appString(R.string.deletion_failed)
                    )
                }
            }
        }
    }

    /** 从结果页点击“继续管理”时调用，返回浏览状态。 */
    fun continueManaging() {
        _uiState.update {
            it.copy(
                phase = PhotosState.Browsing,
                selectedIds = defaultSelectedIds(it),
                detailStartIndex = null
            )
        }
    }

    // -------------------- 内部实现 --------------------

    /** 内部加载实现，重置状态并启动后台扫描。 */
    private fun loadInternal(kind: FileCollectionKind) {
        _uiState.value = FileCollectionUiState(kind = kind, phase = PhotosState.Scanning)
        launchLoad {
            runCatching { buildState(kind) }
                .onSuccess { loaded ->
                    delayIfNeeded(scanDelayMillis)
                    _uiState.value = loaded.copy(
                        phase = if (loadedHasResults(loaded)) PhotosState.Browsing else PhotosState.NoResults
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            phase = PhotosState.NoResults,
                            errorMessage = error.message ?: fileScanFailedMessage()
                        )
                    }
                }
        }
    }

    /** 检查当前是否已加载数据（用于避免重复加载）。 */
    private fun currentItemsLoaded(): Boolean {
        val state = _uiState.value
        return state.photoTabs.isNotEmpty() || state.photoConfig != null || state.managedConfig != null
    }

    /** 构建指定类型的 UI 状态。 */
    private suspend fun buildState(kind: FileCollectionKind): FileCollectionUiState =
        if (!hasAllFilesAccess()) {
            FileCollectionUiState(
                kind = kind,
                errorMessage = fileScanFailedMessage()
            )
        } else when (kind) {
            FileCollectionKind.Photos -> {
                val items = mapPhotoItems(repository.loadImages())
                FileCollectionUiState(
                    kind = kind,
                    photoTabs = buildPhotoTabs(items)
                )
            }
            FileCollectionKind.Screenshots -> {
                val items = mapPhotoItems(repository.loadScreenshots())
                val resultSize = items.totalPhotoSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
                FileCollectionUiState(
                    kind = kind,
                    photoConfig = FileCollectionConfig(
                        title = "Screenshots",
                        scanText = "Scanning screenshots...",
                        actionText = "Delete",
                        processingText = "Cleanup Completed...",
                        resultAmount = resultSize.first,
                        resultUnit = resultSize.second,
                        resultCaption = "Deleted in this cleanup",
                        layout = CollectionLayout.Screenshots,
                        items = items
                    )
                )
            }
            FileCollectionKind.Videos -> mediaState(
                kind = kind,
                title = "Videos",
                scanText = "Scanning videos...",
                files = repository.loadVideos(),
                tabTitles = listOf("All", "DCIM", "Download")
            )
            FileCollectionKind.Audios -> mediaState(
                kind = kind,
                title = "Audios",
                scanText = "Scanning audios...",
                files = repository.loadAudios(),
                tabTitles = listOf("All", "Music"),
                layout = CollectionLayout.AudioList
            )
            FileCollectionKind.SimilarPhotos -> {
                val source = mapPhotoItems(repository.loadImages())
                val groups = buildSimilarPhotoGroups(source)
                val resultSize = source.totalPhotoSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
                FileCollectionUiState(
                    kind = kind,
                    photoConfig = FileCollectionConfig(
                        title = "Similar Photos",
                        scanText = "Scanning similar photos...",
                        actionText = "Delete",
                        processingText = "Cleanup Completed...",
                        resultAmount = resultSize.first,
                        resultUnit = resultSize.second,
                        resultCaption = "Deleted in this cleanup",
                        layout = CollectionLayout.SimilarPhotos,
                        items = groups.flatMap { it.items },
                        groups = groups
                    )
                )
            }
            FileCollectionKind.PhotoPrivacy -> {
                val items = mapPhotoItems(repository.loadPrivacyImages())
                val selectedIds = items.map { it.id }.toSet()
                FileCollectionUiState(
                    kind = kind,
                    photoConfig = FileCollectionConfig(
                        title = "Photo Privacy",
                        scanText = "Scanning photo privacy...",
                        actionText = "Remove Location Data",
                        processingText = "Removing Location Data...",
                        resultAmount = items.size.toString(),
                        resultUnit = "Photos",
                        resultCaption = "Location data removed",
                        layout = CollectionLayout.PhotoPrivacy,
                        items = items,
                        defaultSelectedIds = selectedIds
                    ),
                    selectedIds = selectedIds
                )
            }
            FileCollectionKind.LargeFiles -> managedState(
                kind = kind,
                title = "Large Files",
                scanText = "Scanning large files...",
                files = repository.loadLargeFiles(),
                style = ManagedFileListStyle.Default
            )
            FileCollectionKind.Documents -> managedState(
                kind = kind,
                title = "Documents",
                scanText = "Scanning documents...",
                files = repository.loadDocuments(),
                style = ManagedFileListStyle.Documents
            )
        }

    private fun hasAllFilesAccess(): Boolean =
        runCatching { repository.hasAllFilesAccess() }.getOrDefault(false)

    /** 构建媒体类型（视频/音频）的状态。 */
    private fun mediaState(
        kind: FileCollectionKind,
        title: String,
        scanText: String,
        files: List<ManagedFileItem>,
        tabTitles: List<String>,
        layout: CollectionLayout = CollectionLayout.MediaGrid
    ): FileCollectionUiState {
        val items = mapPhotoItems(files)
        val resultSize = items.totalPhotoSizeLabel()?.splitSizeLabel() ?: ("0" to "B")
        return FileCollectionUiState(
            kind = kind,
            photoConfig = FileCollectionConfig(
                title = title,
                scanText = scanText,
                actionText = "Delete",
                processingText = "Cleanup Completed...",
                resultAmount = resultSize.first,
                resultUnit = resultSize.second,
                resultCaption = "Deleted in this cleanup",
                layout = layout,
                items = items,
                tabs = buildMediaTabs(items, tabTitles)
            )
        )
    }

    /** 构建文档/大文件类型的状态。 */
    private fun managedState(
        kind: FileCollectionKind,
        title: String,
        scanText: String,
        files: List<ManagedFileItem>,
        style: ManagedFileListStyle
    ): FileCollectionUiState {
        val items = mapManagedFileUiItems(files)
        val resultSize = items.totalManagedSizeLabel().splitSizeLabel()
        return FileCollectionUiState(
            kind = kind,
            managedConfig = ManagedFileListConfig(
                title = title,
                scanText = scanText,
                tabs = buildManagedFileTabs(items, listOf("All", "Download", "Other")),
                items = items,
                resultAmount = resultSize.first,
                resultUnit = resultSize.second,
                style = style
            )
        )
    }

    /** 删除后重新加载数据，并保留当前阶段和已删除统计。 */
    private suspend fun rebuildAfterMutation(kind: FileCollectionKind) {
        val rebuilt = buildState(kind)
        _uiState.update {
            rebuilt.copy(
                phase = it.phase,
                deletedBytes = it.deletedBytes,
                removedLocationCount = it.removedLocationCount
            )
        }
    }

    /** 判断加载后的状态是否有实际结果（非空）。 */
    private fun loadedHasResults(state: FileCollectionUiState): Boolean =
        when {
            state.isPhotos -> state.photoTabs.firstOrNull()?.items.orEmpty().isNotEmpty()
            state.managedConfig != null -> state.managedConfig.items.isNotEmpty()
            else -> state.photoConfig?.items.orEmpty().isNotEmpty()
        }

    /** 获取该类型默认选中的 ID 集合（例如隐私照片默认全选）。 */
    private fun defaultSelectedIds(state: FileCollectionUiState): Set<Int> =
        state.photoConfig?.defaultSelectedIds ?: emptySet()

    /** 延迟指定毫秒，如果 >0。 */
    private suspend fun delayIfNeeded(millis: Long) {
        if (millis > 0L) delay(millis)
    }

    /** 启动后台加载任务（支持测试注入同步执行）。 */
    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch(ioDispatcher) { block() }
        }
    }

}

// ================== 重复文件专用 ViewModel ==================

/**
 * 重复文件页面的 UI 状态。
 */
internal data class DuplicateFilesUiState(
    val phase: PhotosState = PhotosState.Scanning,                     // 当前阶段
    val groups: List<DuplicateGroupItem> = emptyList(),                // 重复文件分组列表
    val selectedGroupId: Int? = null,                                  // 当前打开的分组 ID
    val selectedFileKeys: Set<String> = emptySet(),                    // 选中的文件唯一标识
    val deletedBytes: Long = 0L,                                       // 本次删除释放的字节数
    val errorMessage: String? = null                                   // 错误信息
) {
    /** 当前打开的分组详情。 */
    val selectedGroup: DuplicateGroupItem?
        get() = groups.firstOrNull { it.id == selectedGroupId }

    /** 所有可删除文件的 key 集合（每组保留第一个，其余均可删除）。 */
    val allDeleteFileKeys: Set<String>
        get() = groups.flatMap { group -> group.files.drop(1).map(::duplicateFileKey) }.toSet()

    /** 实际要删除的文件对象列表。 */
    val filesToDelete: List<ManagedFileItem>
        get() = groups.flatMap { group ->
            group.files
                .filter { duplicateFileKey(it) in selectedFileKeys }
                .mapNotNull { it.realFile }
        }

    /** 选中文件的总大小。 */
    val selectedDeleteSize: Long get() = filesToDelete.sumOf { it.sizeBytes }

    /** 是否全选了所有可删除文件。 */
    val allSelected: Boolean
        get() = allDeleteFileKeys.isNotEmpty() && selectedFileKeys.containsAll(allDeleteFileKeys)

    /** 选中文件的 Uri 列表（用于系统删除授权）。 */
    val selectedUris: List<Uri> get() = filesToDelete.map { it.uri }
}

/**
 * 重复文件管理的 ViewModel。
 *
 * 负责扫描重复文件、分组展示、选择要删除的副本、删除操作等。
 */
internal class DuplicateFilesViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,
    private val deleteDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS,
    private val completeDelayMillis: Long = 700L
) : ViewModel() {

    constructor(
        repository: FileRepository,
        ioDispatcher: CoroutineDispatcher
    ) : this(
        repository = repository,
        ioDispatcher = ioDispatcher,
        testLoader = null,
        scanDelayMillis = 900L,
        deleteDelayMillis = FILE_DELETE_ANIMATION_MIN_MILLIS,
        completeDelayMillis = 700L
    )

    private val _uiState = MutableStateFlow(DuplicateFilesUiState())
    val uiState: StateFlow<DuplicateFilesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** 刷新重复文件列表（重新扫描）。 */
    fun refresh() {
        _uiState.value = DuplicateFilesUiState(phase = PhotosState.Scanning)
        launchLoad {
            runCatching {
                if (!hasAllFilesAccess()) return@runCatching emptyList()
                mapDuplicateGroups(repository.loadDuplicateFiles())
            }
                .onSuccess { groups ->
                    delayIfNeeded(scanDelayMillis)
                    _uiState.value = DuplicateFilesUiState(
                        phase = if (groups.isEmpty()) PhotosState.NoResults else PhotosState.Browsing,
                        groups = groups,
                        selectedFileKeys = groups.flatMap { it.files.drop(1).map(::duplicateFileKey) }.toSet()
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            phase = PhotosState.NoResults,
                            errorMessage = error.message ?: duplicateScanFailedMessage()
                        )
                    }
                }
        }
    }

    /** 打开指定的重复文件分组。 */
    fun openGroup(group: DuplicateGroupItem) {
        _uiState.update { it.copy(selectedGroupId = group.id) }
    }

    /** 关闭分组详情页。 */
    fun closeGroup() {
        _uiState.update { it.copy(selectedGroupId = null) }
    }

    /** 全选/取消全选所有可删除文件。 */
    fun toggleAll() {
        _uiState.update {
            it.copy(selectedFileKeys = if (it.allSelected) emptySet() else it.allDeleteFileKeys)
        }
    }

    /** 切换单个重复文件的选中状态。 */
    fun toggleFile(file: DuplicateFileEntry) {
        val key = duplicateFileKey(file)
        _uiState.update {
            it.copy(
                selectedFileKeys = if (key in it.selectedFileKeys) {
                    it.selectedFileKeys - key
                } else {
                    it.selectedFileKeys + key
                }
            )
        }
    }

    /** 自动选中当前分组中除了第一个文件外的所有文件。 */
    fun autoSelectCurrentGroup() {
        val group = _uiState.value.selectedGroup ?: return
        val groupKeys = group.files.map(::duplicateFileKey).toSet()
        val deletableGroupKeys = group.files.drop(1).map(::duplicateFileKey).toSet()
        _uiState.update {
            it.copy(selectedFileKeys = (it.selectedFileKeys - groupKeys) + deletableGroupKeys)
        }
    }

    fun toggleCurrentGroupSelection() {
        val group = _uiState.value.selectedGroup ?: return
        val groupKeys = group.files.map(::duplicateFileKey).toSet()
        val deletableGroupKeys = group.files.drop(1).map(::duplicateFileKey).toSet()
        _uiState.update {
            val hasSelectedInGroup = it.selectedFileKeys.any { key -> key in groupKeys }
            it.copy(
                selectedFileKeys = if (hasSelectedInGroup) {
                    it.selectedFileKeys - groupKeys
                } else {
                    it.selectedFileKeys + deletableGroupKeys
                }
            )
        }
    }

    /** 进入删除确认阶段。 */
    fun requestDelete() {
        if (_uiState.value.filesToDelete.isNotEmpty()) {
            _uiState.update { it.copy(phase = PhotosState.ConfirmDelete) }
        }
    }

    /** 取消删除。 */
    fun cancelDelete() {
        _uiState.update { it.copy(phase = PhotosState.Browsing) }
    }

    /** 系统删除授权被拒绝时调用。 */
    fun rejectSystemDelete() {
        cancelDelete()
    }

    /** 清除错误信息。 */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** 执行删除操作。 */
    fun deleteSelectedFiles() {
        val selectedFiles = _uiState.value.filesToDelete
        if (selectedFiles.isEmpty()) {
            _uiState.update { it.copy(phase = PhotosState.Browsing) }
            return
        }

        _uiState.update { it.copy(phase = PhotosState.Deleting, selectedGroupId = null) }
        launchLoad {
            runCatching {
                val freedBytes = repository.deleteFiles(selectedFiles)
                if (freedBytes <= 0L) {
                    error(deletionFailedMessage())
                }
                delayIfNeeded(deleteDelayMillis)
                if (!hasAllFilesAccess()) {
                    error(duplicateScanFailedMessage())
                }
                val groups = mapDuplicateGroups(repository.loadDuplicateFiles())
                _uiState.update {
                    it.copy(
                        phase = PhotosState.CompleteAnimation,
                        groups = groups,
                        selectedGroupId = null,
                        selectedFileKeys = emptySet(),
                        deletedBytes = freedBytes
                    )
                }
                delayIfNeeded(completeDelayMillis)
                _uiState.update { it.copy(phase = PhotosState.Result) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        phase = PhotosState.Browsing,
                        errorMessage = error.message ?: appString(R.string.deletion_failed)
                    )
                }
            }
        }
    }

    /** 从结果页继续管理时调用，重置状态。 */
    fun continueManaging() {
        _uiState.update { it.copy(phase = PhotosState.Browsing, selectedFileKeys = emptySet(), selectedGroupId = null) }
    }

    // -------------------- 内部辅助 --------------------

    private suspend fun delayIfNeeded(millis: Long) {
        if (millis > 0L) delay(millis)
    }

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch(ioDispatcher) { block() }
        }
    }

    private fun hasAllFilesAccess(): Boolean =
        runCatching { repository.hasAllFilesAccess() }.getOrDefault(false)
}

// ================== 全局工具函数 ==================

/** 获取应用字符串资源。 */
private fun appString(resId: Int): String = QuickCleanApplication.instance.getString(resId)

private fun fileScanFailedMessage(): String =
    runCatching { appString(R.string.file_scan_failed) }.getOrDefault("File scan failed.")

private fun duplicateScanFailedMessage(): String =
    runCatching { appString(R.string.duplicate_scan_failed) }.getOrDefault("Duplicate file scan failed.")

/** 删除失败时的默认错误消息。 */
private fun deletionFailedMessage(): String =
    runCatching { appString(R.string.deletion_failed) }.getOrDefault("Deletion failed.")
