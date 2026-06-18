package com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXEmptyScanResult
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.DeleteConfirmDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.NoResultsDialog
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.StopScanDialog
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateFilesViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.PhotosState
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.requestMediaStoreDeleteOrDeleteDirectly
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.splitSizeLabel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.CompleteAnimationContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.DeletingAnimationContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileCollectionResultContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.ScanningContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.views.DuplicateGroupDetailContent
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.views.DuplicateGroupsContent
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun DuplicateFilesManagerScreen(
    permissionGateConfig: PermissionGateConfig? = null
) {
    DuplicateFilesManagerScreenState(
        viewModel = koinViewModel(),
        permissionGateConfig = permissionGateConfig
    )
}

@Composable
private fun DuplicateFilesManagerScreenState(
    viewModel: DuplicateFilesViewModel,
    permissionGateConfig: PermissionGateConfig? = null
) {
    val router = LocalRouter.current
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showStopDialog by remember { mutableStateOf(false) }
    val groupListScrollState = remember { ScrollState(0) }
    val groupDetailScrollStates = remember { mutableMapOf<Int, ScrollState>() }
    fun scrollStateForGroup(groupId: Int): ScrollState =
        groupDetailScrollStates.getOrPut(groupId) { ScrollState(0) }
    val selectedGroup = uiState.selectedGroup
    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.deleteSelectedFiles()
        } else {
            viewModel.rejectSystemDelete()
        }
    }

    FileManagerErrorToastEffect(
        errorMessage = uiState.errorMessage,
        onConsumed = viewModel::clearError
    )

    BackHandler {
        when {
            uiState.phase == PhotosState.Scanning -> showStopDialog = true
            selectedGroup != null -> viewModel.closeGroup()
            uiState.phase == PhotosState.Result -> router.goHome()
            else -> router.goBack()
        }
    }

    if (uiState.phase == PhotosState.Deleting) {
        DeletingAnimationContent(stringResource(R.string.file_deleting_duplicate_files))
        return
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.nav_duplicate_files),
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        backgroundBrush = FileManagerPageBrush,
        onBack = {
            when {
                uiState.phase == PhotosState.Scanning -> showStopDialog = true
                selectedGroup != null -> viewModel.closeGroup()
                uiState.phase == PhotosState.Result -> router.goHome()
                else -> router.goBack()
            }
        },
        permissionGateConfig = permissionGateConfig,
        bottomBar = {
            if ((uiState.phase == PhotosState.Browsing || uiState.phase == PhotosState.ConfirmDelete) && selectedGroup == null) {
                CleanXBottomActionBar(
                    enabled = uiState.filesToDelete.isNotEmpty(),
                    text = stringResource(R.string.file_clean_up_size, formatDuplicateCleanupSize(uiState.selectedDeleteSize)),
                    onClick = viewModel::requestDelete,
                    backgroundColor = Color.Transparent,
                    buttonModifier = Modifier.height(52.dp),
                    buttonCornerRadius = 10.dp,
                    buttonFontSize = 20.sp,
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FileManagerPageBrush)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState.phase) {
                PhotosState.Scanning -> ScanningContent(text = stringResource(R.string.file_scanning_duplicate_files))
                PhotosState.Browsing, PhotosState.ConfirmDelete -> {
                    val group = selectedGroup
                    if (group == null) {
                        DuplicateGroupsContent(
                            groups = uiState.groups,
                            selectedFileKeys = uiState.selectedFileKeys,
                            allSelected = uiState.allSelected,
                            scrollState = groupListScrollState,
                            onToggleAll = viewModel::toggleAll,
                            onOpenGroup = viewModel::openGroup
                        )
                    } else {
                        DuplicateGroupDetailContent(
                            group = group,
                            selectedFileKeys = uiState.selectedFileKeys,
                            scrollState = scrollStateForGroup(group.id),
                            onToggleFile = viewModel::toggleFile,
                            onAutoSelect = viewModel::autoSelectCurrentGroup,
                            onToggleGroupSelection = viewModel::toggleCurrentGroupSelection
                        )
                    }
                }
                PhotosState.Deleting -> Unit
                PhotosState.CompleteAnimation -> CompleteAnimationContent()
                PhotosState.Result -> {
                    val result = FileSizeFormatter.format(uiState.deletedBytes).splitSizeLabel()
                    FileCollectionResultContent(
                        amount = result.first,
                        unit = result.second,
                        caption = stringResource(R.string.file_deleted_in_cleanup),
                        onContinue = viewModel::continueManaging,
                    )
                }
                PhotosState.NoResults -> CleanXEmptyScanResult(
                    message = stringResource(R.string.file_scan_completed_no_results),
                )
            }
        }
    }

    if (showStopDialog) {
        StopScanDialog(
            onQuit = {
                showStopDialog = false
                router.goBack()
            },
            onResume = { showStopDialog = false }
        )
    }

    if (uiState.phase == PhotosState.ConfirmDelete) {
        DeleteConfirmDialog(
            onCancel = viewModel::cancelDelete,
            onDelete = {
                requestMediaStoreDeleteOrDeleteDirectly(
                    context = context,
                    uris = uiState.selectedUris,
                    launchRequest = deleteLauncher::launch,
                    deleteDirectly = viewModel::deleteSelectedFiles
                )
            }
        )
    }

    if (uiState.phase == PhotosState.NoResults) {
        NoResultsDialog(onBack = { router.goBack() })
    }
}

@Composable
private fun FileManagerErrorToastEffect(
    errorMessage: String?,
    onConsumed: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        onConsumed()
    }
}

private fun formatDuplicateCleanupSize(bytes: Long): String =
    if (bytes == 0L) "0KB" else FileSizeFormatter.format(bytes)
