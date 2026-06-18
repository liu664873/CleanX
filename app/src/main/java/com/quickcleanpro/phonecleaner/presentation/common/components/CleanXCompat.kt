package com.quickcleanpro.phonecleaner.presentation.common.components

import android.os.SystemClock
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation

val CleanXBackground = Color(0xFFF7FAFD)
val CleanXText = Color(0xFF2D3748)
val CleanXMutedText = Color(0xFF8190A5)
val CleanXBlue = Color(0xFF4179FC)
val CleanXDivider = Color(0xFFE2EAF3)

val CleanXPagePadding = 16.dp
val CleanXCompactPadding = 8.dp
val CleanXButtonHorizontalPadding = 18.dp
val CleanXButtonHeight = 50.dp
val CleanXCompactButtonHeight = 36.dp
val CleanXIconButtonSize = 40.dp
val CleanXHeaderIconSize = 28.dp
val CleanXHeaderTopPadding = 12.dp
val CleanXHeaderBottomPadding = 14.dp
val CleanXTextTitle = 20.sp
val CleanXTextBody = 16.sp
val CleanXTextCaption = 14.sp
val CleanXTextTiny = 12.sp
val CleanXLineTitle = 27.sp
val CleanXLineSubtitle = 22.sp
val CleanXLineBody = 20.sp
val CleanXLineCaption = 18.sp
val CleanXPillShape = RoundedCornerShape(50)
val CleanXTileShape = RoundedCornerShape(12.dp)

data class CleanXTabItem(
    val title: String,
    val value: String? = null,
)

@Composable
fun CleanXSegmentedTabs(
    items: List<CleanXTabItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 4.dp,
    horizontalSpacing: Dp = 8.dp,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 6.dp,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 20.sp,
    valueFontSize: TextUnit = 12.sp,
    valueLineHeight: TextUnit = 15.sp,
    selectedContainerColor: Color = CleanXBlue,
    unselectedContainerColor: Color = Color(0xFFF6F7FB),
    selectedContentColor: Color = Color.White,
    unselectedContentColor: Color = Color(0xA61D2959),
) {
    if (items.isEmpty()) return
    val safeSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == safeSelectedIndex
            Surface(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(cornerRadius))
                        .clickable { onSelected(index) },
                color = if (selected) selectedContainerColor else unselectedContainerColor,
                shape = RoundedCornerShape(cornerRadius),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = item.title,
                        color = if (selected) selectedContentColor else unselectedContentColor,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.value?.takeIf { it.isNotBlank() }?.let { value ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = value,
                            color = if (selected) selectedContentColor else unselectedContentColor,
                            fontSize = valueFontSize,
                            lineHeight = valueLineHeight,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.cleanXDebouncedClick(
    enabled: Boolean = true,
    debounceMillis: Long = 500L,
    onClick: () -> Unit,
): Modifier =
    composed {
        val lastClickTime = remember { LongArray(1) }
        clickable(enabled = enabled) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastClickTime[0] >= debounceMillis) {
                lastClickTime[0] = now
                onClick()
            }
        }
    }

@Composable
fun CleanXPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = CleanXButtonHeight,
    cornerRadius: Dp = 50.dp,
    fontSize: TextUnit = CleanXTextTitle,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val alpha = if (isPressed.value && enabled) 0.65f else 1f

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer(alpha = alpha),
        shape = RoundedCornerShape(cornerRadius),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = CleanXBlue,
                contentColor = Color.White,
                disabledContainerColor = CleanXBlue.copy(alpha = 0.45f),
                disabledContentColor = Color.White,
            ),
        contentPadding = PaddingValues(horizontal = CleanXButtonHorizontalPadding),
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            lineHeight = CleanXLineSubtitle,
            fontWeight = FontWeight.W500,
        )
    }
}

@Composable
fun CleanXBottomActionBar(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = CleanXBackground,
    buttonModifier: Modifier = Modifier,
    buttonCornerRadius: Dp = 50.dp,
    buttonFontSize: TextUnit = CleanXTextTitle,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .navigationBarsPadding()
                .padding(horizontal = CleanXPagePadding, vertical = CleanXPagePadding),
    ) {
        CleanXPrimaryButton(
            text = text,
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            cornerRadius = buttonCornerRadius,
            fontSize = buttonFontSize,
        )
    }
}


@Composable
fun CleanXHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = CleanXHeaderTopPadding, bottom = CleanXHeaderBottomPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(CleanXIconButtonSize)
                    .clip(CleanXPillShape)
                    .cleanXDebouncedClick { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = CleanXText,
                modifier = Modifier.size(CleanXHeaderIconSize),
            )
        }
        Spacer(modifier = Modifier.size(CleanXCompactPadding / 2))
        Text(
            text = title,
            color = CleanXText,
            fontSize = CleanXTextTitle,
            lineHeight = CleanXLineTitle,
            fontWeight = FontWeight.W500,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

@Composable
fun CleanXScanRingAnimation(
    modifier: Modifier = Modifier,
    ringModifier: Modifier? = null,
    backgroundResId: Int? = R.mipmap.ic_scan_nor_bg,
    backgroundModifier: Modifier? = null,
    ringWidth: Dp = 20.dp,
    ringColor: Color = Color(0xFF1AA7EC),
    backgroundColor: Color = Color(0xFFF1F9FC),
    animationDurationMillis: Int = 1000,
    tailSweepDegrees: Float = 180f,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (backgroundResId != null) {
            Image(
                painter = painterResource(backgroundResId),
                contentDescription = null,
                modifier = backgroundModifier ?: Modifier.matchParentSize(),
                contentScale = ContentScale.Fit,
            )
        }

        val resolvedRingModifier =
            ringModifier ?: if (backgroundResId != null) {
                Modifier.fillMaxSize(0.7f)
            } else {
                Modifier.matchParentSize()
            }

        CleanSpiralAnimation(
            modifier = resolvedRingModifier,
            containerSize = null,
            centerSize = 0.dp,
            animationDurationMillis = animationDurationMillis,
        )

        content()
    }
}

@Composable
fun CleanXDeleteAnimation(
    modifier: Modifier = Modifier,
    fallbackText: String? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CleanSpiralAnimation(
            modifier = Modifier.size(252.dp),
            centerSize = 0.dp,
            animationDurationMillis = 1800,
        )
        Text(
            text = fallbackText ?: stringResource(R.string.delete_loading_fallback),
            color = CleanXMutedText,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun CleanXFullScreenDeleteAnimation(
    modifier: Modifier = Modifier,
    fallbackText: String? = null,
    backgroundColor: Color = CleanXBackground,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        CleanXDeleteAnimation(
            modifier = Modifier.fillMaxSize(),
            fallbackText = fallbackText,
        )
    }
}

@Composable
fun CommonResultContent(
    onNavigateTool: (String) -> Unit,
    modifier: Modifier = Modifier,
    excludedToolRoutes: Set<String> = emptySet(),
    completionContent: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            completionContent()
            Spacer(modifier = Modifier.height(24.dp))
            ToolFeatureBanners(
                excludeRoutes = excludedToolRoutes,
            )
        }
    }
}

@Composable
fun CommonResultCheckIcon(
    modifier: Modifier = Modifier,
    size: Dp = 45.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(0xFFCFEFFF)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(size * 0.74f)
                    .clip(CircleShape)
                    .background(Color(0xFF1AA7EC)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.52f),
            )
        }
    }
}
