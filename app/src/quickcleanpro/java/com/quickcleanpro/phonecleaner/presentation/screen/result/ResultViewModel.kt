package com.quickcleanpro.phonecleaner.presentation.screen.result

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.data.repository.JunkAuthorizedDeleteResult
import com.quickcleanpro.phonecleaner.data.repository.JunkDeleteOutcome
import com.quickcleanpro.phonecleaner.data.repository.JunkFileDeleteHelper
import com.quickcleanpro.phonecleaner.domain.model.CategoryCleanGroup
import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.CleanResult
import com.quickcleanpro.phonecleaner.domain.state.CleanupSummary
import com.quickcleanpro.phonecleaner.domain.state.PendingDeleteAuthorization
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.usecase.MemoryCleanUseCase
import com.quickcleanpro.phonecleaner.domain.model.clean.MemoryCleanResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quickcleanpro.phonecleaner.presentation.common.appString
import com.quickcleanpro.phonecleaner.presentation.common.memoryCleanUseCaseOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.sharedScanStateOrPreview

private const val CLEANING_ANIMATION_MIN_MILLIS = 1_600L

internal val DefaultResultDisplayCategories = listOf(
    JunkCategory.CACHE,
    JunkCategory.TEMP_FILE,
    JunkCategory.RESIDUAL,
    JunkCategory.APK,
    JunkCategory.LARGE_FILE
)

class ResultViewModel constructor(
    private val sharedState: SharedScanState,
    private val memoryCleanUseCase: MemoryCleanUseCase,
    private val ioDispatcher: CoroutineDispatcher,
    private val cleaningAnimationMinMillis: Long = CLEANING_ANIMATION_MIN_MILLIS,
    initialMessage: String = appString(R.string.result_loading)
) : ViewModel() {

    constructor() : this(
        sharedScanStateOrPreview(),
        memoryCleanUseCaseOrPreview(),
        Dispatchers.IO
    )

    sealed class ScreenState {
        data class Preview(
            val groups: List<CategoryCleanGroup>,
            val checkedCount: Int,
            val checkedSize: Long,
            val checkedEmptyCategories: Set<JunkCategory> = emptySet()
        ) : ScreenState()

        data class Cleaning(val freedSpace: Long = 0L) : ScreenState()

        data class AwaitingDeleteAuthorization(
            val deleteRequest: PendingIntent,
            val message: String
        ) : ScreenState()

        data class Completed(
            val result: CleanResult,
            val memoryResult: MemoryCleanResult? = null
        ) : ScreenState()

        data class Error(val message: String) : ScreenState()
    }

    private data class CleaningExecutionState(
        val pendingAuthorizationOutcomes: List<JunkDeleteOutcome> = emptyList(), // 瀵板懏宸块弶鍐畱閸掔娀娅庣紒鎾寸亯
        val directCleanedFiles: List<JunkFile> = emptyList(),                   // 閻╁瓨甯撮幋鎰閸掔娀娅庨惃鍕瀮娴?
        val directFailedFiles: List<JunkFile> = emptyList(),                    // 閻╁瓨甯存径杈Е閻ㄥ嫭鏋冩禒?
        val directFreedSpace: Long = 0L,                                        // 閻╁瓨甯撮柌濠冩杹閻ㄥ嫮鈹栭梻?
        val memoryResult: MemoryCleanResult? = null                             // 閸愬懎鐡ㄥ〒鍛倞缂佹挻鐏?
    )

    // UI 閻樿埖鈧焦绁﹂敍鍫㈩潌閺堝褰查崣姗堢礉閸忣剙绱戦崣顏囶嚢閿?
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Error(initialMessage))
    private val _selectedSummary = MutableStateFlow(SelectionSummary())

    private var cleaningExecutionState = CleaningExecutionState()
    private var checkedEmptyCategories: Set<JunkCategory> = emptySet()

    /** 妞ょ敻娼伴崣顖濐潎鐎电喓娈戠仦鏇犮仛閻樿埖鈧降鈧?*/
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    /** 瑜版挸澧犻柅澶夎厬缂佺喕顓搁敍鍫熸殶闁插繐鎷版径褍鐨敍澶堚偓?*/
    val selectedSummary: StateFlow<SelectionSummary> = _selectedSummary.asStateFlow()

    /**
     * 娴犲骸鍙℃禍顐ｅ閹诲繒濮搁幀浣稿鏉炶姤澹傞幓蹇曠波閺嬫粣绱濋獮鑸电€娲暕鐟欏牆鍨庣紒鍕┾偓?
     */
    fun loadPreview() {
        val scanResult: ScanResult? = sharedState.scanResult.value
        if (scanResult == null) {
            _screenState.value = ScreenState.Error(appString(R.string.result_no_data))
            _selectedSummary.value = SelectionSummary()
            return
        }

        val groups = buildPreviewGroups(scanResult)
        checkedEmptyCategories = defaultEmptyCheckedCategories(groups)
        publishPreview(groups)
    }

    /**
     * 閸掑洦宕查崡鏇氶嚋閺傚洣娆㈤惃鍕偓澶夎厬閻樿埖鈧降鈧?
     * @param categoryIndex 閸掑棛琚崷銊ュ灙鐞涖劋鑵戦惃鍕偍瀵?
     * @param itemIndex 閺傚洣娆㈤崷銊嚉閸掑棛琚稉顓犳畱缁便垹绱?
     */
    fun toggleItemSelection(categoryIndex: Int, itemIndex: Int) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val groups = state.groups.toMutableList()
        val group = groups.getOrNull(categoryIndex) ?: return
        val items = group.items.toMutableList()
        val item = items.getOrNull(itemIndex) ?: return
        items[itemIndex] = item.copy(isChecked = !item.isChecked)
        groups[categoryIndex] = group.copy(items = items)
        checkedEmptyCategories -= group.category
        publishPreview(groups)
    }

    /**
     * 閸掑洦宕查弫缈犻嚋閸掑棛琚惃鍕偓澶夎厬閻樿埖鈧緤绱欓崗銊┾偓?閸忋劋绗夐柅澶涚礆閵?
     * @param categoryIndex 閸掑棛琚槐銏犵穿
     */
    fun toggleCategorySelection(categoryIndex: Int) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val groups = state.groups.toMutableList()
        val group = groups.getOrNull(categoryIndex) ?: return
        if (group.items.isEmpty()) {
            toggleEmptyCategorySelection(group.category, groups)
            return
        }
        val allChecked = group.items.all { it.isChecked }
        groups[categoryIndex] = group.copy(
            items = group.items.map { it.copy(isChecked = !allChecked) }
        )
        checkedEmptyCategories -= group.category
        publishPreview(groups)
    }

    fun toggleCategorySelection(category: JunkCategory) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val categoryIndex = state.groups.indexOfFirst { it.category == category }
        if (categoryIndex >= 0) {
            toggleCategorySelection(categoryIndex)
        } else {
            toggleEmptyCategorySelection(category, state.groups)
        }
    }

    /**
     * 瀵偓婵绔婚悶鍡樼ウ缁嬪鈧?
     * @param context 娑撳﹣绗呴弬鍥风礉閻劋绨崚鐘绘珟閹垮秳缍?
     */
    fun startCleaning(context: Context) {
        val state = _screenState.value as? ScreenState.Preview ?: return
        val selectedItems = state.groups.flatMap { it.items }.filter { it.isChecked }
        if (selectedItems.isEmpty()) {
            _screenState.value = ScreenState.Error(
                if (checkedEmptyCategories.isNotEmpty()) {
                    appString(R.string.result_zero_byte_selection_hint)
                } else {
                    appString(R.string.result_select_at_least_one)
                }
            )
            return
        }

        // 閸掑洦宕查崚鐗堢閻炲棔鑵戦悩鑸碘偓渚婄礉鐠佹澘缍嶅鈧慨瀣闂?
        _screenState.value = ScreenState.Cleaning()
        val cleaningStartedAt = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                // 閸?IO 缁捐法鈻奸幍褑顢戦崚鐘绘珟閹垮秳缍?
                val outcomes = withContext(ioDispatcher) {
                    selectedItems.map { JunkFileDeleteHelper.delete(context, it.junkFile) }
                }
                // 閸掑棛琚崚鐘绘珟缂佹挻鐏?
                val deleted = outcomes.filter { it.deleted }
                val pending = outcomes.filter { !it.deleted && it.authorizationUri != null }
                val failed = outcomes.filter { !it.deleted && it.authorizationUri == null }
                // 閹笛嗩攽閸愬懎鐡ㄥ〒鍛倞
                val memoryResult = withContext(ioDispatcher) { memoryCleanUseCase() }

                // 娣囨繂鐡ㄦ稉顓㈡？閻樿埖鈧?
                cleaningExecutionState = CleaningExecutionState(
                    pendingAuthorizationOutcomes = pending,
                    directCleanedFiles = deleted.map { it.junkFile },
                    directFailedFiles = failed.map { it.junkFile },
                    directFreedSpace = deleted.sumOf { it.freedBytes },
                    memoryResult = memoryResult
                )

                // 婢跺嫮鎮婇棁鈧憰浣洪兇缂佺喐宸块弶鍐畱閸掔娀娅庨敍鍦搉droid 11+ 婵帊缍嬮弬鍥︽閿?
                val uris = JunkFileDeleteHelper.collectAuthorizationUris(pending)
                if (uris.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val request = MediaStore.createDeleteRequest(context.contentResolver, uris)
                    val pendingAuthorization = PendingDeleteAuthorization(
                        request = request,
                        message = context.getString(R.string.result_confirm_system_deletion, uris.size),
                        pendingCount = uris.size
                    )
                    sharedState.setPendingDeleteAuthorization(pendingAuthorization)
                    _screenState.value = ScreenState.AwaitingDeleteAuthorization(
                        deleteRequest = request,
                        message = pendingAuthorization.message
                    )
                } else {
                    // 閺冪娀娓堕幒鍫熸綀閿涘苯娆㈡潻鐔峰З閻㈣鎮楅惄瀛樺复鐎瑰本鍨氬〒鍛倞
                    delayRemainingCleaningAnimation(cleaningStartedAt)
                    finishCleaning(
                        extraCleanedFiles = emptyList(),
                        extraFailedFiles = pending.map { it.junkFile },
                        extraFreedSpace = 0L
                    )
                }
            } catch (error: Exception) {
                _screenState.value = ScreenState.Error(
                    error.message ?: appString(R.string.result_clean_error)
                )
            }
        }
    }

    /**
     * 婢跺嫮鎮婄化鑽ょ埠閸掔娀娅庨幒鍫熸綀缂佹挻鐏夐敍鍫㈡暏閹磋渹绮犵化鑽ょ埠瀵湱鐛ユ潻鏂挎礀閿涘鈧?
     * @param approved true 鐞涖劎銇氶悽銊﹀煕閸氬本鍓伴崚鐘绘珟閿涘畺alse 鐞涖劎銇氶幏鎺旂卜
     */
    fun handleAuthorizationResult(approved: Boolean) {
        val pending = cleaningExecutionState.pendingAuthorizationOutcomes
        if (pending.isEmpty()) return

        // 濞撳懘娅庨崗鍙橀煩閻樿埖鈧椒鑵戦惃鍕窡閹哄牊娼堟穱鈩冧紖
        sharedState.setPendingDeleteAuthorization(null)
        _screenState.value = ScreenState.Cleaning()
        val cleaningStartedAt = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                // 閺嶈宓侀悽銊﹀煕闁瀚ㄩ張鈧紒鍫⑩€樼€规碍宸块弶鍐ㄥ灩闂勩倗绮ㄩ弸?
                val authorizedResult = withContext(ioDispatcher) {
                    if (approved) {
                        JunkFileDeleteHelper.finalizeAuthorizedDeletes(pending)
                    } else {
                        JunkAuthorizedDeleteResult(
                            cleanedFiles = emptyList(),
                            failedFiles = pending.map { it.junkFile },
                            freedBytes = 0L
                        )
                    }
                }
                delayRemainingCleaningAnimation(cleaningStartedAt)
                finishCleaning(
                    extraCleanedFiles = authorizedResult.cleanedFiles,
                    extraFailedFiles = authorizedResult.failedFiles,
                    extraFreedSpace = authorizedResult.freedBytes
                )
            } catch (error: Exception) {
                _screenState.value = ScreenState.Error(
                    error.message ?: appString(R.string.result_clean_error_after_authorization)
                )
            }
        }
    }

    /**
     * 鐎瑰本鍨氬〒鍛倞閿涘苯鎮庨獮鍓佹纯閹恒儲绔婚悶鍡楁嫲閹哄牊娼堝〒鍛倞閻ㄥ嫮绮ㄩ弸婊愮礉楠炶埖娲块弬鏉垮彙娴滎偆濮搁幀浣碘偓?
     */
    private fun finishCleaning(
        extraCleanedFiles: List<JunkFile>,
        extraFailedFiles: List<JunkFile>,
        extraFreedSpace: Long
    ) {
        val cleanedFiles = cleaningExecutionState.directCleanedFiles + extraCleanedFiles
        val failedFiles = cleaningExecutionState.directFailedFiles + extraFailedFiles
        val freedSpace = cleaningExecutionState.directFreedSpace + extraFreedSpace
        val result = CleanResult(
            cleanedFiles = cleanedFiles,
            freedSpace = freedSpace,
            failedFiles = failedFiles
        )
        val memoryResult = cleaningExecutionState.memoryResult

        // 閺囧瓨鏌婇崗鍙橀煩閻樿埖鈧?
        sharedState.removeCleanedFiles(cleanedFiles)
        sharedState.setCleanResult(result)
        if (memoryResult != null) {
            sharedState.setMemoryResult(memoryResult)
        }
        sharedState.setCleanupSummary(
            CleanupSummary(
                freedSpace = result.freedSpace,
                cleanedCount = result.successCount,
                failedCount = result.failedCount,
                memoryFreedBytes = memoryResult?.freedBytes ?: 0L,
                memoryProcessesKilled = memoryResult?.killedCount ?: 0
            )
        )

        // 闁插秶鐤嗛崘鍛村劥閻樿埖鈧?
        cleaningExecutionState = CleaningExecutionState()
        _screenState.value = ScreenState.Completed(result, memoryResult)
    }

    /**
     * 绾喕绻氬〒鍛倞閸斻劎鏁鹃懛鍐茬毌鐏炴洜銇?minimum 閺冨爼鏆遍妴?
     * @param startedAtMillis 濞撳懐鎮婂鈧慨瀣畱閺冨爼妫块幋?
     */
    private suspend fun delayRemainingCleaningAnimation(startedAtMillis: Long) {
        val remainingMillis = cleaningAnimationMinMillis - (System.currentTimeMillis() - startedAtMillis)
        if (remainingMillis > 0L) delay(remainingMillis)
    }

    /**
     * 閸欐垵绔锋０鍕潔閻樿埖鈧礁鑻熼崥灞绢劄闁鑵戠紒鐔活吀閵?
     * @param groups 閸掑棛绮嶉崚妤勩€?
     */
    private fun publishPreview(groups: List<CategoryCleanGroup>) {
        checkedEmptyCategories = checkedEmptyCategories
            .filter { category ->
                groups.firstOrNull { it.category == category }?.items?.isEmpty() ?: true
            }
            .toSet()
        val summary = SelectionSummary(
            checkedCount = groups.sumOf { it.checkedCount },
            checkedSize = groups.sumOf { it.checkedSize },
            checkedEmptyCategoryCount = checkedEmptyCategories.size
        )
        _selectedSummary.value = summary
        _screenState.value = ScreenState.Preview(
            groups = groups,
            checkedCount = summary.checkedCount,
            checkedSize = summary.checkedSize,
            checkedEmptyCategories = checkedEmptyCategories
        )
    }

    private fun toggleEmptyCategorySelection(category: JunkCategory, groups: List<CategoryCleanGroup>) {
        checkedEmptyCategories = if (category in checkedEmptyCategories) {
            checkedEmptyCategories - category
        } else {
            checkedEmptyCategories + category
        }
        publishPreview(groups)
    }

    /**
     * 娴犲孩澹傞幓蹇曠波閺嬫粍鐎娲暕鐟欏牆鍨庣紒鍕┾偓?
     * @param scanResult 閹殿偅寮跨紒鎾寸亯
     * @return 閸掑棛绮嶉崚妤勩€冮敍灞剧槨娑擃亜鍨庣紒鍕瘶閸氼偆琚崚顐㈡嫲濞撳懐鎮婃い鐟板灙鐞?
     */
    private fun buildPreviewGroups(scanResult: ScanResult): List<CategoryCleanGroup> =
        scanResult.categoryBreakdown.map { (category, files) ->
            CategoryCleanGroup(
                category = category,
                items = files.map { CleanItem(junkFile = it, isChecked = true) }
            )
        }

    private fun defaultEmptyCheckedCategories(groups: List<CategoryCleanGroup>): Set<JunkCategory> {
        val groupsByCategory = groups.associateBy { it.category }
        val residualHasFiles = groupsByCategory[JunkCategory.RESIDUAL]?.items?.isNotEmpty() == true ||
            groupsByCategory[JunkCategory.DUPLICATE]?.items?.isNotEmpty() == true
        return DefaultResultDisplayCategories
            .filter { category ->
                when (category) {
                    JunkCategory.RESIDUAL -> !residualHasFiles
                    else -> groupsByCategory[category]?.items?.isEmpty() ?: true
                }
            }
            .toSet()
    }
}

data class SelectionSummary(
    val checkedCount: Int = 0,
    val checkedSize: Long = 0L,
    val checkedEmptyCategoryCount: Int = 0
)
