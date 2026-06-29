package com.quickcleanpro.phonecleaner.presentation.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quickcleanpro.phonecleaner.R

@Composable
fun CleanXRateDialog(
    onDismiss: () -> Unit,
    onOpenStore: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var rating by remember { mutableIntStateOf(0) }
    var showFeedback by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                                if (onOpenStore != null) {
                                    onOpenStore()
                                } else {
                                    openGooglePlayRatePage(context)
                                }
                            } else {
                                showFeedback = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RateSelectionContent(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    Text(
        text = stringResource(R.string.rate_title),
        color = CleanXText,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(18.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFC52E) else Color(0xFFD3D6DC),
                modifier = Modifier
                    .size(38.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onRatingChange(index + 1) }
                    )
            )
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    CleanXPrimaryButton(
        text = stringResource(R.string.submit),
        onClick = onSubmit,
        enabled = rating > 0
    )
}

@Composable
private fun RateFeedbackContent(onDismiss: () -> Unit) {
    Text(
        text = stringResource(R.string.rate_feedback_title),
        color = CleanXText,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.rate_feedback_message),
        color = CleanXMutedText,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    CleanXPrimaryButton(
        text = stringResource(R.string.ok),
        onClick = onDismiss
    )
}

private fun openGooglePlayRatePage(context: Context) {
    val packageName = context.packageName
    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName")
    ).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val webIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    runCatching {
        context.startActivity(marketIntent)
    }.recoverCatching {
        context.startActivity(webIntent)
    }
}
