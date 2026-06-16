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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter

private val Navy = Color(0xFF1D2959)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanXTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    titleFontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    showBack: Boolean = true,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    val router = LocalRouter.current

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
                IconButton(onClick = { router.goBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_left),
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
