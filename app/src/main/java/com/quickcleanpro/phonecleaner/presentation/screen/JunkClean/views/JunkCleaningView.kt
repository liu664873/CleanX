package com.quickcleanpro.phonecleaner.presentation.screen.JunkClean.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation

@Composable
internal fun JunkCleaningView() {
    CleanSpiralAnimation{
        Image(
            painter = painterResource(R.drawable.ic_tran_can_blue),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
        )
    }
}

