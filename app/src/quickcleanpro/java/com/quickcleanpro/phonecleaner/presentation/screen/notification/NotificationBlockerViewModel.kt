package com.quickcleanpro.phonecleaner.presentation.screen.notification

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.repository.ToolboxRepository
import com.quickcleanpro.phonecleaner.presentation.common.toolboxRepositoryOrPreview
import com.quickcleanpro.phonecleaner.domain.model.notification.BlockableNotificationApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.quickcleanpro.phonecleaner.presentation.common.appString

internal enum class NotificationPage {
    Onboarding,
    Scanning,
    Complete,
    Status,
    Settings
}

internal data class NotificationBlockerUiState(
    val hasAccess: Boolean = false,
    val enabled: Boolean = false,
    val blockedCount: Int = 0,
    val blockedCountsByPackage: Map<String, Int> = emptyMap(),
    val apps: List<BlockableNotificationApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val page: NotificationPage = NotificationPage.Onboarding,
    val isLoading: Boolean = true,
    val isInitialized: Boolean = false,
    val errorMessage: String? = null
)

internal class NotificationBlockerViewModel(
    private val repository: ToolboxRepository = toolboxRepositoryOrPreview(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val testLoader: (((suspend () -> Unit)) -> Unit)? = null
) : ViewModel() {

    constructor(
        repository: ToolboxRepository,
        ioDispatcher: CoroutineDispatcher
    ) : this(
        repository = repository,
        ioDispatcher = ioDispatcher,
        testLoader = null
    )

    private val _uiState = MutableStateFlow(NotificationBlockerUiState())
    private var hasShownAccessScan = false

    val uiState: StateFlow<NotificationBlockerUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        launchLoad {
            runCatching { readSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update { state ->
                        if (!snapshot.hasAccess) {
                            hasShownAccessScan = false
                        }
                        val nextPage = state.page.nextPageAfter(
                            snapshot = snapshot,
                            hasShownAccessScan = hasShownAccessScan
                        )
                        if (nextPage == NotificationPage.Scanning) {
                            hasShownAccessScan = true
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
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isInitialized = true,
                            errorMessage = error.message ?: appString(R.string.notification_status_load_failed)
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
        launchLoad {
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
        _uiState.update { it.copy(page = NotificationPage.Settings) }
    }

    fun leaveSettings() {
        _uiState.update { it.copy(page = NotificationPage.Status) }
    }

    fun finishScanning() {
        _uiState.update {
            if (it.page == NotificationPage.Scanning) {
                it.copy(page = NotificationPage.Status)
            } else {
                it
            }
        }
    }

    fun markComplete() {
        _uiState.update { it.copy(page = NotificationPage.Complete) }
    }

    fun notificationListenerSettingsIntent(): Intent =
        repository.notificationListenerSettingsIntent()

    fun appNotificationSettingsIntent(packageName: String): Intent =
        repository.appNotificationSettingsIntent(packageName)

    fun appDetailsSettingsIntent(packageName: String): Intent =
        repository.appDetailsSettingsIntent(packageName)

    private fun readSnapshot(): NotificationBlockerSnapshot =
        NotificationBlockerSnapshot(
            hasAccess = repository.hasNotificationListenerAccess(),
            enabled = repository.isNotificationBlockingEnabled(),
            blockedCount = repository.blockedNotificationCount(),
            blockedCountsByPackage = repository.blockedNotificationCountsByPackage(),
            apps = repository.notificationApps(),
            selectedPackages = repository.selectedNotificationPackages()
        )

    private fun launchLoad(block: suspend () -> Unit) {
        val loader = testLoader
        if (loader != null) {
            loader(block)
        } else {
            viewModelScope.launch(ioDispatcher) { block() }
        }
    }

}

private data class NotificationBlockerSnapshot(
    val hasAccess: Boolean,
    val enabled: Boolean,
    val blockedCount: Int,
    val blockedCountsByPackage: Map<String, Int>,
    val apps: List<BlockableNotificationApp>,
    val selectedPackages: Set<String>
)

private fun NotificationPage.nextPageAfter(
    snapshot: NotificationBlockerSnapshot,
    hasShownAccessScan: Boolean
): NotificationPage =
    when {
        this == NotificationPage.Settings -> NotificationPage.Settings
        this == NotificationPage.Complete -> NotificationPage.Complete
        this == NotificationPage.Scanning -> NotificationPage.Scanning
        snapshot.hasAccess && !hasShownAccessScan -> NotificationPage.Scanning
        snapshot.hasAccess -> NotificationPage.Status
        else -> NotificationPage.Onboarding
    }

