package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXPrimaryButton

private val SettingsCardBg = Color(0xFFF6F7FB)
private val SettingsNavy = Color(0xFF1D2959)
private val SettingsMuted = Color(0xA61D2959)

@Composable
internal fun TemperatureUnitDialog(
    selected: String,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SettingsCardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.settings_temperature_unit),
                color = SettingsNavy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TemperatureOption(unit = "F", selected = selected == "F", onClick = { onSelected("F") })
                TemperatureOption(unit = "C", selected = selected == "C", onClick = { onSelected("C") })
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun TemperatureOption(
    unit: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${'\u00B0'}$unit",
            color = SettingsNavy,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4179FC),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
internal fun SettingsRateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(4) }
    var showFeedback by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect {
            dialogWindowProvider?.window?.run {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            RateDialogFrame(
                modifier = Modifier
                    .offset(y = (-48).dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                if (showFeedback) {
                    RateFeedbackContent(onDismiss = onDismiss)
                } else {
                    RateSelectionContent(
                        rating = rating,
                        onRatingChange = { rating = it },
                        onSubmit = {
                            if (rating >= 4) {
                                onDismiss()
                                openGooglePlayRatePage(context)
                            } else {
                                showFeedback = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RateDialogFrame(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 343.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 31.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            0f to Color(0xFFF7FAFD),
                            0.2638f to Color.White,
                            1f to Color.White,
                        ),
                    )
                    .padding(start = 16.dp, top = 104.dp, end = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
            RateHeroImage(
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun RateHeroImage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(251.dp)
            .clipToBounds(),
    ) {
        Image(
            painter = painterResource(R.drawable.rate_heart_hand),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(215.dp)
                .offset(x = (20).dp, y = (-47).dp),
        )
    }
}

@Composable
private fun RateSelectionContent(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onSubmit: () -> Unit,
) {
    Text(
        text = stringResource(R.string.rate_title),
        color = SettingsNavy,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(5) { index ->
            Image(
                painter = painterResource(
                    if (index < rating) R.drawable.rate_star_selected else R.drawable.rate_star_unselected,
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onRatingChange(index + 1) },
                    ),
            )
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    CleanXPrimaryButton(
        text = stringResource(R.string.submit),
        onClick = onSubmit,
        enabled = rating > 0,
        height = 46.dp,
        cornerRadius = 10.dp,
        fontSize = 20.sp,
    )
}

@Composable
private fun RateFeedbackContent(onDismiss: () -> Unit) {
    Text(
        text = stringResource(R.string.rate_feedback_title),
        color = SettingsNavy,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.rate_feedback_message),
        color = SettingsMuted,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(20.dp))
    CleanXPrimaryButton(
        text = stringResource(R.string.ok),
        onClick = onDismiss,
    )
}

private fun openGooglePlayRatePage(context: Context) {
    val packageName = context.packageName
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(marketIntent)
    }.recoverCatching {
        context.startActivity(webIntent)
    }
}
