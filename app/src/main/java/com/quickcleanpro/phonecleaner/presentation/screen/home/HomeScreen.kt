package com.quickcleanpro.phonecleaner.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.presentation.common.components.styles.CleanXBlue
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
    val viewModel: HomeViewModel = koinViewModel()
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
    val router = LocalRouter.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val tabs =
        listOf(
            HomeTab(stringResource(R.string.home_tab_home), R.drawable.ic_home),
            HomeTab(stringResource(R.string.home_tab_file_manager), R.drawable.ic_file_manager),
            HomeTab(stringResource(R.string.home_tab_toolbox), R.drawable.ic_toolbox),
        )

    Scaffold(
        containerColor = CleanXBlue,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quick Clean PRO",
                        modifier = Modifier.background(HomeBg),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                    )
                },
                actions = {
                    IconButton(onClick = {
                        router.navigate(Screen.Settings)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
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
                    0 -> HomeTabContent(summaryState = summaryState)
                    1 -> FilesManagerTabContent()
                    2 -> ToolBoxTabContent(summaryState = summaryState)
                }
            }
        }
    }
}
