package com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXEmptyScanResult
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateFileEntry
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.DuplicateGroupItem
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.FileManagerPhase
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.FileManagerCompleteView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.FileManagerDeletingView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.FileManagerResultView
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerPageBrush
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.splitSizeLabel
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.process.FileManagerScanningView
import com.quickcleanpro.phonecleaner.presentation.screen.files.duplicates.DuplicateFilesManagerUiState
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

@Composable
internal fun DuplicateFilesManagerContentView(
    uiState: DuplicateFilesManagerUiState,
    groupListScrollState: ScrollState,
    scrollStateForGroup: (Int) -> ScrollState,
    onToggleAll: () -> Unit,
    onOpenGroup: (DuplicateGroupItem) -> Unit,
    onToggleFile: (DuplicateFileEntry) -> Unit,
    onAutoSelect: () -> Unit,
    onToggleGroupSelection: () -> Unit,
    onContinue: () -> Unit,
) {
    if (uiState.phase == FileManagerPhase.Deleting) {
        FileManagerDeletingView(stringResource(R.string.file_deleting_duplicate_files))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FileManagerPageBrush)
            .padding(horizontal = 16.dp),
    ) {
        when (uiState.phase) {
            FileManagerPhase.Scanning -> FileManagerScanningView(text = stringResource(R.string.file_scanning_duplicate_files))
            FileManagerPhase.Browsing,
            FileManagerPhase.ConfirmDelete -> {
                val group = uiState.selectedGroup
                if (group == null) {
                    DuplicateFilesGroupListView(
                        groups = uiState.groups,
                        selectedFileKeys = uiState.selectedFileKeys,
                        allSelected = uiState.allSelected,
                        scrollState = groupListScrollState,
                        onToggleAll = onToggleAll,
                        onOpenGroup = onOpenGroup,
                    )
                } else {
                    DuplicateFilesGroupDetailView(
                        group = group,
                        selectedFileKeys = uiState.selectedFileKeys,
                        scrollState = scrollStateForGroup(group.id),
                        onToggleFile = onToggleFile,
                        onAutoSelect = onAutoSelect,
                        onToggleGroupSelection = onToggleGroupSelection,
                    )
                }
            }
            FileManagerPhase.Deleting -> Unit
            FileManagerPhase.CompleteAnimation -> FileManagerCompleteView()
            FileManagerPhase.Result -> {
                val result = FileSizeFormatter.format(uiState.deletedBytes).splitSizeLabel()
                FileManagerResultView(
                    amount = result.first,
                    unit = result.second,
                    caption = stringResource(R.string.file_deleted_in_cleanup),
                    onContinue = onContinue,
                )
            }
            FileManagerPhase.NoResults -> CleanXEmptyScanResult(
                message = stringResource(R.string.file_scan_completed_no_results),
            )
        }
    }
}
