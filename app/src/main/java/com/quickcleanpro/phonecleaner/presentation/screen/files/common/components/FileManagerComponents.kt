package com.quickcleanpro.phonecleaner.presentation.screen.files.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXBlue

internal val FileManagerListBottomPadding = 112.dp
internal val FileManagerPageBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFFE3ECFD), Color(0xFFDFEBF5))
)
internal val FileManagerCardColor = Color(0xFFF6F7FB)
internal val FileManagerNavy = Color(0xFF1D2959)
internal val FileManagerMutedNavy = Color(0xA61D2959)
internal val FileManagerDividerColor = Color(0xFFD4D7DE)

@Composable
internal fun SelectionCircle(selected: Boolean, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(
            id = if (selected) R.drawable.ic_selected else R.drawable.ic_unselected
        ),
        contentDescription = null,
        modifier = modifier.size(21.dp)
    )
}

@Composable
internal fun FileManagerSelectAllAction(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Row(
        modifier = modifier.clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(if (selected) R.string.file_unselect_all else R.string.file_select_all),
            color = CleanXBlue,
            fontSize = if (compact) 16.sp else 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
        )
        SelectionCircle(selected = selected)
    }
}
