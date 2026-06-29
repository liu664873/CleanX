package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.CleanXButtonHeight
import com.quickcleanpro.phonecleaner.presentation.common.CleanXButtonHorizontalPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXCompactPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXFullScreenDeleteAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXItemSpacing
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLargePadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineBody
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineCaption
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineHero
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineSubtitle
import com.quickcleanpro.phonecleaner.presentation.common.CleanXLineTiny
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMediumPadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPagePadding
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPillShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScanRingAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSmallShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextBody
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextBodySmall
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextCaption
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextDisplay
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextHero
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextSubtitle
import com.quickcleanpro.phonecleaner.presentation.common.CleanXTextTiny
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultCheckIcon
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultScreen
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

private val WhatsAppAnimationHeight = 360.dp
private val WhatsAppScanRingSize = 252.dp
private val WhatsAppScanRingWidth = 20.dp
private val WhatsAppResultBottomPadding = 104.dp
private val WhatsAppResultHeroHeight = 126.dp
private val WhatsAppHeaderTopGap = 46.dp
private val WhatsAppHeaderBottomGap = 36.dp
private val WhatsAppLogoOuterSize = 84.dp
private val WhatsAppLogoInnerSize = 64.dp
private val WhatsAppLogoSize = 44.dp
private val WhatsAppGroupRowHeight = 62.dp
private val WhatsAppCategoryTouchSize = 42.dp
private val WhatsAppCategoryIconBoxSize = 36.dp
private val WhatsAppCategoryIconSize = 23.dp
private val WhatsAppSelectionSize = 22.dp
private val WhatsAppSelectionSmallSize = 16.dp
private val WhatsAppSelectionCheckSize = 15.dp
private val WhatsAppErrorTopPadding = 72.dp
private val WhatsAppActionGap = 24.dp
private val WhatsAppUnitBottomPadding = 5.dp
private val WhatsAppScanLabelTextSize = 10.sp
private val WhatsAppScanLabelLineHeight = 13.sp
private val WhatsAppRetryTextSize = 17.sp

@Composable
fun WhatsAppCleanerScreen(
    onBack: () -> Unit,
    onNavigateTool: (String) -> Unit,
    viewModel: WhatsAppCleanerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.startScanIfNeeded()
    }

    when (uiState.phase) {
        WhatsAppCleanerPhase.Cleaning -> WhatsAppDeletingContent()
        WhatsAppCleanerPhase.Result -> WhatsAppResultContent(
            deletedCount = uiState.deletedCount,
            deletedBytes = uiState.deletedBytes,
            onBack = onBack,
            onNavigateTool = onNavigateTool
        )
        WhatsAppCleanerPhase.ScanResult -> ToolboxScaffold(
            titleRes = R.string.whatsapp_cleaner,
            onBack = onBack,
            bottom = {
                CleanXBottomActionBar(
                    text = stringResource(
                        R.string.remove_size,
                        FileSizeFormatter.format(uiState.selectedBytes)
                    ),
                    enabled = uiState.selectedBytes > 0L,
                    onClick = viewModel::cleanSelectedFiles
                )
            }
        ) {
            WhatsAppScanResultContent(
                uiState = uiState,
                onToggleGroup = viewModel::toggleGroup,
                onToggleCategory = viewModel::toggleCategory,
                onToggleExpanded = viewModel::toggleExpanded
            )
        }
        else -> ToolboxScaffold(
            titleRes = R.string.whatsapp_cleaner,
            onBack = onBack,
            scrollable = uiState.phase != WhatsAppCleanerPhase.Cleaning
        ) {
            when (uiState.phase) {
                WhatsAppCleanerPhase.Scanning -> WhatsAppScanningContent(text = stringResource(R.string.scanning_whatsapp_files))
                WhatsAppCleanerPhase.Cleaning -> Unit
                WhatsAppCleanerPhase.Error -> WhatsAppErrorContent(
                    errorMessage = uiState.errorMessage ?: stringResource(R.string.whatsapp_clean_unavailable),
                    onRetry = viewModel::retry
                )
                WhatsAppCleanerPhase.ScanResult,
                WhatsAppCleanerPhase.Result -> Unit
            }
        }
    }
}

@Composable
internal fun WhatsAppCleanerRoute(
    onBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    WhatsAppCleanerScreen(
        onBack = onBack,
        onNavigateTool = onNavigateTool,
        viewModel = koinViewModel()
    )
}

@Composable
private fun WhatsAppDeletingContent() {
    CleanXFullScreenDeleteAnimation(
        fallbackText = stringResource(R.string.cleaning_whatsapp_files)
    )
}

@Composable
private fun WhatsAppScanningContent(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WhatsAppAnimationHeight),
        contentAlignment = Alignment.Center
    ) {
        CleanXScanRingAnimation(
            modifier = Modifier.size(WhatsAppScanRingSize),
            ringWidth = WhatsAppScanRingWidth,
            ringColor = CleanXBlue,
            backgroundColor = CleanXBlue.copy(alpha = 0.12f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.clean_upper),
                    color = CleanXBlue,
                    fontSize = CleanXTextDisplay,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text,
                    color = Color(0xFF9DDCFA),
                    fontSize = WhatsAppScanLabelTextSize,
                    lineHeight = WhatsAppScanLabelLineHeight,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WhatsAppScanResultContent(
    uiState: WhatsAppCleanerUiState,
    onToggleGroup: (WhatsAppCleanerGroup) -> Unit,
    onToggleCategory: (WhatsAppCleanerGroup, WhatsAppCleanerCategory) -> Unit,
    onToggleExpanded: (WhatsAppCleanerGroup) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = WhatsAppResultBottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(WhatsAppResultHeroHeight),
            color = Color(0xFFE7EAF4),
            shape = RoundedCornerShape(
                bottomStart = CleanXMediumPadding,
                bottomEnd = CleanXMediumPadding
            )
        ) {}
        Spacer(modifier = Modifier.height(WhatsAppHeaderTopGap))
        WhatsAppOccupyingHeader(totalBytes = uiState.scannedBytes)
        Spacer(modifier = Modifier.height(WhatsAppHeaderBottomGap))
        Column(verticalArrangement = Arrangement.spacedBy(CleanXItemSpacing)) {
            uiState.groups.forEach { group ->
                WhatsAppResultGroupCard(
                    groupItem = group,
                    onToggleGroup = { onToggleGroup(group.group) },
                    onToggleExpanded = { onToggleExpanded(group.group) },
                    onToggleCategory = { category -> onToggleCategory(group.group, category) }
                )
            }
        }
    }
}

@Composable
private fun WhatsAppOccupyingHeader(totalBytes: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(WhatsAppLogoOuterSize)
                .clip(CircleShape)
                .background(Color(0xFFEFFDF2)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(WhatsAppLogoInnerSize)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_whats_app_cleaner),
                    contentDescription = null,
                    modifier = Modifier.size(WhatsAppLogoSize)
                )
            }
        }
        Spacer(modifier = Modifier.width(CleanXLargePadding - 2.dp))
        Column {
            Text(
                text = stringResource(R.string.occupying),
                color = Color(0xFF5F6876),
                fontSize = CleanXTextSubtitle,
                lineHeight = CleanXLineSubtitle
            )
            Spacer(modifier = Modifier.height(CleanXCompactPadding))
            SizeHeadline(bytes = totalBytes)
        }
    }
}

@Composable
private fun SizeHeadline(bytes: Long) {
    val parts = FileSizeFormatter.format(bytes).splitSizeParts()
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = parts.first,
            color = CleanXText,
            fontSize = CleanXTextHero,
            lineHeight = CleanXLineHero,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(CleanXCompactPadding / 2))
        Text(
            text = parts.second,
            color = CleanXText,
            fontSize = CleanXTextBodySmall,
            lineHeight = CleanXLineSubtitle,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = WhatsAppUnitBottomPadding)
        )
    }
}

@Composable
private fun WhatsAppResultGroupCard(
    groupItem: WhatsAppCleanerGroupItem,
    onToggleGroup: () -> Unit,
    onToggleExpanded: () -> Unit,
    onToggleCategory: (WhatsAppCleanerCategory) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = CleanXSmallShape
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WhatsAppGroupRowHeight)
                    .padding(horizontal = CleanXPagePadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WhatsAppSelectionBadge(
                    selected = groupItem.selected,
                    enabled = groupItem.hasFiles,
                    modifier = Modifier.clickable(enabled = groupItem.hasFiles) { onToggleGroup() }
                )
                Spacer(modifier = Modifier.width(CleanXMediumPadding + 2.dp))
                Text(
                    text = stringResource(groupItem.group.displayNameRes),
                    color = CleanXText,
                    fontSize = CleanXTextBody,
                    lineHeight = CleanXLineBody,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = FileSizeFormatter.format(groupItem.totalBytes),
                    color = CleanXText,
                    fontSize = CleanXTextBodySmall,
                    lineHeight = CleanXLineBody,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(CleanXItemSpacing))
                Icon(
                    imageVector = if (groupItem.expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CleanXMutedText,
                    modifier = Modifier
                        .size(WhatsAppSelectionSize)
                        .clickable { onToggleExpanded() }
                )
            }

            if (groupItem.expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE1E6EF))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CleanXMediumPadding + 2.dp, vertical = CleanXMediumPadding + 2.dp),
                    verticalArrangement = Arrangement.spacedBy(CleanXItemSpacing)
                ) {
                    groupItem.children.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(CleanXCompactPadding)
                        ) {
                            rowItems.forEach { child ->
                                WhatsAppCategoryItem(
                                    item = child,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onToggleCategory(child.category) }
                                )
                            }
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatsAppCategoryItem(
    item: WhatsAppCleanerSubItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(CleanXSmallShape)
            .clickable(enabled = item.hasFiles) { onClick() }
            .padding(horizontal = CleanXCompactPadding / 2, vertical = CleanXCompactPadding / 4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(WhatsAppCategoryTouchSize),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(WhatsAppCategoryIconBoxSize)
                    .clip(RoundedCornerShape(CleanXCompactPadding))
                    .background(item.category.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(item.category.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(WhatsAppCategoryIconSize)
                )
            }
            if (item.selected) {
                WhatsAppSelectionBadge(
                    selected = true,
                    enabled = true,
                    modifier = Modifier.size(WhatsAppSelectionSmallSize)
                )
            }
        }
        Spacer(modifier = Modifier.height(CleanXCompactPadding - 2.dp))
        Text(
            text = stringResource(item.category.displayNameRes),
            color = CleanXText,
            fontSize = CleanXTextTiny,
            lineHeight = CleanXLineTiny,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(CleanXCompactPadding / 2))
        Text(
            text = FileSizeFormatter.format(item.totalBytes),
            color = CleanXMutedText,
            fontSize = CleanXTextTiny,
            lineHeight = CleanXLineTiny
        )
    }
}

@Composable
private fun WhatsAppSelectionBadge(
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(WhatsAppSelectionSize)
            .clip(CircleShape)
            .background(
                when {
                    selected -> Color(0xFF5850EC)
                    enabled -> Color.Transparent
                    else -> Color(0xFFE2E8F0)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(WhatsAppSelectionCheckSize)
            )
        } else if (enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .borderCompat()
            )
        }
    }
}

@Composable
private fun WhatsAppResultContent(
    deletedCount: Int,
    deletedBytes: Long,
    onBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    val deletedSizeLabel = FileSizeFormatter.format(deletedBytes)

    CommonResultScreen(
        titleRes = R.string.whatsapp_cleaner,
        onBack = onBack,
        onNavigateTool = onNavigateTool,
        excludedToolRoutes = setOf(Screen.WhatsAppCleaner.route)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CommonResultCheckIcon()
            Spacer(modifier = Modifier.height(CleanXLargePadding))
            Text(
                stringResource(R.string.all_junk_files_removed),
                color = Color(0xFF8797AF),
                fontSize = CleanXTextBody
            )
            if (deletedCount > 0 || deletedBytes > 0L) {
                Spacer(modifier = Modifier.height(CleanXCompactPadding))
                Text(
                    text = stringResource(R.string.removed_files_size, deletedCount, deletedSizeLabel),
                    color = CleanXText,
                    fontSize = CleanXTextBodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WhatsAppErrorContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(WhatsAppErrorTopPadding))
        CommonResultCheckIcon(size = WhatsAppLogoSize)
        Spacer(modifier = Modifier.height(WhatsAppActionGap))
        Text(
            text = errorMessage,
            color = CleanXMutedText,
            fontSize = CleanXTextBody,
            lineHeight = CleanXLineSubtitle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(WhatsAppActionGap))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(CleanXButtonHeight),
            shape = CleanXPillShape,
            colors = ButtonDefaults.buttonColors(containerColor = CleanXBlue),
            contentPadding = PaddingValues(horizontal = CleanXButtonHorizontalPadding)
        ) {
            Text(
                stringResource(R.string.retry),
                color = Color.White,
                fontSize = WhatsAppRetryTextSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val WhatsAppCleanerGroup.displayNameRes: Int
    @StringRes
    get() = when (this) {
        WhatsAppCleanerGroup.Cache -> R.string.cache
        WhatsAppCleanerGroup.File -> R.string.file
    }

private val WhatsAppCleanerCategory.displayNameRes: Int
    @StringRes
    get() = when (this) {
        WhatsAppCleanerCategory.Images -> R.string.images
        WhatsAppCleanerCategory.Videos -> R.string.videos
        WhatsAppCleanerCategory.Audios -> R.string.whatsapp_category_audios
        WhatsAppCleanerCategory.Documents -> R.string.whatsapp_category_documents
        WhatsAppCleanerCategory.Databases -> R.string.whatsapp_category_databases
        WhatsAppCleanerCategory.Other -> R.string.other
    }

private val WhatsAppCleanerCategory.iconRes: Int
    get() = when (this) {
        WhatsAppCleanerCategory.Images -> R.drawable.ic_photos
        WhatsAppCleanerCategory.Videos -> R.drawable.ic_videos
        WhatsAppCleanerCategory.Audios -> R.drawable.ic_audios
        WhatsAppCleanerCategory.Documents -> R.drawable.ic_documents
        WhatsAppCleanerCategory.Databases -> R.drawable.ic_file_blue
        WhatsAppCleanerCategory.Other -> R.drawable.ic_file
    }

private val WhatsAppCleanerCategory.iconBackground: Color
    get() = when (this) {
        WhatsAppCleanerCategory.Images -> Color(0xFFF7ECFF)
        WhatsAppCleanerCategory.Videos -> Color(0xFFFFECF5)
        WhatsAppCleanerCategory.Audios -> Color(0xFFEAF4FF)
        WhatsAppCleanerCategory.Documents -> Color(0xFFFFF6E5)
        WhatsAppCleanerCategory.Databases -> Color(0xFFEFF3FF)
        WhatsAppCleanerCategory.Other -> Color(0xFFE9FFF9)
    }

private fun String.splitSizeParts(): Pair<String, String> {
    val number = takeWhile { it.isDigit() || it == '.' }.ifBlank { "0" }
    val unit = dropWhile { it.isDigit() || it == '.' }.trim().ifBlank { "B" }
    return number to unit
}

@Composable
private fun Modifier.borderCompat(): Modifier = this.background(Color.Transparent)

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewWhatsAppCleanerScreen() {
    QuickCleanTheme {
        WhatsAppCleanerScreen(
            onBack = {},
            onNavigateTool = {}
        )
    }
}
