package com.quickcleanpro.phonecleaner.presentation.common.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.quickcleanpro.phonecleaner.R

private val Navy = Color(0xFF1D2959)

/**
 * Common top app bar used across all detail pages.
 *
 * @param title Title text displayed in the center
 * @param onBack Callback invoked when the back button is clicked
 * @param titleFontSize Title text size (default 20.sp)
 * @param showBack Whether to show the back navigation icon (default true)
 * @param actions Optional trailing actions composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanXTopAppBar(
    title: String,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    titleFontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    showBack: Boolean = true,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontSize = titleFontSize,
                fontWeight = fontWeight,
                color = Navy,
            )
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ok),
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp),
                        tint = Navy,
                    )
                }
            }
        },
        actions = actions ?: {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
    )
}
