package com.quickcleanpro.phonecleaner.presentation.screen.junkclean

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.components.ToolFeatureBanners
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanCompleteBadge
import com.quickcleanpro.phonecleaner.presentation.common.components.animations.CleanSpiralAnimation
import com.quickcleanpro.phonecleaner.presentation.common.components.buttons.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import kotlinx.coroutines.delay

private val CardBg = Color(0xFFF6F7FB)
private val Navy = Color(0xFF1D2959)
private val NavyMuted = Color(0xA61D2959)
private val Blue = Color(0xFF4179FC)
private val Divider15 = Color(0x261D2959)
private val DarkText = Color(0xFF2D3748)
private val DarkTextMuted = Color(0xA62D3748)
private val CardRadius = 12.dp

private enum class JunkCleanState {
    SCANNING,
    SCAN_RESULT,
    CLEANING,
    CLEAN_COMPLETE,
    CLEAN_RESULT,
}

private data class JunkCategory(
    val resId: Int,
    val sizeText: String,
    val checked: Boolean = true,
)

@Composable
fun JunkCleanScreen() {
    val routeManager = LocalRouter.current
    var state by remember { mutableStateOf(JunkCleanState.SCANNING) }

    val categories = remember {
        mutableStateListOf(
            JunkCategory(R.string.junk_group_system_cache, "1.5 MB"),
            JunkCategory(R.string.junk_group_ad_junk_files, "670 kB"),
            JunkCategory(R.string.junk_group_residual_junks, "540 kB"),
            JunkCategory(R.string.junk_group_obsolete_apks, "432 kB"),
            JunkCategory(R.string.junk_group_other_junk_files, "0 kB", checked = false),
        )
    }

    // Auto-advance states with delays
    LaunchedEffect(state) {
        when (state) {
            JunkCleanState.SCANNING -> {
                delay(2500)
                state = JunkCleanState.SCAN_RESULT
            }
            JunkCleanState.CLEANING -> {
                delay(2000)
                state = JunkCleanState.CLEAN_COMPLETE
            }
            JunkCleanState.CLEAN_COMPLETE -> {
                delay(1200)
                state = JunkCleanState.CLEAN_RESULT
            }
            else -> {}
        }
    }

    CleanXScaffoldPage(
        title = stringResource(R.string.junk_removal),
        contentPadding = PaddingValues(0.dp),
        scrollEnabled = false,
    ) {
        when (state) {
            JunkCleanState.SCANNING -> ScanningContent()
            JunkCleanState.SCAN_RESULT ->
                ScanResultContent(
                    categories = categories,
                    onCleanClick = { state = JunkCleanState.CLEANING },
                )
            JunkCleanState.CLEANING -> CleaningContent()
            JunkCleanState.CLEAN_COMPLETE -> CleanCompleteContent()
            JunkCleanState.CLEAN_RESULT ->
                CleanResultContent(
                    onDone = { routeManager.goBack() },
                )
        }
    }
}

// ==================== SCANNING STATE ====================
@Composable
private fun ScanningContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CleanSpiralAnimation(
                content = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "0",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText,
                        )
                        Text(
                            text = "kB",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkTextMuted,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = stringResource(R.string.scan_loading_fallback),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Navy,
            )
        }
    }
}

// ==================== SCAN RESULT STATE ====================
@Composable
private fun ScanResultContent(
    categories: List<JunkCategory>,
    onCleanClick: () -> Unit,
) {
    val checkedCategories = categories.filter { it.checked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Categories card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shape = RoundedCornerShape(CardRadius),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                categories.forEachIndexed { index, category ->
                    CategoryRow(
                        name = stringResource(category.resId),
                        size = category.sizeText,
                        checked = category.checked,
                    )
                    if (index < categories.lastIndex) {
                        HorizontalDivider(
                            color = Divider15,
                            thickness = 1.dp,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom area
        Text(
            text = "${checkedCategories.size} items • 3.1 MB selected",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = NavyMuted,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(12.dp))

        CleanXPrimaryButton(
            text = stringResource(R.string.clean_now),
            onClick = onCleanClick,
            enabled = checkedCategories.isNotEmpty(),
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CategoryRow(
    name: String,
    size: String,
    checked: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Navy,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = size,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = NavyMuted,
            )
        }

        // Checkbox
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (checked) Blue else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ok),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFC8D2DE).copy(alpha = 0.3f)),
                )
            }
        }
    }
}

// ==================== CLEANING STATE ====================
@Composable
private fun CleaningContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CleanSpiralAnimation(
                content = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "0",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkText,
                        )
                        Text(
                            text = "kB",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkTextMuted,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = stringResource(R.string.scan_loading_fallback),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Navy,
            )
        }
    }
}

// ==================== CLEAN COMPLETE STATE ====================
@Composable
private fun CleanCompleteContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.clean_completed),
            contentDescription = null,
            modifier = Modifier.size(248.dp, 149.dp)
        )
    }
}

// ==================== CLEAN RESULT STATE ====================
@Composable
private fun CleanResultContent(
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Small completion badge at top
        CleanCompleteBadge(badgeSize = 44.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Amount cleaned
        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "3.1",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "MB",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.clean_result_junk_removed),
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF8190A5),
        )

        Spacer(modifier = Modifier.height(24.dp))

        ToolFeatureBanners()

        Spacer(modifier = Modifier.weight(1f))

        // Done button
        CleanXPrimaryButton(
            text = stringResource(R.string.onboarding_done),
            onClick = onDone,
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
