package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R

private val VirusTitle = Color(0xFF1D2959)
private val VirusSecondary = Color(0xA61D2959)
private val VirusBlue = Color(0xFF4179FC)
private val VirusPanelShape = RoundedCornerShape(12.dp)
private val VirusButtonShape = RoundedCornerShape(10.dp)

@Composable
internal fun DeleteVirusFileDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = VirusPanelShape,
        containerColor = Color.White,
        title = {
            Text(
                text = stringResource(R.string.delete),
                color = VirusTitle,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = stringResource(R.string.delete_this_file),
                color = VirusSecondary,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = VirusButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VirusBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = VirusSecondary,
                        fontSize = 17.sp
                    )
                }
            }
        }
    )
}
