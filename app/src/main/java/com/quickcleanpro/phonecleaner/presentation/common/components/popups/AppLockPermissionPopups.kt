package com.quickcleanpro.phonecleaner.presentation.common.components.popups

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXPrimaryButton

@Composable
internal fun AppLockUsageAccessPermissionDialog(
    onManagePermission: () -> Unit,
    onDismissToHome: () -> Unit
) {
    FigmaPermissionOverlay(onDismiss = onDismissToHome) {
        AppLockUsageAccessPermissionCard(onManagePermission = onManagePermission)
    }
}

@Composable
private fun AppLockUsageAccessPermissionCard(
    onManagePermission: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .widthIn(max = 343.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF7F8FD), Color.White),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_lock_usage_permission_title),
                color = AppLockDialogNavy,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.app_lock_usage_permission_desc),
                color = AppLockDialogNavy,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppPermissionCard(
                grantText = stringResource(R.string.app_lock_usage_permission_grant)
            )
            Spacer(modifier = Modifier.height(20.dp))
            CleanXPrimaryButton(
                text = stringResource(R.string.submit),
                onClick = onManagePermission,
                height = 46.dp,
                cornerRadius = 10.dp,
                fontSize = 20.sp,
            )
        }
    }
}

@Composable
internal fun AppLockOverlayPermissionDialog(
    onAllowNow: () -> Unit,
    onCancel: () -> Unit
) {
    FigmaPermissionOverlay(onDismiss = onCancel) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .widthIn(max = 343.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF7F8FD), Color.White)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PermissionHeroImage()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_lock_overlay_permission_message),
                    color = AppLockDialogNavy,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                CleanXPrimaryButton(
                    text = stringResource(R.string.manage_permission),
                    onClick = onAllowNow,
                    height = 46.dp,
                    cornerRadius = 10.dp,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                PermissionCancelButton(onClick = onCancel)
            }
        }
    }
}

@Composable
private fun PermissionHeroImage() {
    Image(
        painter = painterResource(R.drawable.app_lock_permission),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .size(width = 237.dp, height = 132.dp)
    )
}

@Composable
private fun PermissionCancelButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.56.dp, CleanXBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = CleanXBlue
        )
    ) {
        Text(
            text = stringResource(R.string.cancel),
            fontSize = 19.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun AppPermissionCard(grantText: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8FAFF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    color = CleanXBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = grantText,
                color = AppLockDialogNavy.copy(alpha = 0.65f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun FigmaPermissionOverlay(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .padding(bottom = 56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        ) {
            content()
        }
    }
}

private val AppLockDialogNavy = Color(0xFF1D2959)
