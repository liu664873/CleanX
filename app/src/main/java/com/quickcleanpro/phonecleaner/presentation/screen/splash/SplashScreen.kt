package com.quickcleanpro.phonecleaner.presentation.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.AppConfig
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.common.utils.openUrl
import com.quickcleanpro.phonecleaner.presentation.theme.CleanXTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    paused: Boolean = false,
    onSplashFinished: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scaleAnim = remember { Animatable(0.3f) }
    val alphaAnim = remember { Animatable(0f) }
    val progressAnim = remember { Animatable(0f) }
    var progress by remember { mutableFloatStateOf(0f) }
    var pausedForLink by remember { mutableStateOf(false) }
    val splashPaused = paused || pausedForLink
    val interactionSource = remember { MutableInteractionSource() }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    pausedForLink = false
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(splashPaused) {
        if (splashPaused) return@LaunchedEffect
        if (scaleAnim.value < 1f) {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            )
        }
        if (alphaAnim.value < 1f) {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600),
            )
        }
        if (progressAnim.value < 1f) {
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
            ) {
                progress = value
            }
        }
        delay(300)
        onSplashFinished()
    }

    val logoShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.15f),
                                    Color.Black.copy(alpha = 0.35f),
                                ),
                        ),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 160.dp)
                    .alpha(alphaAnim.value)
                    .scale(scaleAnim.value),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(100.dp)
                        .border(1.35.dp, Color.White, logoShape)
                        .clip(logoShape),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 30.sp,
            )
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            RoundedProgressBar(progress = progress)

            FlowRow(
                modifier =
                    Modifier
                        .width(330.dp)
                        .wrapContentHeight()
                        .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.splash_accept_prefix),
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFFD9D9D9),
                )
                Text(
                    modifier =
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            pausedForLink = true
                            if (!context.openUrl(AppConfig.TERMS_OF_SERVICE_URL)) {
                                pausedForLink = false
                            }
                        },
                    text = stringResource(R.string.settings_terms_of_service),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                )
                Text(
                    text = " | ",
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFFD9D9D9),
                )
                Text(
                    modifier =
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            pausedForLink = true
                            if (!context.openUrl(AppConfig.PRIVACY_POLICY_URL)) {
                                pausedForLink = false
                            }
                        },
                    text = stringResource(R.string.settings_privacy_policy),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewSplashScreen() {
    CleanXTheme {
        SplashScreen(
            onSplashFinished = {},
        )
    }
}
