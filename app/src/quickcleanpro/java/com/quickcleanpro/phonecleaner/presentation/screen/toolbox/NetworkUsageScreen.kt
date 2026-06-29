package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScanRingAnimation
import com.quickcleanpro.phonecleaner.presentation.common.cleanXTabContentSwipe
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

@Composable
fun NetworkUsageScreen(
    onBack: () -> Unit,
    onPermissionDenied: () -> Unit = onBack
) {
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val viewModel: NetworkUsageViewModel = koinViewModel()
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(permissionCoordinator) {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.NetworkUsageLoadStats,
            onGranted = {
                permissionGranted = true
                viewModel.refreshUsage()
            },
            onRejected = onPermissionDenied,
        )
    }

    CleanXScaffold(titleRes = R.string.network_usage, onBack = onBack) { paddingValues ->
        if (permissionGranted) {
            NetworkUsageScreenState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                viewModel = viewModel
            )
        }
    }
}

@Composable
internal fun NetworkUsageRoute(
    onBack: () -> Unit,
    onPermissionDenied: () -> Unit = onBack
) {
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val viewModel: NetworkUsageViewModel = koinViewModel()
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(permissionCoordinator) {
        permissionCoordinator.guard(
            action = CleanXProtectedAction.NetworkUsageLoadStats,
            onGranted = {
                permissionGranted = true
                viewModel.refreshUsage()
            },
            onRejected = onPermissionDenied,
        )
    }

    CleanXScaffold(titleRes = R.string.network_usage, onBack = onBack) { paddingValues ->
        if (permissionGranted) {
            NetworkUsageScreenState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun NetworkUsageScreenState(
    modifier: Modifier = Modifier,
    viewModel: NetworkUsageViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selected = uiState.selectedTab
    val listScrollState = rememberScrollState()

    LaunchedEffect(selected, uiState.usage) {
        listScrollState.scrollTo(0)
    }

    val currentUsage = uiState.usage
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CleanXBackground)
            .padding(horizontal = 16.dp)
    ) {
        TwoTabHeader(
            leftTitle = stringResource(R.string.cellular_today),
            leftValue = currentUsage?.let { formatNetworkBytes(it.cellularTotalBytes) } ?: "--",
            rightTitle = stringResource(R.string.wifi_today),
            rightValue = currentUsage?.let { formatNetworkBytes(it.wifiTotalBytes) } ?: "--",
            selected = selected,
            onSelected = viewModel::selectTab
        )
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .cleanXTabContentSwipe(
                    selectedIndex = selected,
                    itemCount = 2,
                    onSelected = viewModel::selectTab
                )
                .verticalScroll(listScrollState)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            if (uiState.isLoading || currentUsage == null) {
                NetworkUsageLoadingCard()
            } else {
                val selectedTotalBytes = uiState.selectedTotalBytes
                val displayItems = buildNetworkUsageDisplayItems(
                    apps = uiState.selectedApps,
                    selectedTotalBytes = selectedTotalBytes
                )

                if (selectedTotalBytes > 0L) {
                    UsageSummaryCard(usage = currentUsage, selected = selected)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (selectedTotalBytes > 0L && displayItems.isNotEmpty()) {
                    UsageAppsCard(
                        items = displayItems
                    )
                } else {
                    Spacer(modifier = Modifier.height(74.dp))
                    Image(
                        painter = painterResource(id = R.drawable.blank),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(230.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkUsageLoadingCard() {
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
                .testTag("NetworkUsageLoading"),
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
private fun PreviewNetworkUsageScreen() {
    QuickCleanTheme { NetworkUsageScreen(onBack = {}) }
}

/** 鏍煎紡鍖栫綉缁滅敤閲忓瓧鑺傛暟锛岄伩鍏嶉〉闈㈢洿鎺ヤ緷璧栧簳锟?Reader锟?*/
internal fun formatNetworkBytes(bytes: Long): String = FileSizeFormatter.format(bytes)

