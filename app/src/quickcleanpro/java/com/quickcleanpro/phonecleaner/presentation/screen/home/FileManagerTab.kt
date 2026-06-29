package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXGridSpacing
import com.quickcleanpro.phonecleaner.presentation.common.CleanXIconTile
import com.quickcleanpro.phonecleaner.presentation.common.FileDestination
import com.quickcleanpro.phonecleaner.presentation.common.route.AppNavigationEvent

private data class FileCategory(
    @param:StringRes val titleRes: Int,
    val iconRes: Int,
    val route: String
)

@Composable
internal fun FileManagerTabContent(
    onFeatureClick: () -> Unit = {},
    onNavigate: (AppNavigationEvent) -> Unit = {}
) {
    val categories = remember {
        listOf(
            FileCategory(R.string.nav_photos, R.drawable.ic_photos, FileDestination.Photos.route),
            FileCategory(R.string.nav_similar_photos, R.drawable.ic_similar_photos, FileDestination.SimilarPhotos.route),
            FileCategory(R.string.nav_photo_privacy, R.drawable.ic_photo_privacy, FileDestination.PhotoPrivacy.route),
            FileCategory(R.string.nav_screenshots, R.drawable.ic_screenshots, FileDestination.Screenshots.route),
            FileCategory(R.string.nav_videos, R.drawable.ic_videos, FileDestination.Videos.route),
            FileCategory(R.string.nav_audios, R.drawable.ic_audios, FileDestination.Audios.route),
            FileCategory(R.string.nav_large_files, R.drawable.ic_large_files, FileDestination.LargeFiles.route),
            FileCategory(R.string.nav_duplicate_files, R.drawable.ic_duplicate_files, FileDestination.DuplicateFiles.route),
            FileCategory(R.string.nav_documents, R.drawable.ic_documents, FileDestination.Documents.route)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(CleanXGridSpacing),
        verticalArrangement = Arrangement.spacedBy(CleanXGridSpacing)
    ) {
        items(categories) { category ->
            val title = stringResource(category.titleRes)
            CleanXIconTile(
                title = title,
                icon = painterResource(id = category.iconRes),
                onClick = {
                    onFeatureClick()
                    onNavigate(AppNavigationEvent.AdDestination(category.route))
                }
            )
        }
    }
}
