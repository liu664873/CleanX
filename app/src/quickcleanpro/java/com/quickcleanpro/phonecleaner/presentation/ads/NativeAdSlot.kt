package com.quickcleanpro.phonecleaner.presentation.ads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NativeAdSlot(
    placement: AdPlacement,
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 0.dp,
    loadedHeight: Dp = 120.dp,
    adController: AdController = LocalAdController.current
) {
    val state = adController.nativeAdState(placement)
    val slotHeight = when (state) {
        NativeAdState.Unavailable -> collapsedHeight
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = slotHeight)
            .height(slotHeight)
    )
}
