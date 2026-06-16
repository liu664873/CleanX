package com.quickcleanpro.phonecleaner.presentation.screen.applock

import android.content.Context
import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.domain.model.applock.AppLockApp
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXIconButtonSize
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPillShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPrimaryButton
import com.quickcleanpro.phonecleaner.presentation.common.components.CleanXScaffoldPage
import com.quickcleanpro.phonecleaner.presentation.common.permission.PermissionGateConfig
import com.quickcleanpro.phonecleaner.presentation.common.route.LocalRouter
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.ConcurrentHashMap

@Composable
internal fun AppLockRoute(
    viewModel: AppLockViewModel = koinViewModel(),
    permissionGateConfig: PermissionGateConfig? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val toastRes = uiState.toastRes
    val toastMessage = toastRes?.let { stringResource(it) }

    fun openOverlayPermissionSettings() {
        val intent = viewModel.overlayPermissionIntent()
        if (intent == null) {
            viewModel.dismissOverlayPermissionDialog()
            return
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            viewModel.dismissOverlayPermissionDialog()
        } catch (_: Exception) {
            viewModel.dismissOverlayPermissionDialog()
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAfterResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }

    AppLockScreen(
        uiState = uiState,
        onLeavePinPage = viewModel::leavePinPage,
        onReturnToManage = viewModel::returnToManage,
        onOpenSearch = viewModel::openSearch,
        onOpenSettings = viewModel::openSettings,
        onSearch = viewModel::updateSearchQuery,
        onTogglePackage = viewModel::togglePackage,
        onToggleAll = viewModel::toggleAllApps,
        onBeginCreatePin = viewModel::beginCreatePin,
        onStartChangePin = viewModel::startChangePin,
        onDigit = viewModel::addPinDigit,
        onDeleteDigit = viewModel::removePinDigit,
        onMonitoringChange = viewModel::setMonitoringEnabled,
        onAutoLockChange = viewModel::setAutoLockEnabled,
        onVibrationChange = viewModel::setVibrationEnabled,
        permissionGateConfig = permissionGateConfig
    )

    if (uiState.overlayPermissionRequired) {
        AppLockOverlayPermissionDialog(
            onAllowNow = ::openOverlayPermissionSettings,
            onCancel = viewModel::dismissOverlayPermissionDialog
        )
    }
}

@Composable
private fun AppLockScreen(
    uiState: AppLockUiState,
    onLeavePinPage: () -> Unit,
    onReturnToManage: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearch: (String) -> Unit,
    onTogglePackage: (String) -> Unit,
    onToggleAll: () -> Unit,
    onBeginCreatePin: () -> Unit,
    onStartChangePin: () -> Unit,
    onDigit: (Char) -> Unit,
    onDeleteDigit: () -> Unit,
    onMonitoringChange: (Boolean) -> Unit,
    onAutoLockChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    permissionGateConfig: PermissionGateConfig?
) {
    val router = LocalRouter.current
    CleanXScaffoldPage(
        title = stringResource(
            when (uiState.page) {
            AppLockPage.Settings -> R.string.setting
            AppLockPage.Pin -> uiState.pinStep.titleRes
            else -> R.string.app_lock
            }
        ),
        scrollEnabled = false,
        contentPadding = PaddingValues(0.dp),
        onBack = {
            when (uiState.page) {
                AppLockPage.Search,
                AppLockPage.Settings -> onReturnToManage()
                AppLockPage.Pin -> {
                    if (uiState.pinStep == AppLockPinStep.Verify) {
                        router.goBack()
                    } else {
                        onLeavePinPage()
                    }
                }
                else -> router.goBack()
            }
        },
        actions = {
            if (uiState.page == AppLockPage.Manage) {
                Box(
                    modifier = Modifier
                        .size(CleanXIconButtonSize)
                        .clip(CleanXPillShape)
                        .clickable { onOpenSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_setting),
                        contentDescription = stringResource(R.string.setting),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        permissionGateConfig = permissionGateConfig,
        bottomBar = {
            if (uiState.page == AppLockPage.SelectApps) {
                AppLockBottomBar(
                    text = stringResource(R.string.lock_selected_apps),
                    onClick = onBeginCreatePin,
                    enabled = uiState.hasSelectedApps
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppLockBackground)
        ) {
            when (uiState.page) {
                AppLockPage.SelectApps -> SelectAppsPage(
                    uiState = uiState,
                    onTogglePackage = onTogglePackage
                )
                AppLockPage.Pin -> PinPage(
                    uiState = uiState,
                    onDigit = onDigit,
                    onDelete = onDeleteDigit
                )
                AppLockPage.Manage -> ManagePage(
                    uiState = uiState,
                    onOpenSearch = onOpenSearch,
                    onTogglePackage = onTogglePackage,
                    onToggleAll = onToggleAll,
                    onAutoLockChange = onAutoLockChange
                )
                AppLockPage.Search -> SearchPage(
                    uiState = uiState,
                    onSearch = onSearch,
                    onTogglePackage = onTogglePackage
                )
                AppLockPage.Settings -> SettingsPage(
                    uiState = uiState,
                    onMonitoringChange = onMonitoringChange,
                    onVibrationChange = onVibrationChange,
                    onStartChangePin = onStartChangePin
                )
            }
        }
    }
}

@Composable
private fun SelectAppsPage(
    uiState: AppLockUiState,
    onTogglePackage: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            AppLockCard {
                when {
                    uiState.isLoading && uiState.apps.isEmpty() -> LoadingState()
                    uiState.apps.isEmpty() -> EmptyState(text = stringResource(R.string.app_lock_no_apps))
                    else -> AppLockRows(
                        apps = uiState.apps,
                        onTogglePackage = onTogglePackage,
                        showDividers = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ManagePage(
    uiState: AppLockUiState,
    onOpenSearch: () -> Unit,
    onTogglePackage: (String) -> Unit,
    onToggleAll: () -> Unit,
    onAutoLockChange: (Boolean) -> Unit
) {
    val noAppsText = stringResource(R.string.app_lock_no_apps)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 28.dp, bottom = 50.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SearchEntryCard(onClick = onOpenSearch) }
        item {
            AutoLockCard(
                checked = uiState.autoLockEnabled,
                onClick = { onAutoLockChange(!uiState.autoLockEnabled) }
            )
        }
        item {
            CheckActionCard(
                title = stringResource(R.string.lock_all_apps),
                checked = uiState.allAppsLocked,
                onClick = onToggleAll
            )
        }
        if (uiState.isLoading && uiState.apps.isEmpty()) {
            item { LoadingCard() }
        } else {
            appSections(
                apps = uiState.apps,
                emptyText = noAppsText,
                onTogglePackage = onTogglePackage
            )
        }
    }
}

@Composable
private fun SearchPage(
    uiState: AppLockUiState,
    onSearch: (String) -> Unit,
    onTogglePackage: (String) -> Unit
) {
    val query = uiState.searchQuery.trim()
    val visibleApps = remember(uiState.apps, query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            uiState.apps.filter { app -> app.appName.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SearchInput(
            query = uiState.searchQuery,
            onSearch = onSearch
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            when {
                query.isBlank() -> Unit
                visibleApps.isEmpty() -> item { EmptyCard(text = stringResource(R.string.app_lock_no_search_results)) }
                else -> item {
                    AppLockCard {
                        AppLockRows(
                            apps = visibleApps,
                            onTogglePackage = onTogglePackage,
                            showDividers = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPage(
    uiState: AppLockUiState,
    onMonitoringChange: (Boolean) -> Unit,
    onVibrationChange: (Boolean) -> Unit,
    onStartChangePin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CheckActionCard(
            title = stringResource(R.string.enable),
            checked = uiState.monitoringEnabled,
            onClick = { onMonitoringChange(!uiState.monitoringEnabled) }
        )
        CheckActionCard(
            title = stringResource(R.string.haptic_feedback),
            checked = uiState.vibrationEnabled,
            onClick = { onVibrationChange(!uiState.vibrationEnabled) }
        )
        SettingsNavigationCard(
            title = stringResource(R.string.change_pin),
            onClick = onStartChangePin
        )
    }
}

private fun LazyListScope.appSections(
    apps: List<AppLockApp>,
    emptyText: String,
    onTogglePackage: (String) -> Unit
) {
    if (apps.isEmpty()) {
        item { EmptyCard(text = emptyText) }
        return
    }
    val lockedApps = apps.filter { it.isLocked }
    val unlockedApps = apps.filterNot { it.isLocked }
    if (lockedApps.isNotEmpty()) {
        item {
            AppLockSectionCard(title = stringResource(R.string.locked_apps)) {
                AppLockRows(
                    apps = lockedApps,
                    onTogglePackage = onTogglePackage,
                    showDividers = false
                )
            }
        }
    }
    if (unlockedApps.isNotEmpty()) {
        item {
            AppLockSectionCard(title = stringResource(R.string.my_apps)) {
                AppLockRows(
                    apps = unlockedApps,
                    onTogglePackage = onTogglePackage,
                    showDividers = false
                )
            }
        }
    }
}

@Composable
private fun SearchEntryCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_ser),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.search_app),
                color = AppLockPlaceholderText,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun AutoLockCard(
    checked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(29.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.turn_on_auto_lock),
                    color = AppLockNavy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                AppLockSwitch(checked = checked)
            }
            AppLockDivider()
            Text(
                text = stringResource(R.string.turn_on_auto_lock_hint),
                color = AppLockSecondaryText,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CheckActionCard(
    title: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = AppLockNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            AppLockSwitch(checked = checked)
        }
    }
}

@Composable
private fun AppLockSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 22.dp, height = 9.dp)
                .clip(CleanXPillShape)
                .background(if (checked) AppLockSwitchTrackOn else AppLockSwitchTrackOff)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(10.5.dp)
                .clip(CircleShape)
                .background(if (checked) CleanXBlue else AppLockSwitchThumbOff)
        )
    }
}

@Composable
private fun SettingsNavigationCard(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = AppLockNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Image(
                painter = painterResource(R.mipmap.ic_next),
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun AppLockCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppLockCardColor,
        shape = RoundedCornerShape(AppLockCardRadius)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            content = content
        )
    }
}

@Composable
private fun AppLockSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    AppLockCard {
        Text(
            text = title,
            color = AppLockNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppLockDivider()
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun AppLockDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppLockDividerColor)
    )
}

@Composable
private fun AppLockBottomBar(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppLockBackground)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        CleanXPrimaryButton(
            text = text,
            onClick = onClick,
            enabled = enabled,
            height = 52.dp,
            cornerRadius = 10.dp,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun SearchInput(
    query: String,
    onSearch: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onSearch,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        leadingIcon = {
            Image(
                painter = painterResource(R.mipmap.ic_ser),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                Image(
                    painter = painterResource(R.mipmap.ic_close),
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CleanXPillShape)
                        .clickable { onSearch("") }
                )
            }
        },
        placeholder = {
            Text(
                text = stringResource(R.string.please_enter_app_name),
                color = AppLockPlaceholderText,
                fontSize = 18.sp
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = AppLockNavy,
            fontSize = 18.sp
        ),
        shape = RoundedCornerShape(AppLockCardRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppLockCardColor,
            unfocusedContainerColor = AppLockCardColor,
            disabledContainerColor = AppLockCardColor,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = AppLockNavy,
            unfocusedTextColor = AppLockNavy,
            cursorColor = CleanXBlue
        )
    )
}

@Composable
private fun AppLockRows(
    apps: List<AppLockApp>,
    onTogglePackage: (String) -> Unit,
    showDividers: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (showDividers) 0.dp else 24.dp)) {
        apps.forEachIndexed { index, app ->
            AppLockRow(app = app, onClick = { onTogglePackage(app.packageName) })
            if (showDividers && index != apps.lastIndex) {
                Spacer(modifier = Modifier.height(24.dp))
                AppLockDivider()
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AppLockRow(
    app: AppLockApp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        PackageAppIcon(
            packageName = app.packageName,
            fallbackText = app.appName.take(1).ifBlank { "A" }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = app.appName,
            color = AppLockNavy,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        AppLockSwitch(checked = app.isLocked)
    }
}

@Composable
private fun PinPage(
    uiState: AppLockUiState,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val error = uiState.pinErrorRes
    val errorOffset = remember { Animatable(0f) }

    LaunchedEffect(error) {
        if (error != null) {
            errorOffset.snapTo(0f)
            listOf(20f, -20f, 20f, -20f, 0f).forEach { target ->
                errorOffset.animateTo(target, tween(durationMillis = 80))
            }
        } else {
            errorOffset.snapTo(0f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))
        Text(
            text = stringResource(uiState.pinStep.hintRes),
            color = AppLockNavy,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(42.dp))
        PinDots(length = uiState.pinInput.length)

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier.height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (error != null) {
                Text(
                    text = stringResource(error),
                    color = PinErrorColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { translationX = errorOffset.value }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        NumberPad(
            onDigit = { digit ->
                if (uiState.vibrationEnabled && uiState.pinInput.length != PIN_LENGTH) {
                    performPinVibration(context)
                }
                onDigit(digit)
            },
            onDelete = onDelete
        )
    }
}

@Composable
private fun PinDots(length: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        repeat(4) { index ->
            Surface(
                modifier = Modifier.size(24.dp),
                color = if (index < length) PinSelectedColor else Color.Transparent,
                shape = RoundedCornerShape(50),
                border = if (index < length) null else BorderStroke(2.dp, PinUnselectedBorderColor)
            ) {}
        }
    }
}

@Composable
private fun NumberPad(
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(44.dp)) {
                row.forEach { digit ->
                    NumberButton(label = digit.toString(), onClick = { onDigit(digit) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(44.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(66.dp))
            NumberButton(label = "0", onClick = { onDigit('0') })
            NumberButton(label = "\u00D7", onClick = onDelete)
        }
    }
}

@Composable
private fun NumberButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .size(66.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = PinKeyBackground,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = AppLockNavy,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun performPinVibration(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30L)
        }
    }
}

private const val PIN_LENGTH = 4
private val AppLockBackground = Color(0x00000000)
private val AppLockCardColor = Color(0xFFF6F7FB)
private val AppLockNavy = Color(0xFF1D2959)
private val AppLockSecondaryText = Color(0xA61D2959)
private val AppLockPlaceholderText = Color(0x591D2959)
private val AppLockDividerColor = Color(0x332D3748)
private val AppLockSwitchTrackOff = Color(0xFFECF0F4)
private val AppLockSwitchThumbOff = Color(0xFFAFBBD0)
private val AppLockSwitchTrackOn = Color(0xFFBADDFF)
private val AppLockCardRadius = 12.dp
private val PinSelectedColor = Color(0xFF1D2959)
private val PinUnselectedBorderColor = Color(0xFF66749A)
private val PinKeyBackground = Color.White.copy(alpha = 0.65f)
private val PinErrorColor = Color(0xFFEC521A)

@Composable
private fun LoadingCard() {
    AppLockCard {
        LoadingState()
    }
}

@Composable
private fun EmptyCard(text: String) {
    AppLockCard {
        EmptyState(text = text)
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = CleanXBlue)
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = AppLockSecondaryText, fontSize = 16.sp)
    }
}

@Composable
private fun PackageAppIcon(
    packageName: String,
    fallbackText: String
) {
    val context = LocalContext.current
    val bitmap = remember(packageName) { loadPackageIconBitmap(context, packageName) }
    if (bitmap != null) {
        Image(
            painter = BitmapPainter(bitmap),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CleanXBlue.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackText,
                color = CleanXBlue,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val appLockIconCache = ConcurrentHashMap<String, ImageBitmap>()

private fun loadPackageIconBitmap(context: android.content.Context, packageName: String): ImageBitmap? {
    appLockIconCache[packageName]?.let { return it }
    val drawable = runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
        ?: return null
    val bitmap = drawable.safeBitmap().asImageBitmap()
    appLockIconCache[packageName] = bitmap
    return bitmap
}

private fun Drawable.safeBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    return toBitmap(width = 96, height = 96)
}
