package com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.ThreatDrawableImage
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusCenterBadge
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusPageScaffold
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusProgressTrack
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanMode
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanUiState
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusTitle
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusTrackInactiveLine

@Composable
internal fun VirusScanningView(
    mode: VirusScanMode,
    uiState: VirusScanUiState,
) {
    VirusPageScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                CleanSpiralAnimation {
                    VirusCenterBadge(size = 58.dp) {
                        ThreatDrawableImage(
                            drawable = uiState.currentIcon,
                            fallback = R.mipmap.ic_protection,
                            modifier = Modifier.size(58.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(124.dp))

                Text(
                    text = uiState.currentLabel,
                    color = VirusTitle,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                VirusProgressTrack(
                    mode = mode,
                    progress = uiState.progressFraction,
                    hasAdbRisk = uiState.hasAdbRisk,
                    appThreatCount = uiState.appThreatCount,
                    fileThreatCount = uiState.fileThreatCount
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.powered_by_trustlook),
                    color = VirusTrackInactiveLine.copy(alpha = 0.75f),
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}
