package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScanRingAnimation
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.cleanXTabContentSwipe
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme

/**
 * App Usage 妞ょ敻娼伴妴? *
 * 妞ょ敻娼伴崣顏囩鐠愶絾瑕嗛弻鎾跺Ц閹降鈧礁鎼锋惔鏃傚仯閸戣绨ㄦ禒璺烘嫲閸氼垰濮╃化鑽ょ埠鐠佸墽鐤嗛幋鏍х安閻劏顕涢幆鍛淬€夐妴? * 閺冦儲婀￠懠鍐ㄦ纯娑撳濯洪惃鍕潔瀵偓閹椒绮涙穱婵堟殌閸?UI 鐏炲偊绱濋崶鐘辫礋鐎瑰啩绗夌仦鐐扮艾娑撴艾濮熼弫鐗堝祦閵? */
@Composable
fun AppUsageScreen(
    onBack: () -> Unit,
    onPermissionDenied: () -> Unit = onBack
) {
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val viewModel: AppUsageViewModel = koinViewModel()
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(permissionCoordinator) {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.AppUsageLoadStats,
            onGranted = {
                permissionGranted = true
                viewModel.refreshAfterResume()
            },
            onRejected = onPermissionDenied,
        )
    }

    CleanXScaffold(titleRes = R.string.app_usage, onBack = onBack) { paddingValues ->
        if (permissionGranted) {
            AppUsageScreenState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                viewModel = viewModel
            )
        }
    }
}

@Composable
internal fun AppUsageRoute(
    onBack: () -> Unit,
    onPermissionDenied: () -> Unit = onBack
) {
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val viewModel: AppUsageViewModel = koinViewModel()
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(permissionCoordinator) {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.AppUsageLoadStats,
            onGranted = {
                permissionGranted = true
                viewModel.refreshAfterResume()
            },
            onRejected = onPermissionDenied,
        )
    }

    CleanXScaffold(titleRes = R.string.app_usage, onBack = onBack) { paddingValues ->
        if (permissionGranted) {
            AppUsageScreenState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun AppUsageScreenState(
    modifier: Modifier = Modifier,
    viewModel: AppUsageViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var rangeExpanded by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshRunningPackagesOnly()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AppUsageContent(
        uiState = uiState,
        rangeExpanded = rangeExpanded,
        onRangeExpandedChange = { rangeExpanded = it },
        onRangeSelected = viewModel::selectRange,
        onTabSelected = viewModel::selectTab,
        onStopApp = { packageName ->
            context.startActivity(viewModel.appInfoIntent(packageName))
        },
        modifier = modifier
    )
}

@Composable
private fun AppUsageContent(
    uiState: AppUsageUiState,
    rangeExpanded: Boolean,
    onRangeExpandedChange: (Boolean) -> Unit,
    onRangeSelected: (AppUsageDateRange) -> Unit,
    onTabSelected: (AppUsageMetricTab) -> Unit,
    onStopApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listScrollState = rememberScrollState()

    LaunchedEffect(uiState.selectedRange, uiState.selectedTab, uiState.isLoading) {
        listScrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanXBackground)
            .padding(horizontal = 16.dp)
    ) {
        AppUsageRangeSelector(
            selectedRange = uiState.selectedRange,
            expanded = rangeExpanded,
            onExpandedChange = onRangeExpandedChange,
            onRangeSelected = onRangeSelected
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .navigationBarsPadding()
            ) {
                AppUsageLoadingCard()
            }
        } else {
            AppUsageChartCard(label = uiState.totalUsageLabel)
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    AppUsageTabsHeader(
                        selectedTab = uiState.selectedTab,
                        onSelected = onTabSelected
                    )
                    InfoDivider()
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .navigationBarsPadding()
                    .cleanXTabContentSwipe(
                        selectedIndex = AppUsageMetricTab.values().indexOf(uiState.selectedTab).coerceAtLeast(0),
                        itemCount = AppUsageMetricTab.values().size,
                        onSelected = { index ->
                            AppUsageMetricTab.values().getOrNull(index)?.let(onTabSelected)
                        }
                    ),
                color = Color.White,
                shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(listScrollState)
                        .padding(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    AppUsageRowsContent(
                        items = uiState.visibleItems,
                        onStopApp = onStopApp
                    )
                }
            }
        }
    }
}

@Composable
private fun AppUsageRangeSelector(
    selectedRange: AppUsageDateRange,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRangeSelected: (AppUsageDateRange) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val menuWidth = maxWidth.coerceAtMost(343.dp)
        val rowWidth = (menuWidth - 32.dp).coerceAtLeast(0.dp)
        val menuOffsetX = (maxWidth - menuWidth).coerceAtLeast(0.dp)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable { onExpandedChange(!expanded) },
            color = Color.White,
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(selectedRange.labelRes),
                    color = CleanXText,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CleanXText,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
        }
        AppUsageRangeMenu(
            expanded = expanded,
            selectedRange = selectedRange,
            menuWidth = menuWidth,
            rowWidth = rowWidth,
            offsetX = menuOffsetX,
            onDismiss = { onExpandedChange(false) },
            onRangeSelected = { range ->
                onRangeSelected(range)
                onExpandedChange(false)
            }
        )
    }
}

@Composable
private fun AppUsageRangeMenu(
    expanded: Boolean,
    selectedRange: AppUsageDateRange,
    menuWidth: Dp,
    rowWidth: Dp,
    offsetX: Dp,
    onDismiss: () -> Unit,
    onRangeSelected: (AppUsageDateRange) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(offsetX, 4.dp),
        modifier = Modifier
            .width(menuWidth)
            .height(188.dp)
            .background(Color.White),
        shape = RoundedCornerShape(12.dp),
        containerColor = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .width(menuWidth)
                .height(188.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            visibleAppUsageRanges.forEachIndexed { index, range ->
                AppUsageRangeMenuItem(
                    range = range,
                    selected = range == selectedRange,
                    rowWidth = rowWidth,
                    onClick = { onRangeSelected(range) }
                )
                if (index != visibleAppUsageRanges.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(rowWidth)
                            .height(1.dp)
                            .background(Color(0xFFD9DEE6))
                    )
                }
            }
        }
    }
}

@Composable
private fun AppUsageRangeMenuItem(
    range: AppUsageDateRange,
    selected: Boolean,
    rowWidth: Dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(rowWidth)
            .height(21.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(range.labelRes),
            color = CleanXText,
            fontSize = 18.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = CleanXBlue,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
    }
}

/**
 * App Usage 閸旂姾娴囬崡鐘辩秴閸楋紕澧栭妴? */
private val visibleAppUsageRanges = listOf(
    AppUsageDateRange.Today,
    AppUsageDateRange.Yesterday,
    AppUsageDateRange.Last7Days,
    AppUsageDateRange.Last30Days
)

@Composable
internal fun AppUsageLoadingCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("AppUsageLoading"),
            contentAlignment = Alignment.Center
        ) {
            CleanXScanRingAnimation(
                modifier = Modifier.size(72.dp),
                backgroundResId = null,
                ringWidth = 6.dp,
                ringColor = CleanXBlue,
                backgroundColor = CleanXBlue.copy(alpha = 0.12f),
                animationDurationMillis = 900
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewAppUsageScreen() {
    QuickCleanTheme { AppUsageScreen(onBack = {}) }
}
