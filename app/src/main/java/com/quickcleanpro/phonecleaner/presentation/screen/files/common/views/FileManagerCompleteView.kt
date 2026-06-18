package com.quickcleanpro.phonecleaner.presentation.screen.files.common.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.presentation.common.components.CelebratingView
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanCompleteBadge

@Composable
internal fun FileManagerCompleteView() {
    Column() {
        CelebratingView()
        Spacer(modifier = Modifier.fillMaxWidth().height(111.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Cleanup Compled...",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF1D2959),
                textAlign = TextAlign.Center,
            )
        )
    }
}
