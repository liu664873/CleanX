package com.quickcleanpro.phonecleaner.presentation.screen.home

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.RoundedProgressBar
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.navigation.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.navigation.Screen

private val StorageYellow = Color(0xFFFDBB22)
private val VirusCardBlue = Brush.linearGradient(
    colors = listOf(Color(0xFF90B2FB), Color(0xFF88ABFB)),
)
private val VirusCardLightBlue = Brush.linearGradient(
    colors = listOf(Color(0xFF90E7FB), Color(0xFF88DAFB)),
)

@Composable
fun HomeTabContent(onNavigate: (AppNavigationEvent) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp),
    ) {
        // Storage Card (Yellow)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = StorageYellow,
                shape = RoundedCornerShape(20.dp),
            ) {}

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp),
            ) {
                Row() {
                    Text(
                        text = stringResource(R.string.home_storage_label),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "40.5GB",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = " / 109.7GB",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                RoundedProgressBar(
                    progress = 0.37f,
                    width = 311.dp,
                    height = 16.dp,
                    trackColor = Color(0xA6FFFFFF),
                    fillColor = Color.White,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onNavigate(AppNavigationEvent.Destination(Screen.Scan.route)) },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = StorageYellow,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text(
                        text = stringResource(R.string.home_remove_junk),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Image(
                painter = painterResource(id = R.drawable.robot),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = (-32).dp)
                    .size(width = 90.dp, height = 84.dp),
                contentScale = ContentScale.Fit,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Virus Scan Card 1
        VirusScanCard(
            gradient = VirusCardBlue,
            imageRes = R.drawable.ic_virus_shield,
            imageWidth = 98.dp,
            imageHeight = 81.dp,
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Virus Scan Card 2
        VirusScanCard(
            gradient = VirusCardLightBlue,
            imageRes = R.drawable.ic_virus_shield,
            imageWidth = 81.dp,
            imageHeight = 81.dp,
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun VirusScanCard(
    gradient: Brush,
    imageRes: Int,
    imageWidth: Dp,
    imageHeight: Dp,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradient, shape = shape)
            .clip(shape)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.width(193.dp)) {
                    Text(
                        text = stringResource(R.string.home_virus_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.home_virus_desc),
                        color = Color.White,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(width = imageWidth, height = imageHeight),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = CleanXBlue
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.clean_now),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
