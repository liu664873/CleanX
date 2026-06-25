package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.app.LocalExternalActivityLaunchHandler
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.DeleteVirusFileDialog
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views.NoVirusResultView
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views.VirusThreatResultView
import java.io.File

@Composable
fun ScanVirusResultScreen(
    viewModel: VirusScanViewModel,
) {
    val router = LocalRouter.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val externalActivityLaunchHandler = LocalExternalActivityLaunchHandler.current
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
            router.replaceCurrent(Screen.NoVirusResult)
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

    VirusThreatResultView(
        uiState = uiState,
        onSolveAdbRisk = {
            openDeveloperSettings(
                context = context,
                externalActivityLaunchHandler = externalActivityLaunchHandler,
            )
        },
        onSolveThreat = { threat ->
            if (threat.isFile) {
                threat.apkPath?.let { fileToDelete = it }
            } else {
                openAppSettings(
                    context = context,
                    packageName = threat.packageName,
                    externalActivityLaunchHandler = externalActivityLaunchHandler,
                )
            }
        },
    )
}

@Composable
fun NoVirusResultScreen() {
    val router = LocalRouter.current
    CleanXScaffoldPage(
        title = stringResource(R.string.anti_virus),
        onBack = { router.goBack() },
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
    ) {
        NoVirusResultView(
            onNavigateTool = { route -> router.navigateAndClearStack(route) },
            modifier = Modifier.fillMaxSize()
        )
    }
}
