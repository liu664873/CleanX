package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXPrimaryButton

private val DialogNavy = Color(0xFF1D2959)
private val DialogBlue = Color(0xFF4179FC)
private val DialogSecondaryButtonBg = Color(0xFFE0EBF7)
private val DialogSecondaryText = Color(0xA61D2959)
private val DialogShape = RoundedCornerShape(12.dp)
private val DialogButtonShape = RoundedCornerShape(8.dp)

@Composable
internal fun CleanXDecisionDialog(
    title: String,
    onDismissRequest: () -> Unit,
    dismissText: String,
    onDismissAction: () -> Unit,
    confirmText: String,
    onConfirmAction: () -> Unit,
    message: String? = null,
) {
    CleanXPopupDialogFrame(onDismissRequest = onDismissRequest) {
        Text(
            text = title,
            color = DialogNavy,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.03.em,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        message?.takeIf { it.isNotBlank() }?.let { body ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = body,
                color = DialogNavy,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.03.em,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CleanXDialogSecondaryButton(
                text = dismissText,
                onClick = onDismissAction,
                modifier = Modifier.weight(1f),
            )
            CleanXDialogOutlinedButton(
                text = confirmText,
                onClick = onConfirmAction,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun CleanXSingleActionDialog(
    title: String,
    actionText: String,
    onAction: () -> Unit,
    onDismissRequest: () -> Unit = onAction,
    message: String? = null,
) {
    CleanXPopupDialogFrame(onDismissRequest = onDismissRequest) {
        Text(
            text = title,
            color = DialogNavy,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.03.em,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        message?.takeIf { it.isNotBlank() }?.let { body ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = body,
                color = DialogNavy,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.03.em,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        CleanXPrimaryButton(
            text = actionText,
            onClick = onAction,
            height = 46.dp,
            cornerRadius = 10.dp,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun CleanXPopupDialogFrame(
    onDismissRequest: () -> Unit,
    verticalOffset: Dp = (-28).dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties =
            DialogProperties(
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
                        .offset(y = verticalOffset)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .widthIn(max = 343.dp)
                        .clip(DialogShape)
                        .background(
                            Brush.verticalGradient(
                                0f to Color(0xFFF7F8FD),
                                0.2638f to Color.White,
                                1f to Color.White,
                            ),
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        )
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

@Composable
private fun CleanXDialogSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(37.dp)
                .clip(DialogButtonShape)
                .background(DialogSecondaryButtonBg)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = DialogSecondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CleanXDialogOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(37.dp)
                .clip(DialogButtonShape)
                .border(1.56.dp, DialogBlue, DialogButtonShape)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = DialogBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}
