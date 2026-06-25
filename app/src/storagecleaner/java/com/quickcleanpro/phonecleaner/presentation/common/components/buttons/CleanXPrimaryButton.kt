package com.quickcleanpro.phonecleaner.presentation.common.components.buttons

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonHeight
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonHorizontalPadding
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonShape
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXButtonVerticalPadding
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXLineSubtitle
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXTextTitle

@Composable
fun CleanXPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val alpha = if (isPressed.value && enabled) 0.65f else 1f

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(CleanXButtonHeight)
            .graphicsLayer(alpha = alpha),
        shape = CleanXButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = CleanXBlue,
            contentColor = Color.White,
            disabledContainerColor = CleanXBlue.copy(alpha = 0.45f),
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = CleanXButtonHorizontalPadding, vertical = CleanXButtonVerticalPadding)
    ) {
        Text(
            text = text,
            fontSize = CleanXTextTitle,
            lineHeight = CleanXLineSubtitle,
            fontWeight = FontWeight.W500
        )
    }
}
