package com.quickcleanpro.phonecleaner.presentation.common.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R

private val SettingsRowBg = Color(0xFFF6F7FB)
private val SettingsRowText = Color(0xFF1D2959)
private val ToggleTrackOn = Color(0xFFBADDFF)
private val ToggleTrackOff = Color(0xFFECF0F4)
private val ToggleThumbOn = Color(0xFF4179FC)
private val ToggleThumbOff = Color(0xFFAFBBD0)
private val SettingsRowShape = RoundedCornerShape(20.dp)

@Composable
fun CleanXSettingsToggleRow(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CleanXSettingsRowFrame(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = label,
            color = SettingsRowText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f),
        )
        CleanXMiniSwitch(checked = checked)
    }
}

@Composable
fun CleanXSettingsNavigationRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes trailingIconRes: Int = R.mipmap.ic_next,
) {
    CleanXSettingsRowFrame(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(
            text = label,
            color = SettingsRowText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f),
        )
        Image(
            painter = painterResource(trailingIconRes),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
fun CleanXMiniSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 22.dp, height = 9.dp)
                .clip(RoundedCornerShape(50))
                .background(if (checked) ToggleTrackOn else ToggleTrackOff),
        )
        Box(
            modifier = Modifier
                .align(if (checked) Alignment.CenterStart else Alignment.CenterEnd)
                .size(10.5.dp)
                .clip(CircleShape)
                .background(if (checked) ToggleThumbOn else ToggleThumbOff),
        )
    }
}

@Composable
private fun CleanXSettingsRowFrame(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        color = SettingsRowBg,
        shape = SettingsRowShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
