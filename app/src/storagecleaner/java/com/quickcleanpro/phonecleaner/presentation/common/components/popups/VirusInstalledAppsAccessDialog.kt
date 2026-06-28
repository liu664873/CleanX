package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

private val VirusInstalledAppsDialogShape = RoundedCornerShape(12.dp)
private val VirusInstalledAppsButtonShape = RoundedCornerShape(28.dp)
private val VirusInstalledAppsPrimary = Color(0xFF4179FC)
private val VirusInstalledAppsTitle = Color(0xFF2D3748)
private val VirusInstalledAppsBody = Color(0xFF8190A5)

@Composable
internal fun VirusInstalledAppsAccessDialog(
    title: String,
    message: String,
    primaryText: String,
    onPrimaryAction: () -> Unit,
    secondaryText: String,
    onSecondaryAction: () -> Unit,
    onDismissRequest: () -> Unit = onSecondaryAction,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .widthIn(max = 343.dp)
                        .clip(VirusInstalledAppsDialogShape)
                        .background(
                            Brush.verticalGradient(
                                0f to Color(0xFFF7FAFD),
                                0.4f to Color.White,
                                1f to Color.White,
                            ),
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = VirusInstalledAppsTitle,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    letterSpacing = 0.03.em,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = VirusInstalledAppsBody,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.03.em,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                VirusInstalledAppsFilledButton(
                    text = primaryText,
                    onClick = onPrimaryAction,
                )
                Spacer(modifier = Modifier.height(16.dp))
                VirusInstalledAppsOutlinedButton(
                    text = secondaryText,
                    onClick = onSecondaryAction,
                )
            }
        }
    }
}

@Composable
private fun VirusInstalledAppsFilledButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(VirusInstalledAppsButtonShape)
                .background(VirusInstalledAppsPrimary)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        VirusInstalledAppsButtonText(
            text = text,
            color = Color.White,
        )
    }
}

@Composable
private fun VirusInstalledAppsOutlinedButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(VirusInstalledAppsButtonShape)
                .border(1.35.dp, VirusInstalledAppsPrimary, VirusInstalledAppsButtonShape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        VirusInstalledAppsButtonText(
            text = text,
            color = VirusInstalledAppsPrimary,
        )
    }
}

@Composable
private fun VirusInstalledAppsButtonText(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        color = color,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}
