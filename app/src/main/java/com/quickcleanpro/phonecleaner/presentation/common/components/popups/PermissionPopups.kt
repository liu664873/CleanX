package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionCopy

private val PermissionDialogNavy = Color(0xFF1D2959)
private val PermissionDialogBlue = Color(0xFF4179FC)
private val PermissionDialogSecondaryButtonBg = Color(0xFFE0EBF7)
private val PermissionDialogSecondaryText = Color(0xA61D2959)
private val PermissionDialogShape = RoundedCornerShape(12.dp)
private val PermissionDialogButtonShape = RoundedCornerShape(8.dp)

@Composable
fun InlinePermissionOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
        ) {
            content()
        }
    }
}

@Composable
fun CleanXPermissionRequiredDialog(
    copy: CleanXPermissionCopy,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .widthIn(max = 343.dp)
            .clip(PermissionDialogShape)
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFFF7F8FD),
                    0.2638f to Color.White,
                    1f to Color.White,
                ),
            )
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✨ " + stringResource(copy.titleRes),
            color = PermissionDialogNavy,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.03.em,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(copy.descriptionRes),
            color = PermissionDialogNavy,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.03.em,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionBullet(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.ic_x),
                    contentDescription = "image description",
                    contentScale = ContentScale.None,
                    modifier = Modifier.size(24.dp),
                )
            },
            text = stringResource(copy.hint1Res)
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionBullet(
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "image description",
                    contentScale = ContentScale.None,
                    modifier = Modifier.size(24.dp),
                )
            },
            text = stringResource(copy.hint2Res)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PermissionDialogSecondaryButton(
                text = stringResource(copy.cancelRes),
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            PermissionDialogOutlinedButton(
                text = stringResource(copy.allowRes),
                onClick = onSubmit,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PermissionBullet(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = text,
            color = PermissionDialogNavy,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PermissionDialogSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(37.dp)
            .clip(PermissionDialogButtonShape)
            .background(PermissionDialogSecondaryButtonBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = PermissionDialogSecondaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionDialogOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(37.dp)
            .clip(PermissionDialogButtonShape)
            .border(1.56.dp, PermissionDialogBlue, PermissionDialogButtonShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = PermissionDialogBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
