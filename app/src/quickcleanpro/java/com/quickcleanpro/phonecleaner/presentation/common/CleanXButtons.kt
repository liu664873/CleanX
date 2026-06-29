package com.quickcleanpro.phonecleaner.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight

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
        shape = CleanXPillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = CleanXBlue,
            contentColor = Color.White,
            disabledContainerColor = CleanXBlue.copy(alpha = 0.45f),
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = CleanXButtonHorizontalPadding)
    ) {
        Text(
            text = text,
            fontSize = CleanXTextTitle,
            lineHeight = CleanXLineSubtitle,
            fontWeight = FontWeight.W500
        )
    }
}

@Composable
fun CleanXBottomActionBar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CleanXBackground)
            .navigationBarsPadding()
            .padding(horizontal = CleanXPagePadding, vertical = CleanXPagePadding)
    ) {
        CleanXPrimaryButton(
            text = text,
            onClick = onClick,
            enabled = enabled
        )
    }
}
