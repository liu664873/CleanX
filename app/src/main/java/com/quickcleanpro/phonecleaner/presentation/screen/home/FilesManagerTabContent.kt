package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

private data class FileManagerItem(
    val iconRes: Int,
    @androidx.annotation.StringRes val labelRes: Int,
)

private val OnboardingNavy = Color(0xFF1D2959)

@Composable
fun FilesManagerTabContent() {
    val items = listOf(
        FileManagerItem(R.drawable.ic_photos, R.string.nav_photos),
        FileManagerItem(R.drawable.ic_similar_photos, R.string.nav_similar_photos),
        FileManagerItem(R.drawable.ic_photo_privacy, R.string.nav_photo_privacy),
        FileManagerItem(R.drawable.ic_screenshots, R.string.nav_screenshots),
        FileManagerItem(R.drawable.ic_videos, R.string.nav_videos),
        FileManagerItem(R.drawable.ic_audios, R.string.nav_audios),
        FileManagerItem(R.drawable.ic_large_files, R.string.nav_large_files),
        FileManagerItem(R.drawable.ic_duplicate_files, R.string.nav_duplicate_files),
        FileManagerItem(R.drawable.ic_documents, R.string.nav_documents),
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            FileManagerCard(item)
        }
    }
}

@Composable
private fun FileManagerCard(item: FileManagerItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
    ) {
        Column(
            modifier = Modifier
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

//
//@Composable
//fun FilesManagerTabContent() {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .padding(horizontal = 16.dp)
//            .padding(top = 32.dp),
//    ) {
//        // Row 1
//        FileManagerGridRow(
//            items = listOf(
//                FileManagerItem(R.drawable.ic_photos, R.string.nav_photos),
//                FileManagerItem(R.drawable.ic_similar_photos, R.string.nav_similar_photos),
//                FileManagerItem(R.drawable.ic_photo_privacy, R.string.nav_photo_privacy),
//            ),
//        )
//        Spacer(modifier = Modifier.height(6.dp))
//
//        // Row 2
//        FileManagerGridRow(
//            items = listOf(
//                FileManagerItem(R.drawable.ic_screenshots, R.string.nav_screenshots),
//                FileManagerItem(R.drawable.ic_videos, R.string.nav_videos),
//                FileManagerItem(R.drawable.ic_audios, R.string.nav_audios),
//            ),
//        )
//        Spacer(modifier = Modifier.height(6.dp))
//
//        // Row 3
//        FileManagerGridRow(
//            items = listOf(
//                FileManagerItem(R.drawable.ic_large_files, R.string.nav_large_files),
//                FileManagerItem(R.drawable.ic_duplicate_files, R.string.nav_duplicate_files),
//                FileManagerItem(R.drawable.ic_documents, R.string.nav_documents),
//            ),
//        )
//
//        Spacer(modifier = Modifier.height(100.dp))
//    }
//}
//
//private data class FileManagerItem(
//    val iconRes: Int,
//    @androidx.annotation.StringRes val labelRes: Int,
//)
//
//@Composable
//private fun FileManagerGridRow(items: List<FileManagerItem>) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//    ) {
//        items.forEach { item ->
//            FileManagerCard(item = item)
//        }
//    }
//}
//
//@Composable
//private fun FileManagerCard(item: FileManagerItem) {
//    Surface(
//        modifier = Modifier.size(110.dp),
//        color = Color.White,
//        shape = RoundedCornerShape(10.dp),
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = 16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ) {
//            Image(
//                painter = painterResource(id = item.iconRes),
//                contentDescription = null,
//                modifier = Modifier.size(52.dp),
//                contentScale = ContentScale.Fit,
//            )
//            Spacer(modifier = Modifier.height(6.dp))
//            Text(
//                text = stringResource(item.labelRes),
//                color = OnboardingNavy,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                lineHeight = 20.sp,
//                textAlign = TextAlign.Center,
//                maxLines = 2,
//            )
//        }
//    }
//}
