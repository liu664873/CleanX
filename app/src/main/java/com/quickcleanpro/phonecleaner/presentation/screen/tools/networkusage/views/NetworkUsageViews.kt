package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXSegmentedTabs
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTabItem
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.PackageAppIcon
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionItem
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage.NetworkUsageDisplayItem
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage.NetworkUsageUiState
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkusage.NetworkUsageViewModel
import com.quickcleanpro.phonecleaner.utils.FileSizeFormatter

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val TitleNavy = Color(0xFF2D3748)
private val NavyMuted = Color(0xA61D2959)
private val ValueMuted = Color(0xFF8190A5)
private val Divider15 = Color(0x332D3748)
private val CardRadius = 12.dp

@Composable
internal fun NetworkUsageScreenState(viewModel: NetworkUsageViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAfterResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.network_usage),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        if (!uiState.hasAccess) {
            NetworkUsageAccessRequiredCard(
                onGrantClick = {
                    permissionCoordinator.request(CleanXPermissionItem.UsageAccess) {
                        viewModel.refreshAfterResume()
                    }
                },
            )
        } else {
            NetworkUsageTabs(
                uiState = uiState,
                onSelected = viewModel::selectTab,
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                uiState.isLoading && uiState.usage == null -> NetworkUsageLoadingCard()
                uiState.selectedTotalBytes <= 0L -> EmptyNetworkState()
                else -> {
                    NetworkSummaryCard(uiState = uiState)
                    Spacer(modifier = Modifier.height(16.dp))
                    NetworkAppList(items = uiState.displayItems)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun NetworkUsageAccessRequiredCard(onGrantClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(R.string.app_usage_permission_title),
                color = Navy,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.permission_network_usage_desc),
                color = NavyMuted,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            CleanXPrimaryButton(
                text = stringResource(R.string.allow_now),
                onClick = onGrantClick,
            )
        }
    }
}

@Composable
private fun NetworkUsageTabs(
    uiState: NetworkUsageUiState,
    onSelected: (Int) -> Unit,
) {
    CleanXSegmentedTabs(
        modifier = Modifier.padding(horizontal = 16.dp),
        items =
            listOf(
                CleanXTabItem(
                    title = stringResource(R.string.cellular_today),
                    value = formatNetworkBytes(uiState.cellularTotalBytes),
                ),
                CleanXTabItem(
                    title = stringResource(R.string.wifi_today),
                    value = formatNetworkBytes(uiState.wifiTotalBytes),
                ),
            ),
        selectedIndex = uiState.selectedIndex,
        onSelected = onSelected,
        cornerRadius = 12.dp,
        horizontalSpacing = 38.dp,
        horizontalPadding = 10.dp,
        verticalPadding = 8.dp,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        valueFontSize = 14.sp,
        valueLineHeight = 18.sp,
        unselectedContainerColor = Color.Transparent,
    )
}

@Composable
private fun NetworkUsageLoadingCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.result_loading),
                color = NavyMuted,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun EmptyNetworkState() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 74.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Image(
            painter = painterResource(id = R.drawable.network_usage_empty),
            contentDescription = null,
            modifier = Modifier.size(256.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun NetworkSummaryCard(uiState: NetworkUsageUiState) {
    val usage = uiState.usage
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(98.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SummaryColumn(
                value = formatNetworkBytes(uiState.selectedTotalBytes),
                label =
                    stringResource(
                        if (usage?.isToday == false) {
                            R.string.total_usage_since_boot
                        } else {
                            R.string.total_usage_today
                        },
                    ).replace('\n', ' '),
            )
            SummaryDivider()
            SummaryColumn(
                value = formatNetworkBytes(uiState.selectedRxBytes),
                label =
                    stringResource(
                        if (usage?.isToday == false) {
                            R.string.downloads_since_boot
                        } else {
                            R.string.downloads_today
                        },
                    ).replace('\n', ' '),
            )
            SummaryDivider()
            SummaryColumn(
                value = formatNetworkBytes(uiState.selectedTxBytes),
                label =
                    stringResource(
                        if (usage?.isToday == false) {
                            R.string.uploads_since_boot
                        } else {
                            R.string.uploads_today
                        },
                    ).replace('\n', ' '),
            )
        }
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier =
            Modifier
                .width(1.dp)
                .height(18.dp)
                .background(Divider15),
    )
}

@Composable
private fun SummaryColumn(
    value: String,
    label: String,
) {
    Column(
        modifier = Modifier.width(89.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = value,
            color = Navy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            color = Navy,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NetworkAppList(items: List<NetworkUsageDisplayItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            val maxUsage = items.maxOfOrNull { it.totalBytes }?.coerceAtLeast(1L) ?: 1L
            items.forEachIndexed { index, item ->
                NetworkAppRow(
                    item = item,
                    progress = (item.totalBytes.toFloat() / maxUsage).coerceIn(0.08f, 1f),
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(color = Divider15, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun NetworkAppRow(
    item: NetworkUsageDisplayItem,
    progress: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PackageAppIcon(
            packageName = item.packageName,
            fallbackText = if (item.isAggregate) "S" else item.appName.take(1).ifBlank { "A" },
            color = CleanXBlue,
            isAggregate = item.isAggregate,
            modifier = Modifier.size(44.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text =
                        if (item.isAggregate) {
                            stringResource(R.string.system_unknown_traffic)
                        } else {
                            item.appName
                        },
                    color = TitleNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatNetworkBytes(item.totalBytes, compact = true),
                    color = ValueMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                )
            }
            RoundedProgressBar(
                progress = progress,
                width = 209.dp,
                height = 4.dp,
                trackColor = Navy.copy(alpha = 0.15f),
                fillColor = CleanXBlue,
            )
        }
    }
}

private fun formatNetworkBytes(
    bytes: Long,
    compact: Boolean = false,
): String {
    val value = FileSizeFormatter.format(bytes.coerceAtLeast(0L))
    return if (compact) value.replace(" ", "") else value
}
