package com.quickcleanpro.phonecleaner.presentation.screen.result

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.CategoryCleanGroup
import com.quickcleanpro.phonecleaner.domain.model.CleanItem
import com.quickcleanpro.phonecleaner.domain.model.CleanResult
import com.quickcleanpro.phonecleaner.domain.model.JunkCategory
import com.quickcleanpro.phonecleaner.domain.model.JunkFile
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXFullScreenDeleteAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTopAppBar
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

private val CleanXBackground = Color(0xFFF7FAFD)
private val CleanXText = Color(0xFF2D3748)
private val CleanXMutedText = Color(0xFF8190A5)
private val ResultCardBg = Color(0xFFF6F7FB)
private val ResultNavy = Color(0xFF1D2959)
private val ResultOrange = Color(0xFFFC7941)
private val ResultDivider = Color(0x261D2959)
private val ResultIconBg = Color(0xA6FFFAEF)

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onCleanComplete: (CleanResult) -> Unit,
) {
    val context = LocalContext.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val selectedSummary by viewModel.selectedSummary.collectAsStateWithLifecycle()
    val deleteAuthorizationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            viewModel.handleAuthorizationResult(result.resultCode == Activity.RESULT_OK)
        }

    LaunchedEffect(screenState) {
        when (val state = screenState) {
            is ResultViewModel.ScreenState.AwaitingDeleteAuthorization -> {
                deleteAuthorizationLauncher.launch(
                    IntentSenderRequest.Builder(state.deleteRequest.intentSender).build(),
                )
            }
            is ResultViewModel.ScreenState.Completed -> {
                delay(850L)
                onCleanComplete(state.result)
            }
            else -> Unit
        }
    }

    if (screenState is ResultViewModel.ScreenState.Cleaning) {
        CleaningContent()
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CleanXBackground,
        topBar = {
            CleanXTopAppBar(title = stringResource(R.string.junk_removal))
        },
        bottomBar = {
            if (screenState is ResultViewModel.ScreenState.Preview) {
                ResultBottomBar(
                    selectedSummary = selectedSummary,
                    onClean = { viewModel.startCleaning(context) },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(CleanXBackground)
                    .padding(paddingValues)
                    .padding(horizontal = 13.dp),
        ) {
            when (val state = screenState) {
                is ResultViewModel.ScreenState.Preview ->
                    ResultListContent(
                        groups = state.groups,
                        checkedEmptyCategories = state.checkedEmptyCategories,
                        selectedSummary = selectedSummary,
                        viewModel = viewModel,
                    )
                is ResultViewModel.ScreenState.AwaitingDeleteAuthorization ->
                    AwaitingAuthorizationContent(state.message)
                is ResultViewModel.ScreenState.Error ->
                    ErrorContent(
                        message = state.message ?: stringResource(state.messageRes),
                    )
                is ResultViewModel.ScreenState.Completed ->
                    CleaningCompleteContent()
                is ResultViewModel.ScreenState.Cleaning -> Unit
            }
        }
    }
}

@Composable
internal fun ResultBottomBar(
    selectedSummary: SelectionSummary,
    onClean: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val onlyZeroByteSelection =
            selectedSummary.checkedCount == 0 && selectedSummary.checkedEmptyCategoryCount > 0
        val buttonText =
            when {
                selectedSummary.checkedCount > 0 ->
                    stringResource(R.string.remove_size, compactSizeLabel(selectedSummary.checkedSize))
                onlyZeroByteSelection -> stringResource(R.string.result_zero_byte_selection_button)
                else -> stringResource(R.string.result_select_items_button)
            }
        if (onlyZeroByteSelection) {
            Text(
                text = stringResource(R.string.result_zero_byte_selection_hint),
                color = CleanXMutedText,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
            )
        }
        CleanXPrimaryButton(
            text = buttonText,
            onClick = onClean,
            enabled = selectedSummary.checkedCount > 0,
            height = 52.dp,
            cornerRadius = 10.dp,
        )
    }
}

@Composable
internal fun AwaitingAuthorizationContent(message: String) {
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
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun ResultListContent(
    groups: List<CategoryCleanGroup>,
    checkedEmptyCategories: Set<JunkCategory>,
    selectedSummary: SelectionSummary,
    viewModel: ResultViewModel,
) {
    val rows = displayGroups(groups)
    var expandedIndex by remember { mutableStateOf(rows.indexOfFirst { it.group.items.isNotEmpty() }) }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SummaryCard(totalSize = selectedSummary.checkedSize)
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ResultCardBg,
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                    rows.forEachIndexed { rowIndex, row ->
                        val items = row.group.items
                        CategoryGroupSection(
                            row = row,
                            checked =
                                row.group.items.takeIf { it.isNotEmpty() }?.all { it.isChecked }
                                    ?: (row.group.category in checkedEmptyCategories),
                            expanded = expandedIndex == rowIndex && items.isNotEmpty(),
                            onToggleExpanded = {
                                expandedIndex = if (expandedIndex == rowIndex) -1 else rowIndex
                            },
                            onToggleCategorySelection = {
                                viewModel.toggleCategorySelection(row.group.category)
                            },
                            onToggleItem = { itemIndex ->
                                row.sourceIndex?.let { sourceIndex ->
                                    viewModel.toggleItemSelection(sourceIndex, itemIndex)
                                }
                            },
                        )
                        if (rowIndex < rows.lastIndex) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(ResultDivider),
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }
}

@Composable
private fun SummaryCard(totalSize: Long) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(79.dp),
        color = ResultCardBg,
        shape = RoundedCornerShape(12.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${stringResource(R.string.occupying)}:",
                    color = ResultNavy,
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayMainSize(totalSize),
                        color = ResultOrange,
                        fontSize = 34.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayUnit(totalSize),
                        color = ResultNavy,
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.junk_result_trash_bucket),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-16).dp, y = (-10).dp)
                        .size(width = 75.dp, height = 97.dp),
            )
        }
    }
}

@Composable
private fun CategoryGroupSection(
    row: ResultDisplayRow,
    checked: Boolean,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onToggleCategorySelection: () -> Unit,
    onToggleItem: (Int) -> Unit,
) {
    val group = row.group
    val items = group.items

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(row.titleRes),
                    color = ResultNavy,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ResultNavy,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clickable(enabled = items.isNotEmpty()) { onToggleExpanded() },
                )
            }
            Row(
                modifier = Modifier.widthIn(min = 120.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = group.formattedTotalSize,
                    color = ResultNavy,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                RoundCheckButton(
                    checked = checked,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clickable { onToggleCategorySelection() },
                )
            }
        }

        if (expanded && items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items.forEachIndexed { index, item ->
                    JunkItemCard(
                        item = item,
                        modifier = Modifier.width(55.dp),
                        onClick = { onToggleItem(index) },
                    )
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
        modifier =
            modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ResultIconBg),
                contentAlignment = Alignment.Center,
            ) {
                JunkItemIcon(item = item)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.fileName.ifBlank { stringResource(R.string.unnamed_file) },
            color = Color(0xA62D3748),
            fontSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.formattedSize,
            color = Color(0xA62D3748),
            fontSize = 12.sp,
            lineHeight = 16.sp,
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(6.dp),
            contentScale = ContentScale.Fit,
        )
    } else {
        FileGlyph(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun RoundCheckButton(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(CircleShape)
                .background(if (checked) CleanXBlue else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color(0xFFC8D2DE),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
        }
    }
}

@Composable
private fun FileGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val orange = Color(0xFFFFA31A)
        val fold = Color(0xFFFFD17C)
        drawRoundRect(
            color = orange,
            topLeft = Offset(size.width * 0.24f, size.height * 0.10f),
            size = Size(size.width * 0.52f, size.height * 0.80f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        )
        drawPath(
            path =
                androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.56f, size.height * 0.10f)
                    lineTo(size.width * 0.76f, size.height * 0.30f)
                    lineTo(size.width * 0.56f, size.height * 0.30f)
                    close()
                },
            color = fold,
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(size.width * 0.38f, size.height * 0.48f),
            size = Size(size.width * 0.30f, 2.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx()),
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(size.width * 0.38f, size.height * 0.62f),
            size = Size(size.width * 0.22f, 2.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx(), 1.dp.toPx()),
        )
    }
}

@Composable
internal fun CleaningContent() {
    CleanXFullScreenDeleteAnimation(
        fallbackText = stringResource(R.string.cleaning_selected_files),
    )
}

@Composable
internal fun CleaningCompleteContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 248.dp, height = 149.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val glowRadius = 63.dp.toPx()

                drawCircle(
                    color = Color(0x334179FC),
                    radius = glowRadius,
                    center = center,
                )
                drawCircle(
                    color = Color(0x1A4179FC),
                    radius = glowRadius + 16.dp.toPx(),
                    center = center,
                )
                drawCircle(
                    color = Color(0xFF4179FC),
                    radius = 7.5.dp.toPx(),
                    center = Offset(center.x - 118.dp.toPx(), center.y - 4.dp.toPx()),
                )
                drawCircle(
                    color = Color(0xFF67E3AD),
                    radius = 6.5.dp.toPx(),
                    center = Offset(center.x + 119.dp.toPx(), center.y - 13.dp.toPx()),
                )
                drawLine(
                    color = Color(0xFF67E3AD),
                    start = Offset(center.x - 96.dp.toPx(), center.y - 36.dp.toPx()),
                    end = Offset(center.x - 78.dp.toPx(), center.y - 30.dp.toPx()),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color(0xFF4179FC),
                    start = Offset(center.x + 83.dp.toPx(), center.y - 57.dp.toPx()),
                    end = Offset(center.x + 104.dp.toPx(), center.y - 63.dp.toPx()),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            Box(
                modifier =
                    Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2F4FF)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_ok),
                    contentDescription = null,
                    modifier = Modifier.size(69.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
internal fun ErrorContent(message: String) {
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
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

private fun displayMainSize(size: Long): String {
    val formatted = JunkFile.formatFileSize(size)
    return formatted.takeWhile { it.isDigit() || it == '.' }.ifBlank { "0" }
}

private fun displayUnit(size: Long): String {
    val formatted = JunkFile.formatFileSize(size)
    return formatted.dropWhile { it.isDigit() || it == '.' }.trim().ifBlank { "B" }
}

internal fun compactSizeLabel(size: Long): String {
    val mainSize = displayMainSize(size)
    val unit = displayUnit(size)
    return "$mainSize$unit"
}

private data class ResultDisplayRow(
    @StringRes val titleRes: Int,
    val group: CategoryCleanGroup,
    val sourceIndex: Int?,
)

private val apkIconBitmapCache = ConcurrentHashMap<String, ImageBitmap>()

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

    val drawable = runCatching { applicationInfo.loadIcon(packageManager) }.getOrNull() ?: return null
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

private fun displayGroups(groups: List<CategoryCleanGroup>): List<ResultDisplayRow> {
    fun row(
        @StringRes titleRes: Int,
        category: JunkCategory,
    ): ResultDisplayRow {
        val index = groups.indexOfFirst { it.category == category }
        val group = groups.getOrNull(index) ?: CategoryCleanGroup(category, emptyList())
        return ResultDisplayRow(titleRes = titleRes, group = group, sourceIndex = index.takeIf { it >= 0 })
    }

    val residualIndex =
        groups.indexOfFirst { it.category == JunkCategory.RESIDUAL }
            .takeIf { it >= 0 }
            ?: groups.indexOfFirst { it.category == JunkCategory.DUPLICATE }.takeIf { it >= 0 }
    val residualGroup = residualIndex?.let { groups[it] } ?: CategoryCleanGroup(JunkCategory.RESIDUAL, emptyList())

    val otherIndex = groups.indexOfFirst { it.category == JunkCategory.LARGE_FILE }.takeIf { it >= 0 }
    val otherGroup = otherIndex?.let { groups[it] } ?: CategoryCleanGroup(JunkCategory.LARGE_FILE, emptyList())

    return listOf(
        row(R.string.junk_group_system_cache, JunkCategory.CACHE),
        row(R.string.junk_group_ad_junk_files, JunkCategory.TEMP_FILE),
        ResultDisplayRow(R.string.junk_group_residual_junks, residualGroup, residualIndex),
        row(R.string.junk_group_obsolete_apks, JunkCategory.APK),
        ResultDisplayRow(R.string.junk_group_other_junk_files, otherGroup, otherIndex),
    )
}
