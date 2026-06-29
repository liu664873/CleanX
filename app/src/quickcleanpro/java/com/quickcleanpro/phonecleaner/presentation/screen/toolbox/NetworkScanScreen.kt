package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import android.provider.Settings
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkScanScreen(
    onBack: () -> Unit,
    onDevices: () -> Unit
) {
    NetworkScanScreenState(
        onBack = onBack,
        onDevices = onDevices,
        viewModel = viewModel()
    )
}

@Composable
internal fun NetworkScanRoute(
    onBack: () -> Unit,
    onDevices: () -> Unit
) {
    NetworkScanScreenState(
        onBack = onBack,
        onDevices = onDevices,
        viewModel = koinViewModel()
    )
}

@Composable
private fun NetworkScanScreenState(
    onBack: () -> Unit,
    onDevices: () -> Unit,
    viewModel: NetworkScanViewModel
) {
    val context = LocalContext.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scanState = uiState.scanState

    fun startNetworkScan() {
        if (uiState.scanState == NetworkScanState.Running || !uiState.hasWifi) return
        permissionCoordinator.guard(
            action = CleanXProtectedAction.NetworkScanStart,
            onGranted = viewModel::startScan,
        )
    }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNetworkStateUntilWifiConnected()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ToolboxScaffold(
        titleRes = R.string.network_scan,
        onBack = onBack,
        bottom = {
            if (scanState == NetworkScanState.Done) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            startNetworkScan()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CleanXBlue)
                    ) {
                        Text(stringResource(R.string.scan_again), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { openSystemSettings(context, Settings.ACTION_WIFI_SETTINGS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = CleanXBlue)
                    ) {
                        Text(stringResource(R.string.switch_wifi), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                PrimaryBottomButton(
                    text = stringResource(
                        if (scanState == NetworkScanState.Running) R.string.scanning_devices else R.string.scan_wifi
                    ),
                    enabled = scanState == NetworkScanState.Running || uiState.hasWifi,
                    onClick = { startNetworkScan() }
                )
            }
        }
    ) {
        ScanResultCard(ssid = uiState.scan?.ssid ?: uiState.networkInfo.ssid, lastScan = uiState.scanTime)
        Spacer(modifier = Modifier.height(14.dp))
        if (!uiState.hasWifi && scanState != NetworkScanState.Running) {
            PermissionPromptCard(
                title = stringResource(R.string.wifi_not_connected),
                description = stringResource(R.string.network_scan_no_wifi_desc),
                action = stringResource(R.string.wifi),
                onClick = { openSystemSettings(context, Settings.ACTION_WIFI_SETTINGS) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
        ScanDetailsCard(scanState = scanState, completedDetailCount = uiState.completedDetailCount)
        if (scanState == NetworkScanState.Done) {
            Spacer(modifier = Modifier.height(18.dp))
            DevicesSummaryCard(devices = uiState.scan?.devices.orEmpty(), onClick = onDevices)
        }
    }
}

private fun openSystemSettings(context: Context, action: String) {
    runCatching { context.startActivity(Intent(action)) }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewNetworkScanScreen() {
    QuickCleanTheme { NetworkScanScreen(onBack = {}, onDevices = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewNetworkScanDevicesScreen() {
    QuickCleanTheme { NetworkScanDevicesScreen(onBack = {}) }
}

@Composable
fun NetworkScanDevicesScreen(onBack: () -> Unit) {
    NetworkScanDevicesScreenState(
        onBack = onBack,
        viewModel = viewModel()
    )
}

@Composable
internal fun NetworkScanDevicesRoute(onBack: () -> Unit) {
    NetworkScanDevicesScreenState(
        onBack = onBack,
        viewModel = koinViewModel()
    )
}

@Composable
private fun NetworkScanDevicesScreenState(
    onBack: () -> Unit,
    viewModel: NetworkScanDevicesViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val devices = uiState.devices
    ToolboxScaffold(titleRes = R.string.network_scan, onBack = onBack) {
        Text(
            text = stringResource(R.string.devices_count, devices.size),
            color = CleanXText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.isLoading) {
            Text(stringResource(R.string.scanning_devices), color = CleanXMutedText, fontSize = 16.sp)
        } else if (devices.isEmpty()) {
            Text(stringResource(R.string.no_devices_on_wifi), color = CleanXMutedText, fontSize = 16.sp)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                devices.forEach { device ->
                    DeviceCard(device = device)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
