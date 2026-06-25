package com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.screen.tools.networkscan.NetworkScanUiState

@Composable
internal fun NetworkScanScanningView(uiState: NetworkScanUiState) {
    NetworkScanResultCard(uiState = uiState)
    Spacer(modifier = Modifier.height(20.dp))
    NetworkScanDetailsCard(uiState = uiState)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = stringResource(R.string.scanning_devices),
        color = NetworkScanNavyMuted,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    )
    Spacer(modifier = Modifier.height(96.dp))
}
