package com.quickcleanpro.phonecleaner.presentation.screen.home

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.source.notification.ToolNotificationSpec
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.components.popups.SettingsRateDialog
import com.quickcleanpro.phonecleaner.presentation.common.route.AppNavigationEvent
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private val HomeBg = Color(0xFFF0F3F7)
private val TabActiveColor = Color.White
private val TabInactiveColor = Color(0xFF8DA3D5)

private data class HomeTab(
    val label: String,
    val iconRes: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(externalBlockingPromptActive: Boolean = false) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = koinViewModel()
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val exitPromptSpec = viewModel.exitPromptSpec
    val showAutoRateDialog = viewModel.showAutoRateDialog
    val router = LocalRouter.current
    val scope = rememberCoroutineScope()
    val tabs =
        listOf(
            HomeTab(stringResource(R.string.home_tab_home), R.drawable.ic_home),
            HomeTab(stringResource(R.string.home_tab_file_manager), R.drawable.ic_file_manager),
            HomeTab(stringResource(R.string.home_tab_toolbox), R.drawable.ic_toolbox),
        )
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    BackHandler(enabled = exitPromptSpec == null && !externalBlockingPromptActive && !showAutoRateDialog) {
        viewModel.requestExitPrompt()
    }

    LaunchedEffect(externalBlockingPromptActive) {
        viewModel.setExternalBlockingPromptActive(externalBlockingPromptActive)
    }

    fun exitApp() {
        viewModel.dismissExitPrompt()
        context.findActivity()?.finish()
    }

    fun openExitPromptFeature() {
        val spec = viewModel.consumeExitPromptForNavigation() ?: return
        router.navigate(AppNavigationEvent.Destination(spec.route))
    }

    Scaffold(
        containerColor = CleanXBlue,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        modifier = Modifier.background(HomeBg),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                    )
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.onFeatureClicked()
                        router.navigate(Screen.Settings)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings),
                            modifier = Modifier.size(28.dp),
                            tint = Color.Unspecified,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HomeBg,
                ),
            )
        },
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                verticalAlignment = Alignment.Top,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = pagerState.currentPage == index
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        viewModel.onTabInteraction()
                                        scope.launch { pagerState.animateScrollToPage(index) }
                                    },
                                ).padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(id = tab.iconRes),
                            contentDescription = tab.label,
                            modifier = Modifier.size(30.dp),
                            tint = if (isSelected) TabActiveColor else TabInactiveColor,
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) TabActiveColor else TabInactiveColor,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                    .background(HomeBg),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 ->
                        HomeTabContent(
                            summaryState = summaryState,
                            onFeatureClick = viewModel::onFeatureClicked,
                        )
                    1 ->
                        FilesManagerTabContent(
                            onFeatureClick = viewModel::onFeatureClicked,
                        )
                    2 ->
                        ToolBoxTabContent(
                            summaryState = summaryState,
                            onFeatureClick = viewModel::onFeatureClicked,
                        )
                }
            }
        }
    }

    if (exitPromptSpec != null) {
        HomeExitPromptDialog(
            spec = exitPromptSpec,
            onExit = ::exitApp,
            onViewNow = ::openExitPromptFeature,
        )
    }

    if (showAutoRateDialog && !externalBlockingPromptActive && exitPromptSpec == null) {
        SettingsRateDialog(onDismiss = viewModel::dismissAutoRateDialog)
    }
}

@Composable
private fun HomeExitPromptDialog(
    spec: ToolNotificationSpec,
    onExit: () -> Unit,
    onViewNow: () -> Unit,
) {
    if (spec.route == Screen.NotificationBar.route) {
        NotificationBarExitPromptDialog(
            onEnableNow = onViewNow,
            onClose = onExit,
        )
    } else {
        ToolExitPromptDialog(
            spec = spec,
            onExit = onExit,
            onViewNow = onViewNow,
        )
    }
}

@Composable
private fun ToolExitPromptDialog(
    spec: ToolNotificationSpec,
    onExit: () -> Unit,
    onViewNow: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 343.dp),
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    painter = painterResource(spec.iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(44.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(toolExitPromptMessageRes(spec.route)),
                    color = CleanXText,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { onExit() }
                                .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.exit),
                            color = Color(0xFFB3BDCB),
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = onViewNow,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, CleanXBlue),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = CleanXBlue,
                            ),
                        contentPadding = PaddingValues(horizontal = 19.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.view_now),
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationBarExitPromptDialog(
    onEnableNow: () -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 343.dp),
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.exit_prompt_notification_title),
                    color = CleanXText,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(14.dp))
                NotificationPromptTip(text = stringResource(R.string.exit_prompt_notification_tip_block))
                Spacer(modifier = Modifier.height(13.dp))
                NotificationPromptTip(text = stringResource(R.string.exit_prompt_notification_tip_matters))
                Spacer(modifier = Modifier.height(13.dp))
                NotificationPromptTip(text = stringResource(R.string.exit_prompt_notification_tip_focus))
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onEnableNow,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = CleanXBlue,
                            contentColor = Color.White,
                        ),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.enable_now),
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(13.dp))
                OutlinedButton(
                    onClick = onClose,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CleanXBlue),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = CleanXBlue,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        fontSize = 20.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPromptTip(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(19.dp)
                    .clip(RoundedCornerShape(50))
                    .background(CleanXBlue),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp),
            )
        }
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = text,
            color = Color(0xFF7F8EAA),
            fontSize = 16.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@StringRes
private fun toolExitPromptMessageRes(route: String): Int =
    when (route) {
        Screen.DeviceInfo.route -> R.string.exit_prompt_device_info
        Screen.Scan.route -> R.string.exit_prompt_junk_removal
        Screen.BatteryInfo.route -> R.string.exit_prompt_battery_info
        Screen.NetworkScan.route -> R.string.exit_prompt_network_scan
        Screen.NetworkUsage.route -> R.string.exit_prompt_network_usage
        else -> R.string.exit_prompt_device_info
    }

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
