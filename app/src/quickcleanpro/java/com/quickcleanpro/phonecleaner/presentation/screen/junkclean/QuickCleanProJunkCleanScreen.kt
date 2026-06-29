package com.quickcleanpro.phonecleaner.presentation.screen.junkclean

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.FeatureKey
import com.quickcleanpro.phonecleaner.domain.model.CategoryCleanGroup
import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import com.quickcleanpro.phonecleaner.operation.FeatureOperationEvent
import com.quickcleanpro.phonecleaner.operation.LocalFeatureOperationTracker
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCheckBadge
import com.quickcleanpro.phonecleaner.presentation.common.CleanXContentPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXFullScreenDeleteAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScanRingAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultCheckIcon
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultScreen
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanEvent
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanPhase
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanResultUiState
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanScanState
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanUiState
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.JunkCleanViewModel
import com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.SelectionSummary
import com.quickcleanpro.phonecleaner.presentation.screen.files.StopScanDialog
import java.util.concurrent.ConcurrentHashMap

@Composable
fun QuickCleanProJunkCleanScreen(
    viewModel: JunkCleanViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateTool: (String) -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val tracker = LocalFeatureOperationTracker.current
    var showStopDialog by remember { mutableStateOf(false) }

    val deleteAuthorizationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            viewModel.handleAuthorizationResult(result.resultCode == Activity.RESULT_OK)
        }

    LaunchedEffect(viewModel, permissionCoordinator) {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.JunkStartScan,
            onGranted = viewModel::startScanIfNeeded,
            onRejected = {
                tracker.trackWithAd(FeatureOperationEvent.PermissionRejected(FeatureKey.JUNK_CLEAN)) {
                    viewModel.clearResult()
                    onNavigateHome()
                }
            },
        )
    }

    LaunchedEffect(viewModel, tracker) {
        viewModel.operationEvents.collect { event ->
            tracker.trackWithAd(event) {}
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is JunkCleanEvent.RequestDeleteAuthorization -> {
                    deleteAuthorizationLauncher.launch(
                        IntentSenderRequest.Builder(event.deleteRequest.intentSender).build(),
                    )
                }
            }
        }
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == JunkCleanPhase.Complete) {
            viewModel.markResultShown()
        }
    }

    fun exitToHome(showCompletionAd: Boolean = false) {
        if (showCompletionAd) {
            tracker.trackWithAd(FeatureOperationEvent.ReturnHome(FeatureKey.JUNK_CLEAN)) {
                viewModel.clearResult()
                onNavigateHome()
            }
        } else {
            viewModel.clearResult()
            onNavigateHome()
        }
    }

    fun handleBack() {
        when (uiState.phase) {
            JunkCleanPhase.Scanning,
            JunkCleanPhase.Cleaning,
            JunkCleanPhase.AwaitingAuthorization,
            -> {
                if (uiState.phase == JunkCleanPhase.Scanning) {
                    viewModel.cancelActiveOperation()
                } else {
                    viewModel.cancelCleaningAndReturnToPreview()
                }
                showStopDialog = true
            }
            JunkCleanPhase.Complete -> exitToHome(showCompletionAd = true)
            else -> onNavigateBack()
        }
    }

    BackHandler(
        enabled = uiState.phase == JunkCleanPhase.Scanning ||
            uiState.phase == JunkCleanPhase.Cleaning ||
            uiState.phase == JunkCleanPhase.AwaitingAuthorization ||
            uiState.phase == JunkCleanPhase.Complete,
        onBack = ::handleBack,
    )

    QuickCleanProJunkCleanContent(
        uiState = uiState,
        onBack = ::handleBack,
        onToggleCategorySelection = { category ->
            viewModel.toggleCategorySelection(listOf(category))
        },
        onToggleItem = { item ->
            viewModel.toggleItemSelection(item.junkFile.id)
        },
        onClean = {
            permissionCoordinator.guard(CleanXProtectedAction.JunkCleanSelected) {
                viewModel.startCleaning(context)
            }
        },
        onContinueFromResult = { exitToHome(showCompletionAd = true) },
        onNavigateTool = onNavigateTool,
    )

    if (showStopDialog) {
        StopScanDialog(
            onQuit = {
                showStopDialog = false
                viewModel.cancelActiveOperation()
                onNavigateBack()
            },
            onResume = {
                showStopDialog = false
                if (uiState.phase == JunkCleanPhase.Scanning) {
                    viewModel.startScanIfNeeded()
                }
            },
        )
    }
}

@Composable
private fun QuickCleanProJunkCleanContent(
    uiState: JunkCleanUiState,
    onBack: () -> Unit,
    onToggleCategorySelection: (JunkCategory) -> Unit,
    onToggleItem: (CleanItem) -> Unit,
    onClean: () -> Unit,
    onContinueFromResult: () -> Unit,
    onNavigateTool: (String) -> Unit,
) {
    when (uiState.phase) {
        JunkCleanPhase.Cleaning -> CleaningContent()
        JunkCleanPhase.Complete -> CleanResultContent(
            result = uiState.cleanResult,
            onBack = onContinueFromResult,
            onNavigateTool = onNavigateTool,
        )
        else -> JunkCleanScaffoldContent(
            uiState = uiState,
            onBack = onBack,
            onToggleCategorySelection = onToggleCategorySelection,
            onToggleItem = onToggleItem,
            onClean = onClean,
        )
    }
}

@Composable
private fun JunkCleanScaffoldContent(
    uiState: JunkCleanUiState,
    onBack: () -> Unit,
    onToggleCategorySelection: (JunkCategory) -> Unit,
    onToggleItem: (CleanItem) -> Unit,
    onClean: () -> Unit,
) {
    CleanXScaffold(
        titleRes = R.string.junk_removal,
        onBack = onBack,
        horizontalPadding = 13.dp,
        bottomBar = {
            if (uiState.phase == JunkCleanPhase.Preview) {
                ResultBottomBar(
                    selectedSummary = uiState.selectedSummary,
                    onClean = onClean,
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = 13.dp),
        ) {
            when (uiState.phase) {
                JunkCleanPhase.Scanning -> ScanProgressContent(uiState = uiState)
                JunkCleanPhase.Preview -> ResultListContent(
                    groups = uiState.groups,
                    checkedEmptyCategories = uiState.checkedEmptyCategories,
                    onToggleCategorySelection = onToggleCategorySelection,
                    onToggleItem = onToggleItem,
                )
                JunkCleanPhase.AwaitingAuthorization -> AwaitingAuthorizationContent(
                    message = uiState.awaitingAuthorizationMessage.orEmpty(),
                )
                JunkCleanPhase.Error -> ErrorContent(
                    message = uiState.errorMessage
                        ?: uiState.errorMessageRes?.let { stringResource(it) }
                        ?: stringResource(R.string.error),
                )
                JunkCleanPhase.Cleaning,
                JunkCleanPhase.Complete,
                -> Unit
            }
        }
    }
}

@Composable
private fun ScanProgressContent(uiState: JunkCleanUiState) {
    val categoryLabel = uiState.currentCategory?.let { stringResource(it.titleRes) }
    val scanningText =
        when {
            uiState.scanState == JunkCleanScanState.Error -> uiState.errorMessage
                ?: uiState.errorMessageRes?.let { stringResource(it) }
                ?: stringResource(R.string.scan_failed)
            categoryLabel != null -> stringResource(R.string.scan_scanning_category, categoryLabel)
            else -> stringResource(R.string.scan_loading_fallback)
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanXBackground),
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            CleanXScanRingAnimation(
                modifier = Modifier.size(260.dp),
                ringWidth = 20.dp,
                ringColor = CleanXBlue,
                backgroundColor = CleanXBlue.copy(alpha = 0.12f),
                animationDurationMillis = 900,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.formattedFoundSize.ifBlank { "0 B" },
                        color = CleanXBlue,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scanningText,
                        color = CleanXMutedText,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultListContent(
    groups: List<CategoryCleanGroup>,
    checkedEmptyCategories: Set<JunkCategory>,
    onToggleCategorySelection: (JunkCategory) -> Unit,
    onToggleItem: (CleanItem) -> Unit,
) {
    val rows = displayGroups(groups)
    var expandedIndex by remember { mutableStateOf(-1) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SummaryCard(totalSize = groups.sumOf { it.totalSize })
        }

        itemsIndexed(rows) { rowIndex, row ->
            val items = row.group.items
            CategoryGroupCard(
                row = row,
                checked = items.takeIf { it.isNotEmpty() }?.all { it.isChecked }
                    ?: (row.group.category in checkedEmptyCategories),
                expanded = expandedIndex == rowIndex && items.isNotEmpty(),
                onToggleExpanded = {
                    expandedIndex = if (expandedIndex == rowIndex) -1 else rowIndex
                },
                onToggleCategorySelection = {
                    onToggleCategorySelection(row.group.category)
                },
                onToggleItem = onToggleItem,
            )
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun SummaryCard(totalSize: Long) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            TrashIcon()
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayMainSize(totalSize),
                        color = Color(0xFFFF8F2D),
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayUnit(totalSize),
                        color = CleanXText,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.occupying),
                    color = Color(0xFF7D8EA8),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryGroupCard(
    row: ResultDisplayRow,
    checked: Boolean,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleCategorySelection: () -> Unit,
    onToggleItem: (CleanItem) -> Unit,
) {
    val group = row.group
    val items = group.items

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFEFF6FC),
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(row.titleRes),
                    color = CleanXText,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = group.formattedTotalSize,
                    color = CleanXText,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.width(7.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = items.isNotEmpty()) { onToggleExpanded() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = CleanXText,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(7.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onToggleCategorySelection() },
                    contentAlignment = Alignment.Center,
                ) {
                    CleanXCheckBadge(checked = checked, size = 21.dp)
                }
            }

            if (expanded && items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFD0D9E4)),
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items.forEach { item ->
                        JunkItemCard(
                            item = item,
                            modifier = Modifier.width(50.dp),
                            onClick = { onToggleItem(item) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JunkItemCard(
    item: CleanItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (item.isChecked) Color(0xFFEAF8FF) else Color(0xFFF1F4F8)),
                contentAlignment = Alignment.Center,
            ) {
                JunkItemIcon(item = item)
            }
            if (item.isChecked) {
                CleanXCheckBadge(
                    checked = true,
                    size = 18.dp,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.fileName.ifBlank { stringResource(R.string.unnamed_file) },
            color = CleanXText,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.formattedSize,
            color = CleanXText,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun JunkItemIcon(item: CleanItem) {
    val context = LocalContext.current
    val filePath = item.junkFile.filePath
    val apkIcon =
        remember(item.category, filePath) {
            if (item.category == JunkCategory.APK) {
                loadApkIconBitmap(context, filePath)
            } else {
                null
            }
        }

    if (apkIcon != null) {
        Image(
            bitmap = apkIcon,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentScale = ContentScale.Fit,
        )
    } else {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            tint = CleanXBlue,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ResultBottomBar(
    selectedSummary: SelectionSummary,
    onClean: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CleanXBackground)
            .navigationBarsPadding()
            .padding(horizontal = 13.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val onlyZeroByteSelection =
            selectedSummary.checkedCount == 0 && selectedSummary.checkedEmptyCategoryCount > 0
        val buttonText =
            if (selectedSummary.checkedCount > 0) {
                stringResource(R.string.remove_size, compactSizeLabel(selectedSummary.checkedSize))
            } else if (onlyZeroByteSelection) {
                stringResource(R.string.result_zero_byte_selection_button)
            } else {
                stringResource(R.string.result_select_items_button)
            }
        if (onlyZeroByteSelection) {
            Text(
                text = stringResource(R.string.result_zero_byte_selection_hint),
                color = CleanXMutedText,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
        }
        CleanXPrimaryButton(
            text = buttonText,
            onClick = onClean,
            enabled = selectedSummary.checkedCount > 0,
        )
    }
}

@Composable
private fun CleaningContent() {
    CleanXFullScreenDeleteAnimation(
        fallbackText = stringResource(R.string.cleaning_selected_files),
    )
}

@Composable
private fun AwaitingAuthorizationContent(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.authorization_required),
            color = CleanXText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = CleanXMutedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = CleanXContentPadding),
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.error),
            color = CleanXText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = CleanXMutedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = CleanXContentPadding),
        )
    }
}

@Composable
private fun CleanResultContent(
    result: JunkCleanResultUiState,
    onBack: () -> Unit,
    onNavigateTool: (String) -> Unit,
) {
    val formatted = result.formattedFreedSpace
    val showResult = result.hasVisibleResult && formatted.isNotBlank()
    val numPart = if (showResult) formatted.substringBeforeLast(" ", formatted) else ""
    val unitPart = if (showResult) formatted.substringAfterLast(" ", "MB") else ""

    CommonResultScreen(
        titleRes = R.string.junk_removal,
        onBack = onBack,
        onNavigateTool = onNavigateTool,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            CommonResultCheckIcon(size = 45.dp)
            Spacer(modifier = Modifier.height(20.dp))
            if (showResult) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = numPart,
                        color = CleanXText,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = unitPart,
                        color = CleanXText,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text =
                        if (result.failedCount > 0) {
                            stringResource(R.string.clean_result_junk_removed_with_failed, result.failedCount)
                        } else {
                            stringResource(R.string.clean_result_junk_removed)
                        },
                    color = CleanXMutedText,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
            } else if (result.failedCount > 0) {
                Text(
                    text = stringResource(R.string.clean_result_files_failed, result.failedCount),
                    color = CleanXMutedText,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            } else {
                Text(
                    text = stringResource(R.string.clean_result_done),
                    color = CleanXMutedText,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TrashIcon() {
    Box(
        modifier = Modifier.size(54.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = Color(0xFFFF3F42),
                topLeft = Offset(size.width * 0.22f, size.height * 0.34f),
                size = Size(size.width * 0.56f, size.height * 0.48f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
            drawRoundRect(
                color = Color(0xFFFF3F42),
                topLeft = Offset(size.width * 0.10f, size.height * 0.16f),
                size = Size(size.width * 0.80f, size.height * 0.16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.38f, size.height * 0.48f),
                end = Offset(size.width * 0.62f, size.height * 0.68f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.62f, size.height * 0.48f),
                end = Offset(size.width * 0.38f, size.height * 0.68f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

private data class ResultDisplayRow(
    @param:StringRes val titleRes: Int,
    val group: CategoryCleanGroup,
)

private val apkIconBitmapCache = ConcurrentHashMap<String, ImageBitmap>()

private fun displayMainSize(size: Long): String {
    val formatted = JunkFile.formatFileSize(size)
    return formatted.takeWhile { it.isDigit() || it == '.' }.ifBlank { "0" }
}

private fun displayUnit(size: Long): String {
    val formatted = JunkFile.formatFileSize(size)
    return formatted.dropWhile { it.isDigit() || it == '.' }.trim().ifBlank { "B" }
}

private fun compactSizeLabel(size: Long): String {
    val mainSize = displayMainSize(size)
    val unit = displayUnit(size)
    return "$mainSize$unit"
}

private fun displayGroups(groups: List<CategoryCleanGroup>): List<ResultDisplayRow> {
    fun row(@StringRes titleRes: Int, category: JunkCategory): ResultDisplayRow {
        val group = groups.firstOrNull { it.category == category } ?: CategoryCleanGroup(category, emptyList())
        return ResultDisplayRow(titleRes = titleRes, group = group)
    }

    val residualGroup = groups.firstOrNull { it.category == JunkCategory.RESIDUAL }
        ?: groups.firstOrNull { it.category == JunkCategory.DUPLICATE }
        ?: CategoryCleanGroup(JunkCategory.RESIDUAL, emptyList())
    val otherGroup = groups.firstOrNull { it.category == JunkCategory.LARGE_FILE }
        ?: CategoryCleanGroup(JunkCategory.LARGE_FILE, emptyList())

    return listOf(
        row(R.string.junk_group_system_cache, JunkCategory.CACHE),
        row(R.string.junk_group_ad_junk_files, JunkCategory.TEMP_FILE),
        ResultDisplayRow(R.string.junk_group_residual_junks, residualGroup),
        row(R.string.junk_group_obsolete_apks, JunkCategory.APK),
        ResultDisplayRow(R.string.junk_group_other_junk_files, otherGroup),
    )
}

private fun loadApkIconBitmap(
    context: Context,
    filePath: String,
): ImageBitmap? {
    if (filePath.isBlank()) return null
    apkIconBitmapCache[filePath]?.let { return it }

    val packageManager = context.packageManager
    val packageInfo =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    filePath,
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(filePath, 0)
            }
        }.getOrNull() ?: return null

    val applicationInfo = packageInfo.applicationInfo ?: return null
    applicationInfo.sourceDir = filePath
    applicationInfo.publicSourceDir = filePath

    val drawable = runCatching { applicationInfo.loadIcon(packageManager) }.getOrNull()
        ?: return null
    return cacheApkIconBitmap(filePath, drawable.toBitmap().asImageBitmap())
}

private fun cacheApkIconBitmap(
    filePath: String,
    bitmap: ImageBitmap,
): ImageBitmap {
    apkIconBitmapCache[filePath] = bitmap
    return bitmap
}

private fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val width = intrinsicWidth.takeIf { it > 0 } ?: 96
    val height = intrinsicHeight.takeIf { it > 0 } ?: 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

private val JunkCategory.titleRes: Int
    @StringRes
    get() =
        when (this) {
            JunkCategory.CACHE -> R.string.category_cache
            JunkCategory.TEMP_FILE -> R.string.category_temp
            JunkCategory.RESIDUAL -> R.string.category_residual
            JunkCategory.APK -> R.string.category_apk
            JunkCategory.DUPLICATE -> R.string.category_duplicate
            JunkCategory.LARGE_FILE -> R.string.category_large
        }
