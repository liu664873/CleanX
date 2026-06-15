package com.quickcleanpro.phonecleaner.presentation.screen.deviceinfo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXTopAppBar
import com.quickcleanpro.phonecleaner.presentation.common.components.ToolFeatureBanners
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen

private val PageBgGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5)),
)
private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Blue = Color(0xFF4179FC)
private val Divider15 = Color(0x261D2959)
private val CardRadius = 12.dp

private enum class SpeedTestState { IDLE, TESTING, RESULT }

@Composable
fun NetworkSpeedScreen(
    onBack: () -> Unit = {},
    onNavigate: (AppNavigationEvent) -> Unit = {},
) {
    val state = remember { mutableStateOf(SpeedTestState.IDLE) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CleanXTopAppBar(
                title = "Network Speed",
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
                Spacer(modifier = Modifier.height(16.dp))

                // Network Info Card
                NetworkInfoCard(state = state.value)

                Spacer(modifier = Modifier.height(16.dp))

                // Speed Test Card
                when (state.value) {
                    SpeedTestState.IDLE -> IdleSpeedCard()
                    SpeedTestState.TESTING -> TestingSpeedCard()
                    SpeedTestState.RESULT -> ResultSpeedCard()
                }

                if (state.value == SpeedTestState.RESULT) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ToolFeatureBanners(
                        onNavigate = onNavigate,
                        excludeRoutes = setOf(Screen.NetworkSpeed.route),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SpeedHistoryCard()
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (state.value) {
                    SpeedTestState.IDLE -> {
                        CleanXPrimaryButton(
                            text = stringResource(R.string.run_speed_test),
                            onClick = { state.value = SpeedTestState.TESTING },
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    SpeedTestState.TESTING -> {
                        Text(
                            text = "Testing...",
                            color = NavyMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                    SpeedTestState.RESULT -> {
                        CleanXPrimaryButton(
                            text = stringResource(R.string.run_speed_test),
                            onClick = { state.value = SpeedTestState.TESTING },
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun NetworkInfoCard(state: SpeedTestState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp),
        ) {
            when (state) {
                SpeedTestState.IDLE -> {
                    InfoLine(
                        label = stringResource(R.string.network_type),
                        value = "--",
                    )
                    InfoDivider()
                    WiFiNameLine(value = "--")
                    InfoDivider()
                    InfoLine(label = "IP", value = "--")
                }
                SpeedTestState.TESTING, SpeedTestState.RESULT -> {
                    InfoLine(
                        label = stringResource(R.string.network_type),
                        value = "Wi-Fi",
                    )
                    InfoDivider()
                    WiFiNameLine(value = "<unknown sid>")
                    InfoDivider()
                    InfoLine(label = "IP", value = "192.168.111.158")
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
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
private fun WiFiNameLine(value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Wi-Fi name",
            color = Navy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
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
private fun InfoDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 16.dp),
        color = Divider15,
        thickness = 1.dp,
    )
}

// --- Idle Speed Card ---
@Composable
private fun IdleSpeedCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Placeholder speed illustration
                Image(
                    painter = painterResource(id = R.drawable.robot),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    SpeedColumn(label = "Download Mbps", value = "-\nMbps", showDivider = false)
                    VerticalSpeedDivider()
                    SpeedColumn(label = "Upload Mbps", value = "-\nMbps", showDivider = false)
                }
            }
        }
    }
}

// --- Testing Speed Card ---
@Composable
private fun TestingSpeedCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Speed test animation placeholder
                Image(
                    painter = painterResource(id = R.drawable.robot),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Divider15, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    SpeedColumn(
                        label = "Download Mbps",
                        value = "2.5\nMbps",
                        showDivider = false,
                    )
                    VerticalSpeedDivider()
                    SpeedColumn(
                        label = "Upload Mbps",
                        value = "15.2\nMbps",
                        showDivider = false,
                    )
                }
            }
        }
    }
}

// --- Result Speed Card ---
@Composable
private fun ResultSpeedCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SpeedColumn(
                    label = "Download Mbps",
                    value = "2.5\nMbps",
                )
                VerticalSpeedDivider()
                SpeedColumn(
                    label = "Upload Mbps",
                    value = "15.2\nMbps",
                )
            }
        }
    }
}

@Composable
private fun SpeedColumn(
    label: String,
    value: String,
    showDivider: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val lines = value.split("\n")
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = lines.first(),
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
            )
            if (lines.size > 1) {
                Text(
                    text = lines[1],
                    color = Navy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = NavyMuted,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun VerticalSpeedDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(64.dp)
            .background(Divider15),
    )
}

// --- Speed History Card ---
@Composable
private fun SpeedHistoryCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(CardRadius),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Speed History",
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            // History chart placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No history yet",
                    color = NavyMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}
