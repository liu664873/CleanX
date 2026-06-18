package com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.ToolFeatureBanners
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanCompleteBadge
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionFeature
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGatePresets
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerCategory
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerGroup
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerGroupItem
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerPhase
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerSubItem
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.whatsappcleaner.WhatsAppCleanerViewModel
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

@Composable
internal fun WhatsAppCleanerScreenState(viewModel: WhatsAppCleanerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.startScanIfNeeded()
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.whatsapp_cleaner),
        permissionGateConfig = PermissionGatePresets.storage(CleanXPermissionFeature.WhatsAppCleaner),
    ) {
        when (uiState.phase) {
            WhatsAppCleanerPhase.Scanning -> WhatsAppLoadingContent(text = stringResource(R.string.scanning_whatsapp_files))
            WhatsAppCleanerPhase.Cleaning -> WhatsAppLoadingContent(text = stringResource(R.string.cleaning_whatsapp_files))
            WhatsAppCleanerPhase.ScanResult -> WhatsAppScanResultContent(
                uiState = uiState,
                onToggleGroup = viewModel::toggleGroup,
                onToggleCategory = viewModel::toggleCategory,
                onToggleExpanded = viewModel::toggleExpanded,
                onClean = viewModel::cleanSelectedFiles,
            )
            WhatsAppCleanerPhase.Result -> WhatsAppResultContent(uiState = uiState)
            WhatsAppCleanerPhase.Error -> WhatsAppErrorContent(
                message = uiState.errorMessage ?: stringResource(R.string.whatsapp_clean_unavailable),
                onRetry = viewModel::retry,
            )
        }
    }
}

@Composable
private fun WhatsAppLoadingContent(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier.size(252.dp),
            contentAlignment = Alignment.Center,
        ) {
            CleanSpiralAnimation(
                modifier = Modifier.size(252.dp),
                centerSize = 100.dp,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_whatsapp_cleaner),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(84.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = text,
            color = Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WhatsAppScanResultContent(
    uiState: WhatsAppCleanerUiState,
    onToggleGroup: (WhatsAppCleanerGroup) -> Unit,
    onToggleCategory: (WhatsAppCleanerGroup, WhatsAppCleanerCategory) -> Unit,
    onToggleExpanded: (WhatsAppCleanerGroup) -> Unit,
    onClean: () -> Unit,
) {
    SummaryCard(totalBytes = uiState.scannedBytes)
    Spacer(modifier = Modifier.height(16.dp))

    uiState.groups.forEach { group ->
        GroupCard(
            groupItem = group,
            onToggleGroup = { onToggleGroup(group.group) },
            onToggleExpanded = { onToggleExpanded(group.group) },
            onToggleCategory = { category -> onToggleCategory(group.group, category) },
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    CleanXPrimaryButton(
        text = stringResource(R.string.remove_size, FileSizeFormatter.format(uiState.selectedBytes)),
        onClick = onClean,
        enabled = uiState.selectedBytes > 0L,
    )
    Spacer(modifier = Modifier.height(100.dp))
}

@Composable
private fun SummaryCard(totalBytes: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE9FFF1)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_whatsapp_cleaner),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(modifier = Modifier.size(14.dp))
            Column {
                Text(
                    text = stringResource(R.string.occupying),
                    color = NavyMuted,
                    fontSize = 15.sp,
                )
                Text(
                    text = FileSizeFormatter.format(totalBytes),
                    color = Navy,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun GroupCard(
    groupItem: WhatsAppCleanerGroupItem,
    onToggleGroup: () -> Unit,
    onToggleExpanded: () -> Unit,
    onToggleCategory: (WhatsAppCleanerCategory) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionBadge(
                    selected = groupItem.selected,
                    enabled = groupItem.hasFiles,
                    modifier = Modifier.clickable(enabled = groupItem.hasFiles, onClick = onToggleGroup),
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(groupItem.group.titleRes),
                        color = Navy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = FileSizeFormatter.format(groupItem.totalBytes),
                        color = NavyMuted,
                        fontSize = 14.sp,
                    )
                }
                Icon(
                    imageVector = if (groupItem.expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NavyMuted,
                    modifier =
                        Modifier
                            .size(28.dp)
                            .clickable(onClick = onToggleExpanded),
                )
            }

            if (groupItem.expanded) {
                Spacer(modifier = Modifier.height(14.dp))
                groupItem.children.forEachIndexed { index, child ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Divider15,
                        )
                    }
                    CategoryRow(
                        item = child,
                        onToggle = { onToggleCategory(child.category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(
    item: WhatsAppCleanerSubItem,
    onToggle: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = item.hasFiles, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.category.iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(item.category.iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.category.titleRes),
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "${item.files.size} files / ${FileSizeFormatter.format(item.totalBytes)}",
                color = NavyMuted,
                fontSize = 13.sp,
            )
        }
        SelectionBadge(
            selected = item.selected,
            enabled = item.hasFiles,
        )
    }
}

@Composable
private fun SelectionBadge(
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        selected -> CleanXBlue
                        enabled -> Color.White
                        else -> Color(0xFFE1E6EF)
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun WhatsAppResultContent(uiState: WhatsAppCleanerUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(42.dp))
        CleanCompleteBadge()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.all_junk_files_removed),
            color = Navy,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.removed_files_size, uiState.deletedCount, FileSizeFormatter.format(uiState.deletedBytes)),
            color = NavyMuted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        ToolFeatureBanners(excludeRoutes = setOf(Screen.WhatsAppCleaner.route))
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun WhatsAppErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.error),
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                color = NavyMuted,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
            CleanXPrimaryButton(
                text = stringResource(R.string.retry),
                onClick = onRetry,
            )
        }
    }
}

private val WhatsAppCleanerGroup.titleRes: Int
    get() =
        when (this) {
            WhatsAppCleanerGroup.Cache -> R.string.cache
            WhatsAppCleanerGroup.File -> R.string.file
        }

private val WhatsAppCleanerCategory.titleRes: Int
    get() =
        when (this) {
            WhatsAppCleanerCategory.Images -> R.string.images
            WhatsAppCleanerCategory.Videos -> R.string.videos
            WhatsAppCleanerCategory.Audios -> R.string.whatsapp_category_audios
            WhatsAppCleanerCategory.Documents -> R.string.whatsapp_category_documents
            WhatsAppCleanerCategory.Databases -> R.string.whatsapp_category_databases
            WhatsAppCleanerCategory.Other -> R.string.other
        }

private val WhatsAppCleanerCategory.iconRes: Int
    get() =
        when (this) {
            WhatsAppCleanerCategory.Images -> R.drawable.ic_photos
            WhatsAppCleanerCategory.Videos -> R.drawable.ic_videos
            WhatsAppCleanerCategory.Audios -> R.drawable.ic_audios
            WhatsAppCleanerCategory.Documents -> R.drawable.ic_documents
            WhatsAppCleanerCategory.Databases -> R.drawable.ic_documents
            WhatsAppCleanerCategory.Other -> R.drawable.ic_file_manager
        }

private val WhatsAppCleanerCategory.iconBackground: Color
    get() =
        when (this) {
            WhatsAppCleanerCategory.Images -> Color(0xFFF7ECFF)
            WhatsAppCleanerCategory.Videos -> Color(0xFFFFECF5)
            WhatsAppCleanerCategory.Audios -> Color(0xFFEAF4FF)
            WhatsAppCleanerCategory.Documents -> Color(0xFFFFF6E5)
            WhatsAppCleanerCategory.Databases -> Color(0xFFEFF3FF)
            WhatsAppCleanerCategory.Other -> Color(0xFFE9FFF9)
        }
