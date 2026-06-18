package com.quickcleanpro.phonecleaner.presentation.screen.tools.common.notification

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.QuickCleanApplication
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import com.quickcleanpro.phonecleaner.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NotificationBarPage {
    Onboarding,
    Scanning,
    Status,
    Settings,
}

data class NotificationBarUiState(
    val hasAccess: Boolean = false,
    val enabled: Boolean = false,
    val blockedCount: Int = 0,
    val blockedCountsByPackage: Map<String, Int> = emptyMap(),
    val apps: List<BlockableNotificationApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val page: NotificationBarPage = NotificationBarPage.Onboarding,
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val errorMessage: String? = null,
)

open class NotificationBarViewModel(
    private val repository: NotificationRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationBarUiState())
    private var hasShownAccessScan = false

    val uiState: StateFlow<NotificationBarUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch(ioDispatcher) {
            runCatching { readSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update { state ->
                        if (!snapshot.hasAccess) {
                            hasShownAccessScan = false
                        }
                        val nextPage =
                            when {
                                state.page == NotificationBarPage.Settings -> NotificationBarPage.Settings
                                state.page == NotificationBarPage.Scanning -> NotificationBarPage.Scanning
                                snapshot.hasAccess && !hasShownAccessScan -> {
                                    hasShownAccessScan = true
                                    NotificationBarPage.Scanning
                                }
                                snapshot.hasAccess -> NotificationBarPage.Status
                                else -> NotificationBarPage.Onboarding
                            }
                        state.copy(
                            hasAccess = snapshot.hasAccess,
                            enabled = snapshot.enabled,
                            blockedCount = snapshot.blockedCount,
                            blockedCountsByPackage = snapshot.blockedCountsByPackage,
                            apps = snapshot.apps,
                            selectedPackages = snapshot.selectedPackages,
                            page = nextPage,
                            isLoading = false,
                            isInitialized = true,
                            errorMessage = null,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isInitialized = true,
                            errorMessage = error.message ?: appString(R.string.notification_status_load_failed),
                        )
                    }
                }
        }
    }

    fun setBlockingEnabled(checked: Boolean): Boolean {
        val hasAccess = repository.hasNotificationListenerAccess()
        if (checked && !hasAccess) {
            _uiState.update {
                it.copy(hasAccess = false, enabled = false, isLoading = false)
            }
            return true
        }

        repository.setNotificationBlockingEnabled(checked)
        refreshState()
        return false
    }

    fun togglePackage(packageName: String) {
        val selected = packageName !in _uiState.value.selectedPackages
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                repository.setNotificationPackageSelected(packageName, selected)
                repository.selectedNotificationPackages()
            }.onSuccess { selectedPackages ->
                _uiState.update { it.copy(selectedPackages = selectedPackages, errorMessage = null) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: appString(R.string.notification_app_selection_update_failed))
                }
            }
        }
    }

    fun showSettings() {
        _uiState.update { it.copy(page = NotificationBarPage.Settings) }
    }

    fun leaveSettings() {
        _uiState.update { it.copy(page = NotificationBarPage.Status) }
    }

    fun finishScanning() {
        _uiState.update {
            if (it.page == NotificationBarPage.Scanning) {
                it.copy(page = NotificationBarPage.Status)
            } else {
                it
            }
        }
    }

    fun notificationListenerSettingsIntent(): Intent = repository.notificationListenerSettingsIntent()

    fun appDetailsSettingsIntent(packageName: String): Intent = repository.appDetailsSettingsIntent(packageName)

    private fun readSnapshot(): NotificationBarSnapshot =
        NotificationBarSnapshot(
            hasAccess = repository.hasNotificationListenerAccess(),
            enabled = repository.isNotificationBlockingEnabled(),
            blockedCount = repository.blockedNotificationCount(),
            blockedCountsByPackage = repository.blockedNotificationCountsByPackage(),
            apps = repository.notificationApps(),
            selectedPackages = repository.selectedNotificationPackages(),
        )
}

private data class NotificationBarSnapshot(
    val hasAccess: Boolean,
    val enabled: Boolean,
    val blockedCount: Int,
    val blockedCountsByPackage: Map<String, Int>,
    val apps: List<BlockableNotificationApp>,
    val selectedPackages: Set<String>,
)

private fun appString(resId: Int): String = QuickCleanApplication.instance.getString(resId)

class NotificationCleanerViewModel(
    repository: NotificationRepository,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NotificationBarViewModel(
    repository = repository,
    ioDispatcher = ioDispatcher,
)
