package com.quickcleanpro.phonecleaner.presentation.screen.applock

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.InlinePermissionOverlay
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText

@Composable
internal fun AppLockUsageAccessPermissionDialog(
    onManagePermission: () -> Unit,
    onDismissToHome: () -> Unit
) {
    BackHandler(onBack = onDismissToHome)
    InlinePermissionOverlay(onDismiss = onDismissToHome) {
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
            .widthIn(max = 360.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_lock_usage_permission_title),
                color = CleanXText,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.app_lock_usage_permission_desc),
                color = CleanXMutedText,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            AppPermissionCard(
                grantText = stringResource(R.string.app_lock_usage_permission_grant)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CleanXPrimaryButton(
                text = stringResource(R.string.allow_now),
                onClick = onManagePermission
            )
        }
    }
}

@Composable
internal fun AppLockOverlayPermissionDialog(
    onAllowNow: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                    letterSpacing = 0.03.em,
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
        color = Color(0xFFF0F5FB),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color(0xFF1B6DFF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp)
                    .height(1.dp)
                    .background(Color(0xFFD4DDE9))
            )
            Text(
                text = grantText,
                color = CleanXText,
                fontSize = 16.sp
            )
        }
    }
}

private val AppLockDialogNavy = Color(0xFF1D2959)
