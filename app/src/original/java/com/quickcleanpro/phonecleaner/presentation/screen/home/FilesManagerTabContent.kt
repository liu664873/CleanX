package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen

private data class FileManagerItem(
    val iconRes: Int,
    @param:StringRes val labelRes: Int,
    val screen: Screen,
)

private val OnboardingNavy = Color(0xFF1D2959)

@Composable
fun FilesManagerTabContent(onFeatureClick: () -> Unit = {}) {
    val router = LocalRouter.current
    val items =
        listOf(
            FileManagerItem(R.drawable.ic_photos, R.string.nav_photos, Screen.PhotosManager),
            FileManagerItem(R.drawable.ic_similar_photos, R.string.nav_similar_photos, Screen.SimilarPhotosManager),
            FileManagerItem(R.drawable.ic_photo_privacy, R.string.nav_photo_privacy, Screen.PhotoPrivacyManager),
            FileManagerItem(R.drawable.ic_screenshots, R.string.nav_screenshots, Screen.ScreenshotsManager),
            FileManagerItem(R.drawable.ic_videos, R.string.nav_videos, Screen.VideosManager),
            FileManagerItem(R.drawable.ic_audios, R.string.nav_audios, Screen.AudiosManager),
            FileManagerItem(R.drawable.ic_large_files, R.string.nav_large_files, Screen.LargeFilesManager),
            FileManagerItem(R.drawable.ic_file_yellow, R.string.nav_duplicate_files, Screen.DuplicateFilesManager),
            FileManagerItem(R.drawable.ic_documents, R.string.nav_documents, Screen.DocumentsManager),
        )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items) { item ->
            FileManagerCard(
                item = item,
                onClick = {
                    onFeatureClick()
                    router.navigate(item.screen)
                },
            )
        }
    }
}

@Composable
private fun FileManagerCard(
    item: FileManagerItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(item.labelRes),
                color = OnboardingNavy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
