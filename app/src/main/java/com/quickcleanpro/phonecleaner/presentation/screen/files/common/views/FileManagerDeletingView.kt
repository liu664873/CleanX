package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation

@Composable
internal fun FileManagerDeletingView(fallbackText: String? = null) {
    CleanSpiralAnimation {
        Image(
            painter = painterResource(R.drawable.tran_scan),
            contentDescription = fallbackText ?: stringResource(R.string.delete_loading_fallback),
            modifier = Modifier.size(100.dp),
        )
    }
}
