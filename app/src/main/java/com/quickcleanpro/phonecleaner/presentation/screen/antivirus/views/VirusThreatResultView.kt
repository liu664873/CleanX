package com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.AdbRiskCard
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusOrange
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusPageScaffold
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusScanUiState
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusSecondary
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusThreat
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusThreatCard
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusTitle

@Composable
internal fun VirusThreatResultView(
    uiState: VirusScanUiState,
    onSolveAdbRisk: () -> Unit,
    onSolveThreat: (VirusThreat) -> Unit,
) {
    VirusPageScaffold(bottomPadding = 40.dp) {
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
                    AdbRiskCard(onSolve = onSolveAdbRisk)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            items(
                items = uiState.threats,
                key = { threat -> threat.id }
            ) { threat ->
                VirusThreatCard(
                    threat = threat,
                    onSolve = { onSolveThreat(threat) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
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
