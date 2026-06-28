package com.quickcleanpro.phonecleaner.presentation.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionItem
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManagePermissionRowState(
    val labelRes: Int,
    val item: CleanXPermissionItem,
    val checked: Boolean,
)

data class ManagePermissionsUiState(
    val rows: List<ManagePermissionRowState> = emptyList(),
)

private fun initialRows(): List<ManagePermissionRowState> =
    storageCleanerManageItems.map { item ->
        ManagePermissionRowState(
            labelRes = item.labelRes,
            item = item.item,
            checked = false,
        )
    }

class ManagePermissionsViewModel(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManagePermissionsUiState(rows = initialRows()))
    val uiState: StateFlow<ManagePermissionsUiState> = _uiState.asStateFlow()

    fun load(context: Context) {
        refresh(context)
    }

    fun refresh(
        context: Context,
        refreshAgainAfterDelay: Boolean = false,
    ) {
        val appContext = context.applicationContext ?: context
        _uiState.value = ManagePermissionsUiState(rows = buildRows(appContext))
        if (refreshAgainAfterDelay) {
            viewModelScope.launch(ioDispatcher) {
                delay(300L)
                _uiState.value = ManagePermissionsUiState(rows = buildRows(appContext))
            }
        }
    }

    fun onResume(context: Context) {
        refresh(context, refreshAgainAfterDelay = true)
    }

    private fun buildRows(context: Context): List<ManagePermissionRowState> {
        val manager = CleanXPermissionRegistry.permissionItemManager(context)
        return storageCleanerManageItems.map { item ->
            ManagePermissionRowState(
                labelRes = item.labelRes,
                item = item.item,
                checked = manager.status(context, item.item).granted,
            )
        }
    }
}

private data class StorageCleanerPermissionManageItem(
    val item: CleanXPermissionItem,
    val labelRes: Int,
)

private val storageCleanerManageItems: List<StorageCleanerPermissionManageItem> =
    listOf(
        StorageCleanerPermissionManageItem(CleanXPermissionItem.StorageFiles, R.string.settings_storage_permission),
        StorageCleanerPermissionManageItem(CleanXPermissionItem.UsageAccess, R.string.settings_usage_data_permission),
        StorageCleanerPermissionManageItem(CleanXPermissionItem.Location, R.string.settings_location_permission),
        StorageCleanerPermissionManageItem(
            CleanXPermissionItem.NotificationListener,
            R.string.settings_notification_permission,
        ),
        StorageCleanerPermissionManageItem(CleanXPermissionItem.Overlay, R.string.settings_overlay_permission),
        StorageCleanerPermissionManageItem(
            CleanXPermissionItem.PostNotifications,
            R.string.settings_post_notifications_permission,
        ),
    )
