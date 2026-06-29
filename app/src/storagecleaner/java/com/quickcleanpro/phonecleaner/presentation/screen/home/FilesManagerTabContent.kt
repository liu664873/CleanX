package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.config.VariantFeature
import com.quickcleanpro.phonecleaner.config.iconRes
import com.quickcleanpro.phonecleaner.config.screenOrNull
import com.quickcleanpro.phonecleaner.config.titleRes
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.presentation.theme.LocalVariantTheme
import androidx.compose.runtime.ReadOnlyComposable

private data class FileManagerItem(
    val iconRes: Int,
    @param:StringRes val labelRes: Int,
    val screen: Screen,
)

private val OnboardingNavy: Color @Composable @ReadOnlyComposable get() = LocalVariantTheme.current.colors.navy
private val LabelWordSeparator = Regex("\\s+")
private val StorageCleanerFileFeatures =
    listOf(
        VariantFeature.PHOTOS,
        VariantFeature.SIMILAR_PHOTOS,
        VariantFeature.PHOTO_PRIVACY,
        VariantFeature.SCREENSHOTS,
        VariantFeature.VIDEOS,
        VariantFeature.AUDIOS,
        VariantFeature.LARGE_FILES,
        VariantFeature.DUPLICATE_FILES,
        VariantFeature.DOCUMENTS,
    )

@Composable
fun FilesManagerTabContent(onFeatureClick: () -> Unit = {}) {
    val router = LocalRouter.current
    val items =
        StorageCleanerFileFeatures
            .mapNotNull(VariantFeature::toFileManagerItem)

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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

private fun VariantFeature.toFileManagerItem(): FileManagerItem? {
    val screen = screenOrNull() ?: return null
    return FileManagerItem(
        iconRes = iconRes(),
        labelRes = titleRes(),
        screen = screen,
    )
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
                .height(118.dp)
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
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                FileManagerCardLabel(label = stringResource(item.labelRes))
            }
        }
    }
}

@Composable
private fun FileManagerCardLabel(label: String) {
    val lines =
        remember(label) {
            LabelWordSeparator
                .split(label.trim(), limit = 2)
                .filter { it.isNotEmpty() }
        }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        lines.forEach { line ->
            Text(
                text = line,
                color = OnboardingNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
