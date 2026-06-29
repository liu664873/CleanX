package com.quickcleanpro.phonecleaner.presentation.screen.toolbox

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import androidx.compose.ui.graphics.Color

@Composable
internal fun ToolboxScaffold(
    title: String,
    onBack: () -> Unit,
    bottom: @Composable (() -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    CleanXScaffold(
        title = title,
        onBack = onBack,
        bottomBar = { bottom?.invoke() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            val contentModifier = Modifier
                .fillMaxSize()
                .let { modifier ->
                    if (scrollable) modifier.verticalScroll(rememberScrollState()) else modifier
                }
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
            Column(
                modifier = contentModifier,
                content = content
            )
        }
    }
}

@Composable
internal fun ToolboxScaffold(
    @StringRes titleRes: Int,
    onBack: () -> Unit,
    bottom: @Composable (() -> Unit)? = null,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ToolboxScaffold(
        title = stringResource(titleRes),
        onBack = onBack,
        bottom = bottom,
        scrollable = scrollable,
        content = content
    )
}

@Composable
internal fun PrimaryBottomButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    CleanXBottomActionBar(
        text = text,
        onClick = onClick,
        enabled = enabled
    )
}

@Composable
internal fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(39.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = CleanXMutedText, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Text(value, color = CleanXText, fontSize = 16.sp)
    }
}

@Composable
internal fun InfoDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE3E8EF))
    )
}

@Composable
internal fun DividerVertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(50.dp)
            .background(Color(0xFFD8DEE7))
    )
}
