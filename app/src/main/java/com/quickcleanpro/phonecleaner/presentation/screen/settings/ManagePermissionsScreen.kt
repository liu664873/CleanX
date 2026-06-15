package com.quickcleanpro.phonecleaner.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.quickcleanpro.phonecleaner.presentation.navigation.LocalNavController

private val PageBgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 20.dp
private val ToggleTrackOn = Color(0xFF4179FC)
private val ToggleTrackOff = Color(0xFFECF0F4)
private val ToggleThumb = Color(0xFFAFBBD0)

@Composable
fun ManagePermissionsScreen(onBack: () -> Unit = {}) {

    var storagePermission by remember { mutableStateOf(true) }
    var usageDataPermission by remember { mutableStateOf(true) }
    var locationManagement by remember { mutableStateOf(true) }
    var notificationToolbar by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = stringResource(R.string.settings_manage_permissions),
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
                        PermissionToggleRow(
                            label = stringResource(R.string.settings_storage_permission),
                            checked = storagePermission,
                            onCheckedChange = { storagePermission = it },
                        )

                        SettingsDivider()

                        PermissionToggleRow(
                            label = stringResource(R.string.settings_usage_data_permission),
                            checked = usageDataPermission,
                            onCheckedChange = { usageDataPermission = it },
                        )

                        SettingsDivider()

                        PermissionToggleRow(
                            label = "Location Management",
                            checked = locationManagement,
                            onCheckedChange = { locationManagement = it },
                        )

                        SettingsDivider()

                        PermissionToggleRow(
                            label = "Notification Toolbar",
                            checked = notificationToolbar,
                            onCheckedChange = { notificationToolbar = it },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun PermissionToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ToggleTrackOn,
                uncheckedThumbColor = ToggleThumb,
                uncheckedTrackColor = ToggleTrackOff,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
            ),
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
