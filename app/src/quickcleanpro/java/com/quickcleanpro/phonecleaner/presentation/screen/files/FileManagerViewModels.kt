package com.quickcleanpro.phonecleaner.presentation.screen.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
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
import com.quickcleanpro.phonecleaner.presentation.common.appString


private const val FILE_DELETE_ANIMATION_MIN_MILLIS = 2000L

internal enum class FileCollectionKind {
    Photos,           // 閺咁噣鈧氨鍙庨悧?
    Screenshots,      // 閹搭亜娴?
    Videos,           // 鐟欏棝顣?
    Audios,           // 闂婃娊顣?
    SimilarPhotos,    // 閻╅晲鎶€閻撗呭閿涘牊瀵滈惄闀愭妧鎼达箑鍨庣紒鍕剁礆
    PhotoPrivacy,     // 闂呮劗顫嗛悡褏澧栭敍鍫ユ付鐟曚焦绔婚梽銈勭秴缂冾喕淇婇幁顖ょ礆
    LargeFiles,       // 婢堆勬瀮娴?
    Documents         // 閺傚洦銆?
}

internal data class FileCollectionUiState(
    val kind: FileCollectionKind? = null,                     // 瑜版挸澧犻梿鍡楁値缁鐎?
    val phase: PhotosState = PhotosState.Scanning,            // 瑜版挸澧犻梼鑸殿唽閿涘牊澹傞幓蹇嬧偓浣圭セ鐟欏牄鈧礁鍨归梽銈囩搼閿?
    val photoTabs: List<PhotoTabInfo> = emptyList(),          // 閻撗呭閹稿妫╅張鐔峰瀻缂佸嫬鎮楅惃?Tab 閸掓銆?
    val photoConfig: FileCollectionConfig? = null,            // 閻撗呭缁娉﹂崥鍫㈡畱闁板秶鐤嗛敍鍫熺垼妫版ǜ鈧焦鏋冨鍫涒偓浣哥鐏炩偓缁涘绱?
    val managedConfig: ManagedFileListConfig? = null,         // 閺傚洦銆?婢堆勬瀮娴犺泛鍨悰銊ф畱闁板秶鐤?
    val selectedIds: Set<Int> = emptySet(),                   // 瀹告煡鈧鑵戦惃鍕瀮娴?ID 闂嗗棗鎮?
    val selectedTabIndex: Int = 0,                            // 瑜版挸澧犻柅澶夎厬閻ㄥ嫪瀵?Tab 缁便垹绱?
    val selectedMediaTabIndex: Int = 0,                       // 婵帊缍嬮崚鍡欒 Tab 缁便垹绱╅敍鍫濐洤 All/Download/Other閿?
    val detailStartIndex: Int? = null,                        // 鐠囷附鍎忔い鍨ⅵ瀵偓閻ㄥ嫯鎹ｆ慨瀣偍瀵洩绱漬ull 鐞涖劎銇氶張顏呭ⅵ瀵偓
    val deletedBytes: Long = 0L,                              // 閺堫剚顐奸崚鐘绘珟闁插﹥鏂侀惃鍕摟閼哄倹鏆?
    val removedLocationCount: Int = 0,                        // 缁夊娅庢担宥囩枂娣団剝浼呴惃鍕弾閻楀洤绱堕弫甯礄娴犲懐鏁ゆ禍?PhotoPrivacy閿?
    val errorMessage: String? = null                          // 闁挎瑨顕ゆ穱鈩冧紖
) {
    /** 閺勵垰鎯佹稉鐑樻珮闁氨鍙庨悧鍥╄閸ㄥ鈧?*/
    val isPhotos: Boolean get() = kind == FileCollectionKind.Photos

    /** 閺勵垰鎯佹稉娲缁変胶鍙庨悧鍥╄閸ㄥ鈧?*/
    val isPhotoPrivacy: Boolean get() = kind == FileCollectionKind.PhotoPrivacy

    /** 瑜版挸澧犳稉?Tab 娑撳娈戦悡褏澧栭崚妤勩€冮敍鍫滅矌閺咁噣鈧氨鍙庨悧鍥风礆閵?*/
    val currentPhotos: List<PhotoItem>
        get() = photoTabs.getOrNull(selectedTabIndex)?.items.orEmpty()

    /**
     * 鐠囷附鍎忔い纰夌礄婢堆冩禈濞村繗顫嶉敍澶夊▏閻劎娈戞い鍦窗閸掓銆冮妴?
     *
     * 閺嶈宓佹稉宥呮倱閻ㄥ嫬绔风仦鈧猾璇茬€锋潻鏂挎礀鐎电懓绨查惃鍕殶閹诡噯绱?
     * - 閹搭亜娴橀敍姘辨纯閹恒儴绻戦崶?items
     * - 閻╅晲鎶€閻撗呭閿涙艾鐫嶉獮铏閺堝鍨庣紒鍕厬閻ㄥ嫮鍙庨悧?
     * - 婵帊缍嬬純鎴炵壐閿涙碍鐗撮幑?selectedMediaTabIndex 鏉╁洦鎶?
     * - 闂呮劗顫嗛悡褏澧栭敍姘崇箲閸ョ偟鈹栭敍鍫ユ缁変胶鍙庨悧鍥ㄧ梾閺堝顕涢幆鍛搭暕鐟欏牓銆夐敍?
     */
    val collectionDetailItems: List<PhotoItem>
        get() {
            val config = photoConfig ?: return emptyList()
            return when (config.layout) {
                CollectionLayout.Screenshots -> config.items
                CollectionLayout.SimilarPhotos -> config.groups.flatMap { it.items }
                CollectionLayout.MediaGrid -> filterMediaGridItems(
                    config.tabs.getOrNull(selectedMediaTabIndex)?.title.orEmpty(),
                    config.items
                )
                CollectionLayout.PhotoPrivacy -> emptyList()
            }
        }

    /** 閺傚洦銆?婢堆勬瀮娴犺泛缍嬮崜宥呭讲鐟欎胶娈戞い鍦窗閸掓銆冮敍鍫熺壌閹诡噣鈧鑵戦惃?Tab 鏉╁洦鎶ら敍澶堚偓?*/
    val visibleManagedItems: List<ManagedFileUiItem>
        get() {
            val config = managedConfig ?: return emptyList()
            return filterManagedFileUiItems(
                config.tabs.getOrNull(selectedTabIndex)?.title.orEmpty(),
                config.items
            )
        }

    /** 瑜版挸澧犻幍鈧張澶婂讲鐟欎線銆嶉惃?ID 闂嗗棗鎮庨敍鍫㈡暏娴滃骸鍙忛柅澶愨偓鏄忕帆閿涘鈧?*/
    val visibleIds: Set<Int>
        get() = when {
            isPhotos -> currentPhotos.map { it.id }.toSet()
            managedConfig != null -> visibleManagedItems.map { it.id }.toSet()
            photoConfig?.layout == CollectionLayout.MediaGrid -> collectionDetailItems.map { it.id }.toSet()
            else -> photoConfig?.items.orEmpty().map { it.id }.toSet()
        }

    /** 閺勵垰鎯侀崗銊┾偓澶夌啊瑜版挸澧犻幍鈧張澶婂讲鐟欎線銆嶉妴?*/
    val allSelected: Boolean
        get() = visibleIds.isNotEmpty() && selectedIds.containsAll(visibleIds)

    /** 瑜版挸澧犻柅澶夎厬閻ㄥ嫮婀＄€圭偞鏋冩禒璺侯嚠鐠炩€冲灙鐞涱煉绱欓悽銊ょ艾閸掔娀娅庨敍澶堚偓?*/
    val selectedFiles: List<ManagedFileItem>
        get() = when {
            isPhotos -> currentPhotos.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            managedConfig != null -> managedConfig.items.filter { it.id in selectedIds }.mapNotNull { it.realFile }
            else -> photoConfig?.items.orEmpty().filter { it.id in selectedIds }.mapNotNull { it.realFile }
        }

    /** 闁鑵戦惃鍕瀮娴犺埖鈧銇囩亸蹇ョ礄鐎涙濡敍澶堚偓?*/
    val selectedSizeBytes: Long get() = selectedFiles.sumOf { it.sizeBytes }

    /** 闁鑵戦惃鍕瀮娴?Uri 閸掓銆冮敍鍫㈡暏娴滃海閮寸紒鐔峰灩闂勩倖宸块弶鍐跨礆閵?*/
    val selectedUris: List<Uri> get() = selectedFiles.map { it.uri }

    /** 缂佹挻鐏夋い鐢告桨閺勫墽銇氶惃鍕ㄢ偓婊勬殶闁插繆鈧繂鎷伴垾婊冨礋娴ｅ秮鈧繐绱濇笟瀣洤 (12.5, MB) 閹?(5, Photos)閵?*/
    val resultSize: Pair<String, String>
        get() = if (isPhotoPrivacy) {
            removedLocationCount.toString() to "Photos"
        } else {
            FileSizeFormatter.format(deletedBytes).splitSizeLabel()
        }
}

/**
 * 閺傚洣娆㈤梿鍡楁値 ViewModel閿涘牆鐔€缁紮绱氶妴?
 *
 * 鐠愮喕鐭楁径姘鳖潚閺傚洣娆㈢猾璇茬€烽敍鍫㈠弾閻楀洢鈧浇顫嬫０鎴欌偓渚€鐓舵０鎴欌偓浣搞亣閺傚洣娆㈤妴浣规瀮濡楋絿鐡戦敍澶屾畱閹殿偅寮块妴渚€鈧瀚ㄩ妴浣稿灩闂勩倗鐡戦柅姘辨暏闁槒绶妴?
 * 鐎涙劗琚崣顖欎簰缂佈勫楠炶埖澧跨仦鏇犲鐎规俺顢戞稉鎭掆偓?
 */
internal open class FileCollectionViewModel(
    private val repository: FileRepository = fileRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null,
    private val scanDelayMillis: Long = 900L,                     // 閹殿偅寮块崥搴″З閻㈣娆㈡潻?
    private val deleteDelayMillis: Long = FILE_DELETE_ANIMATION_MIN_MILLIS, // 閸掔娀娅庨崝銊ф暰閺堚偓閻厽妞傞梹?
    private val completeDelayMillis: Long = 700L                  // 鐎瑰本鍨氶崝銊ф暰閸氬氦绻橀崗銉х波閺嬫粓銆夐惃鍕鏉?
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

    // -------------------- 閺佺増宓侀崝鐘烘祰 --------------------

    /**
     * 閸旂姾娴囬幐鍥х暰缁鐎烽惃鍕瀮娴犲爼娉﹂崥鍫涒偓?
     *
     * 婵″倹鐏夎ぐ鎾冲瀹告彃濮炴潪鐣屾祲閸氬瞼琚崹瀣╃瑬閺佺増宓佹稉宥勮礋缁岀尨绱濋崚娆戞纯閹恒儴绻戦崶鐑囩礉闁灝鍘ら柌宥咁槻閹殿偅寮块妴?
     */
    fun load(kind: FileCollectionKind) {
        if (_uiState.value.kind == kind && currentItemsLoaded()) return
        loadInternal(kind)
    }

    /** 瀵搫鍩楅崚閿嬫煀瑜版挸澧犻梿鍡楁値閿涘牓鍣搁弬鐗堝閹诲骏绱氶妴?*/
    fun refresh() {
        val kind = _uiState.value.kind ?: return
        loadInternal(kind)
    }

    /** 閸掑洦宕叉稉?Tab閿涘牅绶ユ俊鍌滃弾閻楀洦瀵滈弮銉︽埂閸掑棛绮嶉惃鍕瑝閸?Tab閿涘鈧?*/
    fun selectTab(index: Int) {
        _uiState.update { state ->
            state.copy(
                selectedTabIndex = index.coerceAtLeast(0),
                selectedIds = if (state.isPhotos) emptySet() else state.selectedIds,
                detailStartIndex = null
            )
        }
    }

    /** 閸掑洦宕叉刊鎺嶇秼閸掑棛琚?Tab閿涘牆顩?All/Download/Other閿涘鈧?*/
    fun selectMediaTab(index: Int) {
        _uiState.update {
            it.copy(selectedMediaTabIndex = index.coerceAtLeast(0), detailStartIndex = null)
        }
    }

    // -------------------- 闁瀚ㄩ幙宥勭稊 --------------------

    /** 閸掑洦宕查崡鏇氶嚋閺傚洣娆㈤惃鍕偓澶夎厬閻樿埖鈧降鈧?*/
    fun toggleSelection(id: Int) {
        _uiState.update {
            it.copy(selectedIds = if (id in it.selectedIds) it.selectedIds - id else it.selectedIds + id)
        }
    }

    /** 閹靛綊鍣洪崚鍥ㄥ床閹稿洤鐣?ID 闂嗗棗鎮庨惃鍕偓澶夎厬閻樿埖鈧降鈧?*/
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

    /** 閸忋劑鈧?閸欐牗绉烽崗銊┾偓澶婄秼閸撳秴褰茬憴浣烘畱閹碘偓閺堝鏋冩禒韬测偓?*/
    fun toggleAllVisible() {
        toggleIds(_uiState.value.visibleIds)
    }

    /** 閸掑洦宕查惄闀愭妧閻撗呭閸掑棛绮嶆稉顓熷閺堝鍙庨悧鍥╂畱闁鑵戦悩鑸碘偓浣碘偓?*/
    fun toggleGroup(group: PhotoGroup) {
        toggleIds(group.items.map { it.id }.toSet())
    }

    // -------------------- 鐠囷附鍎忔い鍨付閸?--------------------

    /** 閹垫挸绱戠拠锔藉剰妞ょ绱濋獮鑸靛瘹鐎规俺鎹ｆ慨瀣偍瀵洏鈧?*/
    fun openDetail(index: Int?) {
        _uiState.update { it.copy(detailStartIndex = index?.takeIf { value -> value >= 0 }) }
    }

    /** 閸忔娊妫寸拠锔藉剰妞ょ偣鈧?*/
    fun closeDetail() {
        _uiState.update { it.copy(detailStartIndex = null) }
    }

    // -------------------- 閸掔娀娅庡ù浣衡柤 --------------------

    /** 鏉╂稑鍙嗛崚鐘绘珟绾喛顓婚梼鑸殿唽閿涘牆鑴婇崙铏光€樼拋銈咁嚠鐠囨繃顢嬮敍澶堚偓?*/
    fun requestDelete() {
        if (_uiState.value.selectedIds.isNotEmpty()) {
            _uiState.update { it.copy(phase = PhotosState.ConfirmDelete) }
        }
    }

    /** 閸欐牗绉烽崚鐘绘珟閿涘苯娲栭崚鐗堢セ鐟欏牓妯佸▓鐐光偓?*/
    fun cancelDelete() {
        _uiState.update { it.copy(phase = PhotosState.Browsing) }
    }

    /** 缁崵绮洪崚鐘绘珟閹哄牊娼堢悮顐ｅ珕缂佹繃妞傜拫鍐暏閿涘苯娲栭崚鐗堢セ鐟欏牓妯佸▓鐐光偓?*/
    fun rejectSystemDelete() {
        cancelDelete()
    }

    /** 濞撳懘娅庨柨娆掝嚖娣団剝浼呴妴?*/
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 閹笛嗩攽閸掔娀娅庨幙宥勭稊閿涘牊鍨ㄧ粔濠氭珟娴ｅ秶鐤嗘穱鈩冧紖閿涘鈧?
     *
     * 閺嶈宓佽ぐ鎾冲缁鐎风拫鍐暏娴犳挸绨遍惃鍕嚠鎼存梹鏌熷▔鏇礉楠炶泛婀崚鐘绘珟閸氬孩娲块弬?UI 閻樿埖鈧緤绱濇笟婵囶偧缂佸繐宸婚敍?
     * Deleting 閳?CompleteAnimation 閳?Result閵?
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
                    // 闂呮劗顫嗛悡褏澧栭敍姘毙╅梽銈勭秴缂冾喕淇婇幁顖ょ礉閼板奔绗夐弰顖氬灩闂勩倖鏋冩禒?
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
                    // 閺咁噣鈧碍鏋冩禒璁圭窗閸掔娀娅庨弬鍥︽
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

    /** 娴犲海绮ㄩ弸婊堛€夐悙鐟板毊閳ユ粎鎴风紒顓狀吀閻炲棌鈧繃妞傜拫鍐暏閿涘矁绻戦崶鐐寸セ鐟欏牏濮搁幀浣碘偓?*/
    fun continueManaging() {
        _uiState.update {
            it.copy(
                phase = PhotosState.Browsing,
                selectedIds = defaultSelectedIds(it),
                detailStartIndex = null
            )
        }
    }

    // -------------------- 閸愬懘鍎寸€圭偟骞?--------------------

    /** 閸愬懘鍎撮崝鐘烘祰鐎圭偟骞囬敍宀勫櫢缂冾喚濮搁幀浣歌嫙閸氼垰濮╅崥搴″酱閹殿偅寮块妴?*/
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

    /** 濡偓閺屻儱缍嬮崜宥嗘Ц閸氾箑鍑￠崝鐘烘祰閺佺増宓侀敍鍫㈡暏娴滃酣浼╅崗宥夊櫢婢跺秴濮炴潪鏂ょ礆閵?*/
    private fun currentItemsLoaded(): Boolean {
        val state = _uiState.value
        return state.photoTabs.isNotEmpty() || state.photoConfig != null || state.managedConfig != null
    }

    /** 閺嬪嫬缂撻幐鍥х暰缁鐎烽惃?UI 閻樿埖鈧降鈧?*/
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
                tabTitles = listOf("All", "Download", "Other")
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

    /** 閺嬪嫬缂撴刊鎺嶇秼缁鐎烽敍鍫ｎ潒妫?闂婃娊顣堕敍澶屾畱閻樿埖鈧降鈧?*/
    private fun mediaState(
        kind: FileCollectionKind,
        title: String,
        scanText: String,
        files: List<ManagedFileItem>,
        tabTitles: List<String>
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
                layout = CollectionLayout.MediaGrid,
                items = items,
                tabs = buildMediaTabs(items, tabTitles)
            )
        )
    }

    /** 閺嬪嫬缂撻弬鍥ㄣ€?婢堆勬瀮娴犲墎琚崹瀣畱閻樿埖鈧降鈧?*/
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

    /** 閸掔娀娅庨崥搴ㄥ櫢閺傛澘濮炴潪鑺ユ殶閹诡噯绱濋獮鏈电箽閻ｆ瑥缍嬮崜宥夋▉濞堥潧鎷板鎻掑灩闂勩倗绮虹拋掳鈧?*/
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

    /** 閸掋倖鏌囬崝鐘烘祰閸氬海娈戦悩鑸碘偓浣规Ц閸氾附婀佺€圭偤妾紒鎾寸亯閿涘牓娼粚鐚寸礆閵?*/
    private fun loadedHasResults(state: FileCollectionUiState): Boolean =
        when {
            state.isPhotos -> state.photoTabs.firstOrNull()?.items.orEmpty().isNotEmpty()
            state.managedConfig != null -> state.managedConfig.items.isNotEmpty()
            else -> state.photoConfig?.items.orEmpty().isNotEmpty()
        }

    /** 閼惧嘲褰囩拠銉ц閸ㄥ绮拋銈夆偓澶夎厬閻?ID 闂嗗棗鎮庨敍鍫滅伐婵″倿娈ｇ粔浣哄弾閻楀洭绮拋銈呭弿闁绱氶妴?*/
    private fun defaultSelectedIds(state: FileCollectionUiState): Set<Int> =
        state.photoConfig?.defaultSelectedIds ?: emptySet()

    /** 瀵ゆ儼绻滈幐鍥х暰濮ｎ偆顫楅敍灞筋洤閺?>0閵?*/
    private suspend fun delayIfNeeded(millis: Long) {
        if (millis > 0L) delay(millis)
    }

    /** 閸氼垰濮╅崥搴″酱閸旂姾娴囨禒璇插閿涘牊鏁幐浣圭ゴ鐠囨洘鏁為崗銉ユ倱濮濄儲澧界悰宀嬬礆閵?*/
    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch(ioDispatcher) { block() }
        }
    }

}

// ================== 闁插秴顦查弬鍥︽娑撴挾鏁?ViewModel ==================

/**
 * 闁插秴顦查弬鍥︽妞ょ敻娼伴惃?UI 閻樿埖鈧降鈧?
 */
internal data class DuplicateFilesUiState(
    val phase: PhotosState = PhotosState.Scanning,                     // 瑜版挸澧犻梼鑸殿唽
    val groups: List<DuplicateGroupItem> = emptyList(),                // 闁插秴顦查弬鍥︽閸掑棛绮嶉崚妤勩€?
    val selectedGroupId: Int? = null,                                  // 瑜版挸澧犻幍鎾崇磻閻ㄥ嫬鍨庣紒?ID
    val selectedFileKeys: Set<String> = emptySet(),                    // 闁鑵戦惃鍕瀮娴犺泛鏁稉鈧弽鍥槕
    val deletedBytes: Long = 0L,                                       // 閺堫剚顐奸崚鐘绘珟闁插﹥鏂侀惃鍕摟閼哄倹鏆?
    val errorMessage: String? = null                                   // 闁挎瑨顕ゆ穱鈩冧紖
) {
    /** 瑜版挸澧犻幍鎾崇磻閻ㄥ嫬鍨庣紒鍕嚊閹懌鈧?*/
    val selectedGroup: DuplicateGroupItem?
        get() = groups.firstOrNull { it.id == selectedGroupId }

    /** 閹碘偓閺堝褰查崚鐘绘珟閺傚洣娆㈤惃?key 闂嗗棗鎮庨敍鍫熺槨缂佸嫪绻氶悾娆戭儑娑撯偓娑擃亷绱濋崗鏈电稇閸у洤褰查崚鐘绘珟閿涘鈧?*/
    val allDeleteFileKeys: Set<String>
        get() = groups.flatMap { group -> group.files.drop(1).map(::duplicateFileKey) }.toSet()

    /** 鐎圭偤妾憰浣稿灩闂勩倗娈戦弬鍥︽鐎电钖勯崚妤勩€冮妴?*/
    val filesToDelete: List<ManagedFileItem>
        get() = groups.flatMap { group ->
            group.files
                .filter { duplicateFileKey(it) in selectedFileKeys }
                .mapNotNull { it.realFile }
        }

    /** 闁鑵戦弬鍥︽閻ㄥ嫭鈧銇囩亸蹇嬧偓?*/
    val selectedDeleteSize: Long get() = filesToDelete.sumOf { it.sizeBytes }

    /** 閺勵垰鎯侀崗銊┾偓澶夌啊閹碘偓閺堝褰查崚鐘绘珟閺傚洣娆㈤妴?*/
    val allSelected: Boolean
        get() = allDeleteFileKeys.isNotEmpty() && selectedFileKeys.containsAll(allDeleteFileKeys)

    /** 闁鑵戦弬鍥︽閻?Uri 閸掓銆冮敍鍫㈡暏娴滃海閮寸紒鐔峰灩闂勩倖宸块弶鍐跨礆閵?*/
    val selectedUris: List<Uri> get() = filesToDelete.map { it.uri }
}

/**
 * 闁插秴顦查弬鍥︽缁狅紕鎮婇惃?ViewModel閵?
 *
 * 鐠愮喕鐭楅幍顐ｅ伎闁插秴顦查弬鍥︽閵嗕礁鍨庣紒鍕潔缁€鎭掆偓渚€鈧瀚ㄧ憰浣稿灩闂勩倗娈戦崜顖涙拱閵嗕礁鍨归梽銈嗘惙娴ｆ粎鐡戦妴?
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

    /** 閸掗攱鏌婇柌宥咁槻閺傚洣娆㈤崚妤勩€冮敍鍫ュ櫢閺傜増澹傞幓蹇ョ礆閵?*/
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

    /** 閹垫挸绱戦幐鍥х暰閻ㄥ嫰鍣告径宥嗘瀮娴犺泛鍨庣紒鍕┾偓?*/
    fun openGroup(group: DuplicateGroupItem) {
        _uiState.update { it.copy(selectedGroupId = group.id) }
    }

    /** 閸忔娊妫撮崚鍡欑矋鐠囷附鍎忔い鐐光偓?*/
    fun closeGroup() {
        _uiState.update { it.copy(selectedGroupId = null) }
    }

    /** 閸忋劑鈧?閸欐牗绉烽崗銊┾偓澶嬪閺堝褰查崚鐘绘珟閺傚洣娆㈤妴?*/
    fun toggleAll() {
        _uiState.update {
            it.copy(selectedFileKeys = if (it.allSelected) emptySet() else it.allDeleteFileKeys)
        }
    }

    /** 閸掑洦宕查崡鏇氶嚋闁插秴顦查弬鍥︽閻ㄥ嫰鈧鑵戦悩鑸碘偓浣碘偓?*/
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

    /** 閼奉亜濮╅柅澶夎厬瑜版挸澧犻崚鍡欑矋娑擃參娅庢禍鍡欘儑娑撯偓娑擃亝鏋冩禒璺侯樆閻ㄥ嫭澧嶉張澶嬫瀮娴犺翰鈧?*/
    fun autoSelectCurrentGroup() {
        val group = _uiState.value.selectedGroup ?: return
        val groupKeys = group.files.map(::duplicateFileKey).toSet()
        val deletableGroupKeys = group.files.drop(1).map(::duplicateFileKey).toSet()
        _uiState.update {
            it.copy(selectedFileKeys = (it.selectedFileKeys - groupKeys) + deletableGroupKeys)
        }
    }

    /** 鏉╂稑鍙嗛崚鐘绘珟绾喛顓婚梼鑸殿唽閵?*/
    fun requestDelete() {
        if (_uiState.value.filesToDelete.isNotEmpty()) {
            _uiState.update { it.copy(phase = PhotosState.ConfirmDelete) }
        }
    }

    /** 閸欐牗绉烽崚鐘绘珟閵?*/
    fun cancelDelete() {
        _uiState.update { it.copy(phase = PhotosState.Browsing) }
    }

    /** 缁崵绮洪崚鐘绘珟閹哄牊娼堢悮顐ｅ珕缂佹繃妞傜拫鍐暏閵?*/
    fun rejectSystemDelete() {
        cancelDelete()
    }

    /** 濞撳懘娅庨柨娆掝嚖娣団剝浼呴妴?*/
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** 閹笛嗩攽閸掔娀娅庨幙宥勭稊閵?*/
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

    /** 娴犲海绮ㄩ弸婊堛€夌紒褏鐢荤粻锛勬倞閺冩儼鐨熼悽顭掔礉闁插秶鐤嗛悩鑸碘偓浣碘偓?*/
    fun continueManaging() {
        _uiState.update { it.copy(phase = PhotosState.Browsing, selectedFileKeys = emptySet(), selectedGroupId = null) }
    }

    // -------------------- 閸愬懘鍎存潏鍛И --------------------

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

// ================== 閸忋劌鐪銉ュ徔閸戣姤鏆?==================

/** 閼惧嘲褰囨惔鏃傛暏鐎涙顑佹稉鑼剁カ濠ф劑鈧?*/

private fun fileScanFailedMessage(): String =
    runCatching { appString(R.string.file_scan_failed) }.getOrDefault("File scan failed.")

private fun duplicateScanFailedMessage(): String =
    runCatching { appString(R.string.duplicate_scan_failed) }.getOrDefault("Duplicate file scan failed.")

/** 閸掔娀娅庢径杈Е閺冨墎娈戞妯款吇闁挎瑨顕ゅ☉鍫熶紖閵?*/
private fun deletionFailedMessage(): String =
    runCatching { appString(R.string.deletion_failed) }.getOrDefault("Deletion failed.")
