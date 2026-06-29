package com.quickcleanpro.phonecleaner.presentation.screen.files

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.quickcleanpro.phonecleaner.presentation.theme.QuickCleanTheme

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewPhotosManagerScreen() {
    QuickCleanTheme {
        PhotosManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewScreenshotsManagerScreen() {
    QuickCleanTheme {
        ScreenshotsManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewVideosManagerScreen() {
    QuickCleanTheme {
        VideosManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewAudiosManagerScreen() {
    QuickCleanTheme {
        AudiosManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewSimilarPhotosManagerScreen() {
    QuickCleanTheme {
        SimilarPhotosManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewPhotoPrivacyManagerScreen() {
    QuickCleanTheme {
        PhotoPrivacyManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewLargeFilesManagerScreen() {
    QuickCleanTheme {
        LargeFilesManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewDocumentsManagerScreen() {
    QuickCleanTheme {
        DocumentsManagerScreen(onBack = {}, onNavigateTool = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F8FF)
@Composable
private fun PreviewDuplicateFilesManagerScreen() {
    QuickCleanTheme {
        DuplicateFilesManagerScreen(onBack = {}, onNavigateTool = {})
    }
}
