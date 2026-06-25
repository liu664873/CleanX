package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.app.Application
import android.content.ContentValues.TAG
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.config.VariantConfigs
import com.trustlook.sdk.cloudscan.CloudScanClient
import com.trustlook.sdk.cloudscan.CloudScanListener
import com.trustlook.sdk.data.AppInfo
import com.trustlook.sdk.data.Error as TrustlookError
import com.trustlook.sdk.data.Region
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

private const val PROGRESS_TICK_MILLIS = 40L

class VirusScanViewModel constructor(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VirusScanUiState())
    val uiState: StateFlow<VirusScanUiState> = _uiState.asStateFlow()

    private var cloudScanClient: CloudScanClient? = null
    private var completionJob: Job? = null
    private var progressJob: Job? = null
    private var appDisplayJob: Job? = null
    private var pathDisplayJob: Job? = null
    private var appDisplayChannel = Channel<String>(Channel.UNLIMITED)
    private var pathDisplayChannel = Channel<String>(Channel.UNLIMITED)
    private var scanStartedAt: Long = 0L
    private var scanGeneration = 0
    private var isAppDisplayStarted = false
    private var isAppDisplayStopped = false
    private var isPathDisplayStarted = false
    private val processedPackageNames = mutableSetOf<String>()
    private val threatIds = mutableSetOf<String>()
    private val pendingThreatJobs = ConcurrentHashMap.newKeySet<Job>()

    fun startScan(mode: VirusScanMode) {
        resetScanState()
        val generation = scanGeneration
        completionJob = null
        scanStartedAt = System.currentTimeMillis()

        _uiState.value = VirusScanUiState(
            mode = mode,
            isScanning = true,
            currentIcon = getApplication<Application>().getProtectionIcon()
        )
        startTrackProgress(mode, generation)

        refreshAdbRisk()

        if (VariantConfigs.current.trustlookApiKey.isBlank()) {
            handleStartFailure(TrustlookConfigurationException())
            return
        }

        runCatching {
            cloudScanClient = CloudScanClient.Builder(getApplication<Application>())
                .setRegion(Region.INTL)
                .setConnectionTimeout(30_000)
                .setSocketTimeout(30_000)
                .build()

            val listener = createScanListener(mode, generation)
            when (mode) {
                VirusScanMode.Quick -> cloudScanClient?.startQuickScan(listener)
                VirusScanMode.Deep -> cloudScanClient?.startComprehensiveScan(listener)
            }
        }.onFailure { error ->
            handleStartFailure(error)
        }
    }

    fun resetScanState() {
        scanGeneration++
        completionJob?.cancel()
        completionJob = null
        stopTrackProgress()
        resetDisplayQueues()
        pendingThreatJobs.forEach { job -> job.cancel() }
        pendingThreatJobs.clear()
        processedPackageNames.clear()
        threatIds.clear()
        runCatching { cloudScanClient?.cancelScan() }
        cloudScanClient = null
        scanStartedAt = 0L
        _uiState.value = VirusScanUiState()
    }

    fun refreshAdbRisk() {
        viewModelScope.launch(Dispatchers.Default) {
            val hasRisk = hasAdbRisk(getApplication())
            _uiState.update { state -> state.copy(hasAdbRisk = hasRisk) }
        }
    }

    fun clearError() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }

    fun cancelScan() {
        scanGeneration++
        completionJob?.cancel()
        completionJob = null
        stopTrackProgress()
        stopDisplayQueues()
        runCatching { cloudScanClient?.cancelScan() }
        cloudScanClient = null
        scanStartedAt = 0L
        _uiState.update { state ->
            state.copy(isScanning = false, scanCompleted = false)
        }
    }

    fun removeThreatByPackage(packageName: String) {
        _uiState.update { state ->
            val nextThreats = state.threats.filterNot { it.packageName == packageName }
            state.copy(
                threats = nextThreats,
                appThreatCount = nextThreats.count { !it.isFile },
                fileThreatCount = nextThreats.count { it.isFile }
            )
        }
        threatIds.removeAll { it == packageName || it.endsWith(":$packageName") }
    }

    fun removeThreatByFilePath(path: String) {
        _uiState.update { state ->
            val nextThreats = state.threats.filterNot { it.apkPath == path }
            state.copy(
                threats = nextThreats,
                appThreatCount = nextThreats.count { !it.isFile },
                fileThreatCount = nextThreats.count { it.isFile }
            )
        }
        threatIds.remove("file:$path")
    }

    private fun createScanListener(mode: VirusScanMode, generation: Int): CloudScanListener {
        return object : CloudScanListener() {
            override fun onScanStarted() = Unit

            override fun onScanProgress(progress: Int, total: Int, appInfo: AppInfo?) {
                appInfo ?: return
                if (generation == scanGeneration) handleScanProgress(mode, appInfo)
            }

            override fun onScanError(code: Int, message: String?) {
                if (generation != scanGeneration) return
                stopTrackProgress()
                stopDisplayQueues()
                logScanError(code, message)
                _uiState.update { state ->
                    state.copy(
                        isScanning = false,
                        errorMessage = scanErrorMessage(getApplication(), code, message)
                    )
                }
            }

            override fun onScanCanceled() {
                if (generation != scanGeneration) return
                stopTrackProgress()
                stopDisplayQueues()
                _uiState.update { state -> state.copy(isScanning = false) }
            }

            override fun onScanInterrupt() {
                if (generation != scanGeneration) return
                stopTrackProgress()
                stopDisplayQueues()
                _uiState.update { state -> state.copy(isScanning = false) }
            }

            override fun onScanFinished(appList: List<AppInfo?>?) {
                if (generation == scanGeneration) finishWhenUiIsReady(mode, generation)
            }
        }
    }

    private fun handleScanProgress(mode: VirusScanMode, appInfo: AppInfo) {
        val packageName = appInfo.packageName?.takeIf { it.isNotBlank() }
        if (packageName != null) {
            if (appInfo.score >= 6) addThreat(appInfo, isFile = false)
            appDisplayChannel.trySend(packageName)
            return
        }

        if (mode == VirusScanMode.Deep) {
            val path = appInfo.apkPath?.takeIf { it.isNotBlank() }
            if (appInfo.score >= 6) addThreat(appInfo, isFile = true)
            if (path != null) pathDisplayChannel.trySend(path)
        }
    }

    private fun addThreat(appInfo: AppInfo, isFile: Boolean) {
        val job = viewModelScope.launch {
            val threat = withContext(Dispatchers.IO) {
                appInfo.toThreat(getApplication(), isFile)
            }
            if (!threatIds.add(threat.id)) return@launch

            _uiState.update { state ->
                val nextThreats = state.threats + threat
                state.copy(
                    threats = nextThreats,
                    appThreatCount = nextThreats.count { !it.isFile },
                    fileThreatCount = nextThreats.count { it.isFile }
                )
            }
        }
        pendingThreatJobs.add(job)
        job.invokeOnCompletion { pendingThreatJobs.remove(job) }
    }

    private fun finishWhenUiIsReady(mode: VirusScanMode, generation: Int) {
        completionJob?.cancel()
        completionJob = viewModelScope.launch {
            val elapsed = System.currentTimeMillis() - scanStartedAt
            val remaining = (mode.minDurationMillis - elapsed).coerceAtLeast(0L)
            if (remaining > 0L) delay(remaining)
            pendingThreatJobs.toList().joinAll()
            if (generation != scanGeneration) return@launch
            stopTrackProgress()
            stopDisplayQueues()
            _uiState.update { state ->
                state.copy(
                    isScanning = false,
                    scanCompleted = true,
                    progressFraction = 1f
                )
            }
            cloudScanClient = null
        }
    }

    private fun startTrackProgress(mode: VirusScanMode, generation: Int) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                if (generation != scanGeneration) break
                val elapsed = System.currentTimeMillis() - scanStartedAt
                val fraction = (elapsed.toFloat() / mode.minDurationMillis).coerceIn(0f, 1f)
                _uiState.update { state ->
                    if (state.isScanning) {
                        state.copy(progressFraction = maxOf(state.progressFraction, fraction))
                    } else {
                        state
                    }
                }
                handleProgressStageTriggers(mode, fraction, generation)
                if (fraction >= 1f) break
                delay(PROGRESS_TICK_MILLIS)
            }
        }
    }

    private fun stopTrackProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun handleProgressStageTriggers(mode: VirusScanMode, progress: Float, generation: Int) {
        if (progress >= mode.circleStartThreshold(circleIndex = 1)) {
            startAppDisplayConsumer(mode, generation)
        }
        if (mode == VirusScanMode.Quick && progress >= mode.circleFillThreshold(circleIndex = 1)) {
            stopAppDisplayConsumer()
        }
        if (mode == VirusScanMode.Deep && progress >= mode.circleStartThreshold(circleIndex = 2)) {
            stopAppDisplayConsumer()
            enterPathDisplayStage(generation)
            startPathDisplayConsumer(mode, generation)
        }
    }

    private fun handleStartFailure(error: Throwable) {
        if (scanGeneration < 0) return
        stopTrackProgress()
        stopDisplayQueues()
        completionJob?.cancel()
        completionJob = null
        scanStartedAt = 0L
        cloudScanClient = null
        Log.w(TAG, "Trustlook scan start failed", error)
        _uiState.update { state ->
            state.copy(
                isScanning = false,
                errorMessage = scanStartErrorMessage(getApplication(), error)
            )
        }
    }

    private fun startAppDisplayConsumer(mode: VirusScanMode, generation: Int) {
        if (isAppDisplayStarted || isAppDisplayStopped || generation != scanGeneration) return
        isAppDisplayStarted = true
        appDisplayJob = viewModelScope.launch(Dispatchers.Default) {
            var lastUpdateTime = 0L
            for (packageName in appDisplayChannel) {
                if (generation != scanGeneration) break
                if (!processedPackageNames.add(packageName)) continue

                val appLabelAndIcon = withContext(Dispatchers.IO) {
                    getAppLabelAndIcon(getApplication(), packageName)
                }
                delayUntilNextDisplay(lastUpdateTime, mode.displayUpdateIntervalMillis)
                if (generation != scanGeneration) break

                _uiState.update { state ->
                    if (state.isScanning && !state.isPathMode) {
                        state.copy(
                            currentLabel = appLabelAndIcon.first,
                            currentIcon = appLabelAndIcon.second
                        )
                    } else {
                        state
                    }
                }
                lastUpdateTime = System.currentTimeMillis()
            }
        }
    }

    private fun stopAppDisplayConsumer() {
        if (isAppDisplayStopped) return
        isAppDisplayStopped = true
        appDisplayJob?.cancel()
        appDisplayJob = null
        appDisplayChannel.close()
    }

    private fun enterPathDisplayStage(generation: Int) {
        if (isPathDisplayStarted || generation != scanGeneration) return
        _uiState.update { state ->
            if (state.isScanning) {
                state.copy(
                    isPathMode = true,
                    currentLabel = "",
                    currentIcon = getApplication<Application>().getProtectionIcon()
                )
            } else {
                state
            }
        }
    }

    private fun startPathDisplayConsumer(mode: VirusScanMode, generation: Int) {
        if (isPathDisplayStarted || generation != scanGeneration) return
        isPathDisplayStarted = true
        pathDisplayJob = viewModelScope.launch(Dispatchers.Default) {
            var lastUpdateTime = 0L
            for (path in pathDisplayChannel) {
                if (generation != scanGeneration) break
                delayUntilNextDisplay(lastUpdateTime, mode.displayUpdateIntervalMillis)
                if (generation != scanGeneration) break

                _uiState.update { state ->
                    if (state.isScanning && state.isPathMode) {
                        state.copy(currentLabel = path)
                    } else {
                        state
                    }
                }
                lastUpdateTime = System.currentTimeMillis()
            }
        }
    }

    private suspend fun delayUntilNextDisplay(lastUpdateTime: Long, intervalMillis: Long) {
        if (lastUpdateTime == 0L) return
        val elapsed = System.currentTimeMillis() - lastUpdateTime
        if (elapsed < intervalMillis) delay(intervalMillis - elapsed)
    }

    private fun resetDisplayQueues() {
        stopDisplayQueues()
        appDisplayChannel.cancel()
        pathDisplayChannel.cancel()
        appDisplayChannel = Channel(Channel.UNLIMITED)
        pathDisplayChannel = Channel(Channel.UNLIMITED)
        isAppDisplayStarted = false
        isAppDisplayStopped = false
        isPathDisplayStarted = false
    }

    private fun stopDisplayQueues() {
        appDisplayJob?.cancel()
        pathDisplayJob?.cancel()
        appDisplayJob = null
        pathDisplayJob = null
        closeDisplayQueues()
    }

    private fun closeDisplayQueues() {
        appDisplayChannel.close()
        pathDisplayChannel.close()
    }

    override fun onCleared() {
        cancelScan()
        super.onCleared()
    }
}
