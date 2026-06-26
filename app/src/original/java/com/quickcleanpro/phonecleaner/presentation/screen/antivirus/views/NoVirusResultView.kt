package com.quickcleanpro.phonecleaner.presentation.screen.antivirus.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CommonResultContent
import com.quickcleanpro.phonecleaner.presentation.screen.antivirus.VirusSecondary

@Composable
internal fun NoVirusResultView(
    onNavigateTool: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CommonResultContent(
        onNavigateTool = onNavigateTool,
        modifier = modifier.fillMaxSize()
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
