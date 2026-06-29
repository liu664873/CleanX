package com.quickcleanpro.phonecleaner.presentation.screen.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.ScanResult
import com.quickcleanpro.phonecleaner.domain.state.SharedScanState
import com.quickcleanpro.phonecleaner.domain.usecase.ScanJunkUseCase
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.quickcleanpro.phonecleaner.presentation.common.appString
import com.quickcleanpro.phonecleaner.presentation.common.scanJunkUseCaseOrPreview
import com.quickcleanpro.phonecleaner.presentation.common.sharedScanStateOrPreview

/**
 * 閹殿偅寮挎い鐢告桨 ViewModel閿? *
 * 鐠愮喕鐭楅崥顖氬З娑撶粯绔婚悶鍡樺閹诲繈鈧焦绉风拹鐟板彙娴滎偉绻樻惔锔界ウ閿涘苯鑻熼幎濠冨閹诲繋鑵戦惃鍕潔缁€鐑樻瀮濡楀牆鎷扮€瑰本鍨氱紒鎾寸亯閺佸鎮婃稉铏骨旈敓?UI 閻樿埖鈧緤鎷? */
class ScanViewModel constructor(
    private val scanJunkUseCase: ScanJunkUseCase,
    private val sharedState: SharedScanState,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    constructor() : this(
        scanJunkUseCaseOrPreview(),
        sharedScanStateOrPreview(),
        Dispatchers.IO
    )

    /** 閹殿偅寮挎い鐢告桨闂冭埖顔岄敓?*/
    enum class ScanState {
        Idle,
        Scanning,
        Completed,
        Error
    }

    /** 閹殿偅寮挎い鐢告桨鐎瑰本鏆?UI 閻樿埖鈧緤鎷?*/
    data class ScanUiState(
        val scanState: ScanState = ScanState.Idle,
        val progress: Float = 0f,
        val currentCategory: JunkCategory? = null,
        val foundItemCount: Int = 0,
        val foundTotalSize: Long = 0,
        val formattedFoundSize: String = "0 B",
        val scanningLabel: String = "",
        val scanResult: ScanResult? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(ScanUiState())
    private var progressJob: Job? = null
    private var hasStarted = false

    /** 妞ょ敻娼伴崣顖濐潎鐎电喓娈戦幍顐ｅ伎閻樿埖鈧緤鎷?*/
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    /** 妫ｆ牗顐兼潻娑樺弳妞ょ敻娼伴弮璺烘儙閸斻劍澹傞幓蹇ョ礉闁灝鍘ら柌宥咁槻楠炶泛褰傞崥顖氬З閿?*/
    fun startScanIfNeeded() {
        if (hasStarted) return
        startScanInternal(resetSession = true)
    }

    /** 閹殿偅寮挎径杈Е閸氬酣鍣哥拠鏇礉楠炶埖绔荤粚杞扮瑐娑撯偓鏉烆喕绱扮拠婵囨殶閹诡噯鎷?*/
    fun retryScan() {
        hasStarted = false
        startScanInternal(resetSession = true)
    }

    /** 閹笛嗩攽閹殿偅寮块崥顖氬З濞翠胶鈻奸敓?*/
    private fun startScanInternal(resetSession: Boolean) {
        if (_uiState.value.scanState == ScanState.Scanning) return
        hasStarted = true
        if (resetSession) {
            sharedState.clear()
        }
        observeProgress()
        _uiState.value = ScanUiState(scanState = ScanState.Scanning)

        viewModelScope.launch(ioDispatcher) {
            try {
                val result = scanJunkUseCase()
                progressJob?.cancel()
                _uiState.value = buildUiState(
                    scanState = ScanState.Completed,
                    progress = 100f,
                    currentCategory = null,
                    foundCount = result.totalCount,
                    foundSize = result.totalSize,
                    scanResult = result,
                    errorMessage = null
                )
            } catch (error: Exception) {
                progressJob?.cancel()
                _uiState.value = _uiState.value.copy(
                    scanState = ScanState.Error,
                    errorMessage = error.message
                        ?: appString(R.string.scan_failed)
                )
            }
        }
    }

    /** 閺€鍫曟肠閸忓彉闊╅幍顐ｅ伎鏉╂稑瀹抽敍灞借嫙閸氬本顒為崚浼淬€夐棃銏㈠Ц閹緤鎷?*/
    private fun observeProgress() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            sharedState.scanProgress.collect { progress ->
                val currentResult = sharedState.scanResult.value
                _uiState.value = buildUiState(
                    scanState = ScanState.Scanning,
                    progress = progress.percent,
                    currentCategory = progress.currentCategory,
                    foundCount = currentResult?.totalCount ?: progress.foundCount,
                    foundSize = currentResult?.totalSize ?: progress.foundSize,
                    scanResult = currentResult,
                    errorMessage = null
                )
            }
        }
    }

    /** 缂佺喍绔撮弸鍕紦閹殿偅寮跨仦鏇犮仛閻樿埖鈧緤绱濋柆鍨帳妞ょ敻娼伴崘宥堝殰鐞涘本瀚鹃弬鍥攳閿?*/
    private fun buildUiState(
        scanState: ScanState,
        progress: Float,
        currentCategory: JunkCategory?,
        foundCount: Int,
        foundSize: Long,
        scanResult: ScanResult?,
        errorMessage: String?
    ): ScanUiState =
        ScanUiState(
            scanState = scanState,
            progress = progress,
            currentCategory = currentCategory,
            foundItemCount = foundCount,
            foundTotalSize = foundSize,
            formattedFoundSize = FileSizeFormatter.format(foundSize),
            scanningLabel = "",
            scanResult = scanResult,
            errorMessage = errorMessage
        )

    override fun onCleared() {
        progressJob?.cancel()
        super.onCleared()
    }
}
