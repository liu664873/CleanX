package com.quickcleanpro.phonecleaner.presentation.screen.applock

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.source.notification.PersistentNotificationService
import com.quickcleanpro.phonecleaner.domain.model.applock.AppLockApp
import com.quickcleanpro.phonecleaner.domain.repository.AppLockRepository
import com.quickcleanpro.phonecleaner.presentation.common.appLockRepositoryOrPreview
import com.quickcleanpro.phonecleaner.utils.AppLockManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal enum class AppLockPage {
    SelectApps,
    Pin,
    Manage,
    Search,
    Settings
}

internal enum class AppLockPinStep(
    @StringRes val titleRes: Int,
    @StringRes val hintRes: Int
) {
    Create(R.string.create_your_pin, R.string.create_your_pin),
    CreateConfirm(R.string.create_your_pin, R.string.re_enter_pin_to_proceed),
    Verify(R.string.app_lock, R.string.enter_pin_to_use),
    ChangeVerify(R.string.change_pin, R.string.enter_current_pin_to_verify),
    ChangeNew(R.string.change_pin, R.string.enter_your_new_pin),
    ChangeConfirm(R.string.change_pin, R.string.re_enter_pin_to_proceed)
}

internal data class AppLockUiState(
    val page: AppLockPage = AppLockPage.SelectApps,
    val apps: List<AppLockApp> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val pinStep: AppLockPinStep = AppLockPinStep.Create,
    val pinInput: String = "",
    val firstPin: String = "",
    val pinReturnPage: AppLockPage = AppLockPage.Manage,
    @StringRes val pinErrorRes: Int? = null,
    @StringRes val toastRes: Int? = null,
    val monitoringEnabled: Boolean = true,
    val autoLockEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val isPinSet: Boolean = false,
    val overlayPermissionRequired: Boolean = false
) {
    val lockedCount: Int get() = apps.count { it.isLocked }
    val hasSelectedApps: Boolean get() = lockedCount > 0
    val allAppsLocked: Boolean get() = apps.isNotEmpty() && apps.all { it.isLocked }
}

internal class AppLockViewModel(
    application: Application,
    private val repository: AppLockRepository = appLockRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        AppLockUiState(
            page = if (repository.isPinSet()) AppLockPage.Pin else AppLockPage.SelectApps,
            pinStep = if (repository.isPinSet()) AppLockPinStep.Verify else AppLockPinStep.Create,
            monitoringEnabled = repository.isMonitoringEnabled(),
            autoLockEnabled = repository.isAutoLockEnabled(),
            vibrationEnabled = repository.isVibrationEnabled(),
            isPinSet = repository.isPinSet(),
            overlayPermissionRequired = false
        )
    )
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()
    private var completePinJob: Job? = null

    init {
        loadApps()
        syncMonitoringService()
    }

    fun refreshAfterResume() {
        _uiState.update {
            it.copy(
                monitoringEnabled = repository.isMonitoringEnabled(),
                autoLockEnabled = repository.isAutoLockEnabled(),
                vibrationEnabled = repository.isVibrationEnabled(),
                isPinSet = repository.isPinSet(),
                overlayPermissionRequired = shouldPromptOverlayPermission(page = it.page)
            )
        }
        loadApps()
        syncMonitoringService()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openSearch() {
        _uiState.update { it.copy(page = AppLockPage.Search, searchQuery = "") }
    }

    fun openSettings() {
        _uiState.update { it.copy(page = AppLockPage.Settings) }
    }

    fun returnToManage() {
        _uiState.update {
            it.copy(
                page = AppLockPage.Manage,
                searchQuery = "",
                pinInput = "",
                firstPin = "",
                pinErrorRes = null,
                overlayPermissionRequired = false
            )
        }
    }

    fun togglePackage(packageName: String) {
        val current = _uiState.value
        val updatedApps = current.apps.map { app ->
            if (app.packageName == packageName) app.copy(isLocked = !app.isLocked) else app
        }
        _uiState.update { it.copy(apps = sortApps(updatedApps)) }
        if (current.page == AppLockPage.Manage || current.page == AppLockPage.Search) {
            updatedApps.firstOrNull { it.packageName == packageName }?.let {
                repository.setPackageLocked(packageName, it.isLocked)
            }
            syncMonitoringService()
        }
    }

    fun toggleAllApps() {
        val lock = !_uiState.value.allAppsLocked
        val updatedApps = _uiState.value.apps.map { it.copy(isLocked = lock) }
        _uiState.update { it.copy(apps = sortApps(updatedApps)) }
        if (_uiState.value.page == AppLockPage.Manage) {
            repository.setLockedPackages(updatedApps.filter { it.isLocked }.map { it.packageName }.toSet())
            syncMonitoringService()
        }
    }

    fun beginCreatePin() {
        if (!_uiState.value.hasSelectedApps) return
        cancelCompletePinJob()
        _uiState.update {
            it.copy(
                page = AppLockPage.Pin,
                pinStep = AppLockPinStep.Create,
                pinInput = "",
                firstPin = "",
                pinReturnPage = AppLockPage.Manage,
                pinErrorRes = null,
                overlayPermissionRequired = false
            )
        }
    }

    fun consumeOverlayPermissionRequest() {
        _uiState.update { it.copy(overlayPermissionRequired = false) }
    }

    fun startChangePin() {
        cancelCompletePinJob()
        _uiState.update {
            it.copy(
                page = AppLockPage.Pin,
                pinStep = AppLockPinStep.ChangeVerify,
                pinInput = "",
                firstPin = "",
                pinReturnPage = AppLockPage.Settings,
                pinErrorRes = null
            )
        }
    }

    fun addPinDigit(digit: Char) {
        val current = _uiState.value
        if (!digit.isDigit() || current.pinInput.length >= PIN_LENGTH) return
        val nextPin = current.pinInput + digit
        _uiState.update { it.copy(pinInput = nextPin, pinErrorRes = null) }
        if (nextPin.length == PIN_LENGTH) {
            completePinJob?.cancel()
            completePinJob = viewModelScope.launch {
                delay(PIN_COMPLETE_DISPLAY_DELAY_MS)
                if (_uiState.value.pinInput == nextPin) {
                    processCompletePin(nextPin)
                }
                completePinJob = null
            }
        }
    }

    fun removePinDigit() {
        cancelCompletePinJob()
        _uiState.update { it.copy(pinInput = it.pinInput.dropLast(1), pinErrorRes = null) }
    }

    fun leavePinPage() {
        cancelCompletePinJob()
        val current = _uiState.value
        val targetPage = when {
            current.pinStep == AppLockPinStep.Verify && repository.isPinSet() -> AppLockPage.Pin
            current.pinStep == AppLockPinStep.ChangeVerify ||
                current.pinStep == AppLockPinStep.ChangeNew ||
                current.pinStep == AppLockPinStep.ChangeConfirm -> current.pinReturnPage
            repository.isPinSet() -> AppLockPage.Manage
            else -> AppLockPage.SelectApps
        }
        val targetStep = if (targetPage == AppLockPage.Pin) AppLockPinStep.Verify else AppLockPinStep.Create
        _uiState.update {
            it.copy(
                page = targetPage,
                pinStep = targetStep,
                pinInput = "",
                firstPin = "",
                pinErrorRes = null,
                isPinSet = repository.isPinSet(),
                overlayPermissionRequired = false
            )
        }
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        repository.setMonitoringEnabled(enabled)
        _uiState.update { it.copy(monitoringEnabled = enabled) }
        if (enabled) {
            syncMonitoringService()
        } else {
            AppLockManager.disableMonitoring(getApplication<Application>())
        }
    }

    fun setAutoLockEnabled(enabled: Boolean) {
        repository.setAutoLockEnabled(enabled)
        _uiState.update { it.copy(autoLockEnabled = enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        repository.setVibrationEnabled(enabled)
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastRes = null) }
    }

    override fun onCleared() {
        cancelCompletePinJob()
        super.onCleared()
    }

    private fun cancelCompletePinJob() {
        completePinJob?.cancel()
        completePinJob = null
    }

    private fun processCompletePin(pin: String) {
        when (_uiState.value.pinStep) {
            AppLockPinStep.Create -> {
                _uiState.update {
                    it.copy(
                        pinStep = AppLockPinStep.CreateConfirm,
                        pinInput = "",
                        firstPin = pin,
                        pinErrorRes = null
                    )
                }
            }

            AppLockPinStep.CreateConfirm -> {
                if (pin == _uiState.value.firstPin) {
                    val lockedPackages = _uiState.value.apps
                        .filter { it.isLocked }
                        .map { it.packageName }
                        .toSet()
                    repository.savePin(pin)
                    repository.setLockedPackages(lockedPackages)
                    repository.setMonitoringEnabled(true)
                    _uiState.update {
                        it.copy(
                            page = AppLockPage.Manage,
                            pinStep = AppLockPinStep.Create,
                            pinInput = "",
                            firstPin = "",
                            pinErrorRes = null,
                            monitoringEnabled = true,
                            isPinSet = true,
                            overlayPermissionRequired = !repository.hasOverlayPermission()
                        )
                    }
                    loadApps()
                    syncMonitoringService()
                } else {
                    showPinError()
                }
            }

            AppLockPinStep.Verify -> {
                if (repository.verifyPin(pin)) {
                    _uiState.update {
                        it.copy(
                            page = AppLockPage.Manage,
                            pinInput = "",
                            pinErrorRes = null,
                            isPinSet = true,
                            overlayPermissionRequired = !repository.hasOverlayPermission()
                        )
                    }
                    loadApps()
                } else {
                    showPinError()
                }
            }

            AppLockPinStep.ChangeVerify -> {
                if (repository.verifyPin(pin)) {
                    _uiState.update {
                        it.copy(
                            pinStep = AppLockPinStep.ChangeNew,
                            pinInput = "",
                            firstPin = "",
                            pinErrorRes = null
                        )
                    }
                } else {
                    showPinError()
                }
            }

            AppLockPinStep.ChangeNew -> {
                _uiState.update {
                    it.copy(
                        pinStep = AppLockPinStep.ChangeConfirm,
                        pinInput = "",
                        firstPin = pin,
                        pinErrorRes = null
                    )
                }
            }

            AppLockPinStep.ChangeConfirm -> {
                if (pin == _uiState.value.firstPin) {
                    repository.savePin(pin)
                    _uiState.update {
                        it.copy(
                            page = it.pinReturnPage,
                            pinInput = "",
                            firstPin = "",
                            pinErrorRes = null,
                            isPinSet = true,
                            overlayPermissionRequired = false,
                            toastRes = R.string.pin_updated_successfully
                        )
                    }
                } else {
                    showPinError()
                }
            }
        }
    }

    private fun showPinError() {
        _uiState.update {
            it.copy(
                pinInput = "",
                pinErrorRes = R.string.pin_no_match
            )
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            val existingLocks = _uiState.value.apps.associate { it.packageName to it.isLocked }
            _uiState.update { it.copy(isLoading = true) }
            val apps = try {
                withContext(ioDispatcher) { repository.lockableApps() }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update { state -> state.copy(isLoading = false) }
                return@launch
            }
            val mergedApps = if (!repository.isPinSet() && existingLocks.isNotEmpty()) {
                apps.map { app -> existingLocks[app.packageName]?.let { app.copy(isLocked = it) } ?: app }
            } else {
                apps
            }
            _uiState.update { it.copy(apps = sortApps(mergedApps), isLoading = false) }
        }
    }

    private fun sortApps(apps: List<AppLockApp>): List<AppLockApp> =
        apps.sortedWith(
            compareByDescending<AppLockApp> { it.isLocked }
                .thenBy { it.appName.lowercase() }
                .thenBy { it.packageName }
        )

    private fun syncMonitoringService() {
        val context = getApplication<Application>()
        if (repository.isPinSet() &&
            repository.isMonitoringEnabled() &&
            repository.lockedAppCount() > 0 &&
            repository.hasOverlayPermission() &&
            repository.hasUsageAccess()
        ) {
            PersistentNotificationService.enableMonitoring(context)
        } else {
            PersistentNotificationService.disableMonitoring(context)
        }
    }

    private fun shouldPromptOverlayPermission(page: AppLockPage): Boolean =
        page == AppLockPage.Manage &&
            _uiState.value.overlayPermissionRequired &&
            !repository.hasOverlayPermission()

    companion object {
        private const val PIN_LENGTH = 4
        private const val PIN_COMPLETE_DISPLAY_DELAY_MS = 220L
    }
}
