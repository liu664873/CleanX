package com.quickcleanpro.phonecleaner.presentation.screen.applock

import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBackground
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBlue
import com.quickcleanpro.phonecleaner.presentation.common.CleanXBottomActionBar
import com.quickcleanpro.phonecleaner.presentation.common.CleanXDivider
import com.quickcleanpro.phonecleaner.presentation.common.CleanXIconButtonSize
import com.quickcleanpro.phonecleaner.presentation.common.CleanXMutedText
import com.quickcleanpro.phonecleaner.presentation.common.CleanXPillShape
import com.quickcleanpro.phonecleaner.presentation.common.CleanXScaffold
import com.quickcleanpro.phonecleaner.presentation.common.CleanXSectionTitle
import com.quickcleanpro.phonecleaner.presentation.common.CleanXText
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXPermissionItem
import com.quickcleanpro.phonecleaner.presentation.common.permission.CleanXProtectedAction
import com.quickcleanpro.phonecleaner.presentation.common.permission.LocalCleanXPermissionCoordinator
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.ConcurrentHashMap

@Composable
internal fun AppLockRoute(
    onBack: () -> Unit,
    viewModel: AppLockViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val permissionCoordinator = LocalCleanXPermissionCoordinator.current
    val toastRes = uiState.toastRes
    val toastMessage = toastRes?.let { stringResource(it) }

    fun openOverlayPermissionSettings() {
        permissionCoordinator.openSettings(
            item = CleanXPermissionItem.Overlay,
            onGranted = {
                viewModel.consumeOverlayPermissionRequest()
                viewModel.refreshAfterResume()
            },
            onRejected = viewModel::consumeOverlayPermissionRequest,
        )
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
        onBack = onBack,
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
        onMonitoringChange = { enabled ->
            if (enabled) {
                permissionCoordinator.guard(
                    action = CleanXProtectedAction.AppLockEnableMonitoring,
                    onGranted = { viewModel.setMonitoringEnabled(true) },
                )
            } else {
                viewModel.setMonitoringEnabled(false)
            }
        },
        onAutoLockChange = viewModel::setAutoLockEnabled,
        onVibrationChange = viewModel::setVibrationEnabled
    )

    if (uiState.overlayPermissionRequired) {
        AppLockOverlayPermissionDialog(
            onAllowNow = ::openOverlayPermissionSettings,
            onCancel = viewModel::consumeOverlayPermissionRequest
        )
    }
}

@Composable
private fun AppLockScreen(
    uiState: AppLockUiState,
    onBack: () -> Unit,
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
    onVibrationChange: (Boolean) -> Unit
) {
    CleanXScaffold(
        titleRes = when (uiState.page) {
            AppLockPage.Settings -> R.string.setting
            AppLockPage.Pin -> uiState.pinStep.titleRes
            else -> R.string.app_lock
        },
        onBack = {
            when (uiState.page) {
                AppLockPage.Search,
                AppLockPage.Settings -> onReturnToManage()
                AppLockPage.Pin -> {
                    if (uiState.pinStep == AppLockPinStep.Verify) {
                        onBack()
                    } else {
                        onLeavePinPage()
                    }
                }
                else -> onBack()
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
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (uiState.page == AppLockPage.SelectApps) {
                CleanXBottomActionBar(
                    text = stringResource(R.string.lock_selected_apps),
                    onClick = onBeginCreatePin,
                    enabled = uiState.hasSelectedApps
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanXBackground)
                .padding(paddingValues)
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
            .padding(horizontal = 30.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        when {
            uiState.isLoading && uiState.apps.isEmpty() -> item { LoadingCard() }
            uiState.apps.isEmpty() -> item { EmptyCard(text = stringResource(R.string.app_lock_no_apps)) }
            else -> items(uiState.apps, key = { it.packageName }) { app ->
                AppLockRow(app = app, onClick = { onTogglePackage(app.packageName) })
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
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 50.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
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
            .padding(horizontal = 20.dp)
    ) {
        SearchInput(
            query = uiState.searchQuery,
            onSearch = onSearch
        )
        Spacer(modifier = Modifier.height(15.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            when {
                query.isBlank() -> Unit
                visibleApps.isEmpty() -> item { EmptyCard(text = stringResource(R.string.app_lock_no_search_results)) }
                else -> items(visibleApps, key = { it.packageName }) { app ->
                    AppLockRow(app = app, onClick = { onTogglePackage(app.packageName) })
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
            .padding(horizontal = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
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
        item { CleanXSectionTitle(text = stringResource(R.string.locked_apps)) }
        items(lockedApps, key = { it.packageName }) { app ->
            AppLockRow(app = app, onClick = { onTogglePackage(app.packageName) })
        }
    }
    if (unlockedApps.isNotEmpty()) {
        item { CleanXSectionTitle(text = stringResource(R.string.my_apps)) }
        items(unlockedApps, key = { it.packageName }) { app ->
            AppLockRow(app = app, onClick = { onTogglePackage(app.packageName) })
        }
    }
}

@Composable
private fun SearchEntryCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_ser),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.search_app),
                color = CleanXMutedText,
                fontSize = 14.sp
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
            .height(130.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.turn_on_auto_lock),
                    color = CleanXText,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                CheckImage(checked = checked)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CleanXDivider)
            )
            Text(
                text = stringResource(R.string.turn_on_auto_lock_hint),
                color = CleanXMutedText,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(69.dp)
                    .padding(top = 16.dp)
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
            .height(55.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = CleanXText,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            CheckImage(checked = checked)
        }
    }
}

@Composable
private fun CheckImage(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(if (checked) R.mipmap.ic_check else R.mipmap.ic_check_nor),
        contentDescription = null,
        modifier = modifier.size(width = 50.dp, height = 30.dp)
    )
}

@Composable
private fun SettingsNavigationCard(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clickable { onClick() },
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = CleanXText,
                fontSize = 16.sp,
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
private fun SearchInput(
    query: String,
    onSearch: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onSearch,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        singleLine = true,
        leadingIcon = {
            Image(
                painter = painterResource(R.mipmap.ic_ser),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
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
                color = CleanXMutedText
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@Composable
private fun AppLockRow(
    app: AppLockApp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
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
            color = CleanXText,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CheckImage(checked = app.isLocked)
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
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = stringResource(uiState.pinStep.hintRes),
            color = PinSelectedColor,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        PinDots(length = uiState.pinInput.length)

        Spacer(modifier = Modifier.height(15.dp))
        Box(
            modifier = Modifier.height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (error != null) {
                Text(
                    text = stringResource(error),
                    color = PinErrorColor,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { translationX = errorOffset.value }
                )
            }
        }

        Spacer(modifier = Modifier.height(50.dp))
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
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) { index ->
            Surface(
                modifier = Modifier.size(20.dp),
                color = if (index < length) PinSelectedColor else Color.Transparent,
                shape = RoundedCornerShape(50),
                border = if (index < length) null else BorderStroke(1.dp, PinUnselectedBorderColor)
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
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                row.forEach { digit ->
                    NumberButton(label = digit.toString(), onClick = { onDigit(digit) })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.size(70.dp))
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
            .size(70.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        color = PinKeyBackground,
        shape = RoundedCornerShape(35.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal
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
private val PinSelectedColor = Color(0xFF2D3748)
private val PinUnselectedBorderColor = Color(0xA68190A5)
private val PinKeyBackground = Color(0x408190A5)
private val PinErrorColor = Color(0xFFEC521A)

@Composable
private fun LoadingCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CleanXBlue)
        }
    }
}

@Composable
private fun EmptyCard(text: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        color = Color.White,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = CleanXMutedText, fontSize = 16.sp)
        }
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
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
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
