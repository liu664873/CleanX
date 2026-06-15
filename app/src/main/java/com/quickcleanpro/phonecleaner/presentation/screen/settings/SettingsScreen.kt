package com.quickcleanpro.phonecleaner.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTopAppBar
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen

private val PageBgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 20.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNavigate: (AppNavigationEvent) -> Unit = {},
) {

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = stringResource(R.string.nav_settings),
                onBack = onBack,
                modifier = Modifier.systemBarsPadding(),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBgGradient),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CardBg,
                    shape = RoundedCornerShape(CardRadius),
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        // Manage Permissions
                        SettingsArrowRow(
                            label = stringResource(R.string.settings_manage_permissions),
                            onClick = {
                                onNavigate(AppNavigationEvent.Destination(Screen.ManagePermissions.route))
                            },
                        )

                        SettingsDivider()

                        // Temperature Unit
                        SettingsLabelRow(
                            label = stringResource(R.string.settings_temperature_unit),
                            value = "\u00B0F",
                        )

                        SettingsDivider()

                        // Terms of Service
                        SettingsArrowRow(
                            label = stringResource(R.string.settings_terms_of_service),
                            onClick = { },
                        )

                        SettingsDivider()

                        // Privacy Policy
                        SettingsArrowRow(
                            label = stringResource(R.string.settings_privacy_policy),
                            onClick = { },
                        )

                        SettingsDivider()

                        // Rate Us
                        SettingsArrowRow(
                            label = stringResource(R.string.settings_rate_us),
                            onClick = { },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun SettingsArrowRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 22.sp,
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_ok),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = NavyMuted,
        )
    }
}

@Composable
private fun SettingsLabelRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp,
        )
        Text(
            text = value,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = Divider15,
        thickness = 1.dp,
    )
}
