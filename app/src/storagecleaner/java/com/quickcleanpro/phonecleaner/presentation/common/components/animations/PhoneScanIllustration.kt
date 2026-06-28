package com.quickcleanpro.phonecleaner.presentation.common.components.animations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R

@Composable
fun PhoneScanIllustration(
    scanLineProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.scan_phone_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.scan_phone),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 96.dp, height = 150.dp),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.scan_phone_border),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(id = R.drawable.scan_phone_line),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (56 + 90 * scanLineProgress).dp)
                .size(width = 192.dp, height = 18.dp),
            contentScale = ContentScale.FillBounds
        )
    }
}