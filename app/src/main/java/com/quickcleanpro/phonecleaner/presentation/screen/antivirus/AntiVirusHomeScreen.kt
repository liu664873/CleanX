package com.quickcleanpro.phonecleaner.presentation.screen.antivirus

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

@Composable
fun AntiVirusScreen(
    viewModel: VirusScanViewModel,
) {
    val router = LocalRouter.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current

    VirusPageScaffold {
        val featureItems = listOf(
            VirusFeatureItem(R.mipmap.ic_virus, stringResource(R.string.virus)),
            VirusFeatureItem(R.mipmap.ic_malware, stringResource(R.string.malware)),
            VirusFeatureItem(R.mipmap.ic_privacy, stringResource(R.string.privacy)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            CleanSpiralAnimation{
                VirusCenterBadge(
                    size = 56.dp,
                    backgroundBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF746B), Color(0xFFEE4D52)),
                    ),
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_virus_small),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.engine_is_ready),
                color = VirusTitle,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
            )

            Spacer(modifier = Modifier.height(31.dp))

            VirusFeatureCard(items = featureItems)

            Spacer(modifier = Modifier.height(24.dp))

            VirusPrimaryButton(
                text = stringResource(R.string.deep_scan),
                onClick = {
                    permissionCoordinator.guard(CleanXProtectedAction.VirusDeepScanStart) {
                        viewModel.resetScanState()
                        router.navigate(Screen.VirusDeepScan)
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            VirusSecondaryButton(
                text = stringResource(R.string.quick_scan),
                onClick = {
                    viewModel.resetScanState()
                    router.navigate(Screen.VirusQuickScan)
                },
            )
        }
    }
}
