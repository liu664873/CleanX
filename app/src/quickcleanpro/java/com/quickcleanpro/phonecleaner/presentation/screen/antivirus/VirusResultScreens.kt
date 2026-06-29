package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.app.LocalExternalActivityLaunchHandler
import com.quickcleanpro.phonecleaner.presentation.common.CommonResultScreen
import java.io.File

@Composable
fun ScanVirusResultScreen(
    viewModel: VirusScanViewModel,
    onBack: () -> Unit,
    onNoThreats: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val externalActivityLaunchHandler = LocalExternalActivityLaunchHandler.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val deletionFailedText = stringResource(R.string.deletion_failed)
    var fileToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshAdbRisk()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAdbRisk()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.effectiveThreatCount) {
        if (uiState.effectiveThreatCount == 0) {
            onNoThreats()
        }
    }

    DisposableEffect(context) {
        val receiver = PackageRemovedReceiver { packageName ->
            viewModel.removeThreatByPackage(packageName)
        }
        val filter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply {
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    fileToDelete?.let { path ->
        DeleteVirusFileDialog(
            onConfirm = {
                fileToDelete = null
                if (File(path).safeDelete(context)) {
                    viewModel.removeThreatByFilePath(path)
                } else {
                    Toast.makeText(context, deletionFailedText, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { fileToDelete = null }
        )
    }

    VirusPageScaffold(onBack = onBack, bottomPadding = 40.dp) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_in_danger),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.in_danger),
                        color = VirusTitle,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = buildThreatCountText(uiState.effectiveThreatCount),
                    color = VirusSecondary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(30.dp))
            }

            if (uiState.hasAdbRisk) {
                item {
                    AdbRiskCard(
                        onSolve = { openDeveloperSettings(context, externalActivityLaunchHandler) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            items(
                items = uiState.threats,
                key = { threat -> threat.id }
            ) { threat ->
                VirusThreatCard(
                    threat = threat,
                    onSolve = {
                        if (threat.isFile) {
                            threat.apkPath?.let { fileToDelete = it }
                        } else {
                            openAppSettings(context, threat.packageName, externalActivityLaunchHandler)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun NoVirusResultScreen(
    onBack: () -> Unit,
    onNavigateTool: (String) -> Unit
) {
    CommonResultScreen(
        title = stringResource(R.string.anti_virus),
        onBack = onBack,
        onNavigateTool = onNavigateTool
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_no_virus),
                contentDescription = null,
                modifier = Modifier.size(45.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.no_threats_found),
                color = VirusSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun buildThreatCountText(count: Int) = buildAnnotatedString {
    withStyle(SpanStyle(color = VirusOrange)) {
        append(count.toString())
    }
    append(" ")
    append(stringResource(R.string.threats_found))
}
