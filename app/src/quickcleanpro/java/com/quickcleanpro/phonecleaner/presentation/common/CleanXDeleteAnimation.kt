package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.quickcleanpro.phonecleaner.R
@Composable
fun CleanXDeleteAnimation(
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    isPlaying: Boolean = true,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackText: String? = null
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset("delete_animation/delete_finish.json"),
        imageAssetsFolder = "delete_animation/images/"
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = isPlaying,
        speed = 1f
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (composition == null) {
            Text(
                fallbackText ?: stringResource(R.string.delete_loading_fallback),
                color = CleanXMutedText,
                fontSize = 16.sp
            )
        } else {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
    }
}

@Composable
fun CleanXFullScreenDeleteAnimation(
    modifier: Modifier = Modifier,
    fallbackText: String? = null,
    backgroundColor: androidx.compose.ui.graphics.Color = CleanXBackground
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CleanXDeleteAnimation(
            modifier = Modifier.fillMaxSize(),
            fallbackText = fallbackText
        )
    }
}
