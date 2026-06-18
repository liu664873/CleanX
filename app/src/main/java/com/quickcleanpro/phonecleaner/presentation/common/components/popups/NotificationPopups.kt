package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue

private val NotificationNavy = Color(0xFF1D2959)
private val NotificationNavyMuted = Color(0xA61D2959)

@Composable
internal fun NotificationBlockingTurnedOffDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        title = {
            Text(
                text = stringResource(R.string.notification_blocking_off_message),
                color = NotificationNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.notification_blocking_off_detail),
                color = NotificationNavyMuted,
                fontSize = 15.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.ok), color = CleanXBlue)
            }
        },
    )
}
