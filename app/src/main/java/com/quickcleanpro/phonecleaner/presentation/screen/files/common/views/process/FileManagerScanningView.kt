package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views.process

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.screen.files.common.components.FileManagerNavy
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation

@Composable
internal fun FileManagerScanningView(text: String = "Scanning...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 67.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CleanSpiralAnimation {
                Image(
                    painter = painterResource(R.drawable.ic_file),
                    contentDescription = text,
                    modifier = Modifier.size(70.dp),
                )
            }
            Spacer(modifier = Modifier.height(57.dp))
            Text(
                text = text,
                color = FileManagerNavy,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
