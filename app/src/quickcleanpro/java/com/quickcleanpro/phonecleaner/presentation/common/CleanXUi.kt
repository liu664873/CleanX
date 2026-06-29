package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewCleanXUi() {
    Column(verticalArrangement = Arrangement.spacedBy(CleanXItemSpacing)) {
        CleanXHeader(
            title = "Clean",
            onBack = {}
        )
        CleanXPrimaryButton(
            text = "CleanTwo",
            onClick = {}
        )
        CleanXSegmentTabs(
            tabs = listOf("Home", "Files", "Toolbox"),
            selectedIndex = 0,
            onSelected = {}
        )
    }
}
