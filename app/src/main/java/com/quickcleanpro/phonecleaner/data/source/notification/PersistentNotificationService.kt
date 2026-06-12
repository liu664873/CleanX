package com.quickcleanpro.phonecleaner.data.source.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.MainActivity
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.repository.AppLockRepositoryImpl
import com.quickcleanpro.phonecleaner.data.source.applock.LockScreenOverlayService
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistoryOwner
import com.quickcleanpro.phonecleaner.di.QuickCleanApplication
import com.quickcleanpro.phonecleaner.utils.AppLockManager
import com.quickcleanpro.phonecleaner.utils.AppLockPermissionUtils
import com.quickcleanpro.phonecleaner.utils.NotificationChannelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class PersistentNotificationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: AppLockRepositoryImpl
    private var monitorJob: Job? = null
    private var persistentNotificationJob: Job? = null
    private var lastForegroundPackage = ""
    private var lockScreenShowing = false
    private var monitoringEnabled = false
    private var wakeLock: PowerManager.WakeLock? = null

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_START -> syncMonitoringState()
                ACTION_ENABLE_MONITORING -> enableMonitoring()
                ACTION_DISABLE_MONITORING -> disableMonitoring()
                ACTION_APP_FOREGROUND -> _appInForeground.set(true)
                ACTION_APP_BACKGROUND -> {
                    _appInForeground.set(false)
                    handleNotificationTrigger(NotificationTrigger.AppBackground)
                }
                ACTION_RESTORE_PERSISTENT_NOTIFICATION -> schedulePersistentNotificationRestore()
                ACTION_STOP_SERVICE -> {
                    _stopRequested.set(true)
                    disableMonitoring()
                    stopForegroundAndSelf()
                }
                ACTION_PASSWORD_SUCCESS,
                ACTION_LOCK_SCREEN_CANCELLED -> lockScreenShowing = false
            }
        }
    }

    private val systemEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val trigger = when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> NotificationTrigger.ScreenOn
                Intent.ACTION_USER_PRESENT -> NotificationTrigger.UserPresent
                Intent.ACTION_POWER_CONNECTED -> NotificationTrigger.PowerConnected
                Intent.ACTION_POWER_DISCONNECTED -> NotificationTrigger.PowerDisconnected
                else -> null
            } ?: return
            handleNotificationTrigger(trigger)
        }
    }

    private val packageEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val trigger = when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED -> NotificationTrigger.PackageAdded
                Intent.ACTION_PACKAGE_REMOVED -> NotificationTrigger.PackageRemoved
                else -> null
            } ?: return
            if (intent?.getBooleanExtra(Intent.EXTRA_REPLACING, false) == true) return
            handleNotificationTrigger(trigger)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createAllChannels(this)
        startAsForeground()
        _isRunning.set(true)
        _startInFlight.set(false)
        repository = AppLockRepositoryImpl(this)
        if (_stopRequested.get()) {
            disableMonitoring()
            stopForegroundAndSelf()
            return
        }
        startBatteryHistorySampling()
        acquireWakeLock()
        runCatching { registerCommandReceiver() }
        runCatching { registerSystemEventReceiver() }
        startPersistentNotificationWatchdog()
        syncMonitoringState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        startPersistentNotificationWatchdog()
        _isRunning.set(true)
        _startInFlight.set(false)
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                _stopRequested.set(true)
                disableMonitoring()
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                _stopRequested.set(false)
                syncMonitoringState()
            }
            ACTION_ENABLE_MONITORING -> {
                _stopRequested.set(false)
                enableMonitoring()
            }
            ACTION_DISABLE_MONITORING -> {
                _stopRequested.set(false)
                disableMonitoring()
            }
            ACTION_APP_FOREGROUND -> _appInForeground.set(true)
            ACTION_APP_BACKGROUND -> {
                _appInForeground.set(false)
                handleNotificationTrigger(NotificationTrigger.AppBackground)
            }
            ACTION_RESTORE_PERSISTENT_NOTIFICATION -> schedulePersistentNotificationRestore()
            else -> syncMonitoringState()
        }
        startBatteryHistorySampling()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopBatteryHistorySampling()
        disableMonitoring()
        persistentNotificationJob?.cancel()
        persistentNotificationJob = null
        serviceScope.cancel()
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        runCatching { unregisterReceiver(commandReceiver) }
        runCatching { unregisterReceiver(systemEventReceiver) }
        runCatching { unregisterReceiver(packageEventReceiver) }
        _isRunning.set(false)
        _startInFlight.set(false)
        _stopRequested.set(false)
        super.onDestroy()
    }

    private fun syncMonitoringState() {
        if (canMonitor()) {
            enableMonitoring()
        } else {
            disableMonitoring()
        }
    }

    private fun enableMonitoring(): Boolean {
        if (!canMonitor()) {
            disableMonitoring()
            return false
        }
        if (monitoringEnabled) return true
        monitoringEnabled = true
        startMonitoring()
        return true
    }

    private fun disableMonitoring() {
        monitoringEnabled = false
        monitorJob?.cancel()
        monitorJob = null
        lastForegroundPackage = ""
        lockScreenShowing = false
    }

    private fun startMonitoring() {
        if (monitorJob?.isActive == true) return
        monitorJob = serviceScope.launch {
            while (isActive && monitoringEnabled) {
                if (!canMonitor()) {
                    disableMonitoring()
                    break
                }
                checkForegroundApp()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private fun canMonitor(): Boolean =
        runCatching {
            repository.isPinSet() &&
                AppLockManager.isMonitoringEnabled() &&
                repository.lockedAppCount() > 0 &&
                AppLockPermissionUtils.canDrawOverlays(this) &&
                AppLockPermissionUtils.hasUsageStatsPermission(this)
        }.getOrDefault(false)

    private suspend fun checkForegroundApp() {
        val packageName = withContext(Dispatchers.IO) { foregroundPackage() } ?: return
        if (packageName == applicationContext.packageName || packageName == lastForegroundPackage) return
        lastForegroundPackage = packageName
        if (!lockScreenShowing && AppLockManager.isAppLocked(packageName)) {
            showLockScreen(packageName)
        }
    }

    private fun showLockScreen(packageName: String) {
        lockScreenShowing = true
        val intent = Intent(this, LockScreenOverlayService::class.java).apply {
            putExtra(LockScreenOverlayService.EXTRA_TARGET_PACKAGE, packageName)
        }
        runCatching { startService(intent) }
            .onFailure { lockScreenShowing = false }
    }

    private fun foregroundPackage(): String? {
        val manager = runCatching {
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        }.getOrNull() ?: return null
        val now = System.currentTimeMillis()
        val events = runCatching {
            manager.queryEvents((now - EVENT_LOOKBACK_MS).coerceAtLeast(0L), now)
        }.getOrNull()
        val event = UsageEvents.Event()
        var foregroundPackage: String? = null
        if (events != null) {
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val eventPackage = event.packageName ?: continue
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND,
                    UsageEvents.Event.ACTIVITY_RESUMED -> foregroundPackage = eventPackage
                    UsageEvents.Event.MOVE_TO_BACKGROUND,
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED -> {
                        if (foregroundPackage == eventPackage) foregroundPackage = null
                    }
                }
            }
        }
        return foregroundPackage ?: foregroundPackageFromStats(manager, now)
    }

    private fun foregroundPackageFromStats(manager: UsageStatsManager, now: Long): String? =
        runCatching {
            manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                (now - STATS_LOOKBACK_MS).coerceAtLeast(0L),
                now
            )
        }.getOrNull()
            ?.maxByOrNull(UsageStats::getLastTimeUsed)
            ?.packageName

    private fun startAsForeground() {
        val notification = buildPersistentNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                PERSISTENT_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(PERSISTENT_NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundAndSelf() {
        stopBatteryHistorySampling()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun startBatteryHistorySampling() {
        runCatching {
            QuickCleanApplication.instance.batteryHistorySampler.start(BatteryHistoryOwner.Service)
        }
    }

    private fun stopBatteryHistorySampling() {
        runCatching {
            QuickCleanApplication.instance.batteryHistorySampler.stop(BatteryHistoryOwner.Service)
        }
    }

    private fun buildPersistentNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationChannelManager.PERSISTENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_n_notification_cleaner)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.running_in_background))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(persistentNotificationDeletedIntent())
            .build()
    }

    private fun persistentNotificationDeletedIntent(): PendingIntent {
        val intent = Intent(ACTION_RESTORE_PERSISTENT_NOTIFICATION).setPackage(packageName)
        return PendingIntent.getBroadcast(
            this,
            PERSISTENT_NOTIFICATION_DELETE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun schedulePersistentNotificationRestore() {
        if (_stopRequested.get()) return
        serviceScope.launch {
            delay(PERSISTENT_NOTIFICATION_RESTORE_DELAY_MS)
            if (!_stopRequested.get() && hasPostNotificationsPermission()) {
                runCatching { startAsForeground() }
            }
        }
    }

    private fun startPersistentNotificationWatchdog() {
        if (persistentNotificationJob?.isActive == true) return
        persistentNotificationJob = serviceScope.launch {
            while (isActive) {
                delay(PERSISTENT_NOTIFICATION_CHECK_INTERVAL_MS)
                if (_stopRequested.get()) break
                if (hasPostNotificationsPermission() && !isPersistentNotificationActive()) {
                    runCatching { startAsForeground() }
                }
            }
        }
    }

    private fun isPersistentNotificationActive(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return runCatching {
            val manager = getSystemService(NotificationManager::class.java)
            manager.activeNotifications.any { notification ->
                notification.id == PERSISTENT_NOTIFICATION_ID &&
                    notification.packageName == packageName
            }
        }.getOrDefault(true)
    }

    private fun handleNotificationTrigger(trigger: NotificationTrigger) {
        if (!canSendTriggeredNotification(trigger)) return
        serviceScope.launch {
            if (trigger.delayMs > 0L) delay(trigger.delayMs)
            if (_appInForeground.get() || !hasPostNotificationsPermission()) return@launch
            publishToolNotification(trigger)
        }
    }

    private fun canSendTriggeredNotification(trigger: NotificationTrigger): Boolean {
        if (_appInForeground.get() || !hasPostNotificationsPermission()) return false
        val now = System.currentTimeMillis()
        val prefs = notificationTimingPrefs()
        val windowStart = prefs.getLong(KEY_PUSH_WINDOW_START, 0L)
        val count = if (windowStart == 0L || now - windowStart >= PUSH_WINDOW_MS) {
            prefs.edit()
                .putLong(KEY_PUSH_WINDOW_START, now)
                .putInt(KEY_PUSH_WINDOW_COUNT, 0)
                .apply()
            0
        } else {
            prefs.getInt(KEY_PUSH_WINDOW_COUNT, 0)
        }
        if (count >= MAX_TRIGGERED_NOTIFICATIONS_PER_DAY) return false
        if (now - prefs.getLong(KEY_LAST_TRIGGERED_NOTIFICATION, 0L) < GLOBAL_TRIGGER_INTERVAL_MS) {
            return false
        }
        val sceneKey = "${KEY_LAST_TRIGGER_PREFIX}${trigger.key}"
        if (now - prefs.getLong(sceneKey, 0L) < trigger.intervalMs) return false
        prefs.edit()
            .putLong(KEY_LAST_TRIGGERED_NOTIFICATION, now)
            .putLong(sceneKey, now)
            .putInt(KEY_PUSH_WINDOW_COUNT, count + 1)
            .apply()
        return true
    }

    private fun publishToolNotification(trigger: NotificationTrigger) {
        if (!hasPostNotificationsPermission()) return
        val manager = NotificationManagerCompat.from(this)
        val index = notificationIndexFor(trigger)
        val item = ToolNotificationSpecs.getOrNull(index) ?: return
        val notification = NotificationCompat.Builder(this, NotificationChannelManager.TRIGGERED_TOOLS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_n_notification_cleaner)
            .setContentTitle(getString(item.titleRes))
            .setContentText(getString(item.descriptionRes))
            .setContentIntent(targetIntent(item.route, index))
            .setCustomContentView(toolNotificationCollapsedView(item))
            .setCustomBigContentView(toolNotificationExpandedView(item))
            .setCustomHeadsUpContentView(toolNotificationHeadsUpView(item))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .build()
        try {
            manager.notify(TOOL_NOTIFICATION_BASE_ID + index, notification)
        } catch (_: SecurityException) {
            return
        } catch (_: Exception) {
            return
        }
    }

    private fun notificationIndexFor(trigger: NotificationTrigger): Int {
        val preferredTitle = when (trigger) {
            NotificationTrigger.PowerConnected,
            NotificationTrigger.PowerDisconnected -> R.string.battery_info
            NotificationTrigger.PackageAdded,
            NotificationTrigger.PackageRemoved -> R.string.junk_removal
            NotificationTrigger.ScreenOn,
            NotificationTrigger.UserPresent,
            NotificationTrigger.AppBackground -> null
        }
        val preferredIndex = preferredTitle?.let { titleRes ->
            ToolNotificationSpecs.indexOfFirst { it.titleRes == titleRes }
        } ?: -1
        if (preferredIndex >= 0) return preferredIndex
        val prefs = notificationTimingPrefs()
        val nextIndex = (prefs.getInt(KEY_NEXT_TOOL_INDEX, -1) + 1)
            .floorMod(ToolNotificationSpecs.size.coerceAtLeast(1))
        prefs.edit().putInt(KEY_NEXT_TOOL_INDEX, nextIndex).apply()
        return nextIndex
    }

    private fun notificationTimingPrefs() =
        getSharedPreferences(NOTIFICATION_TIMING_PREFS, Context.MODE_PRIVATE)

    private fun hasPostNotificationsPermission(): Boolean =
        runCatching {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)

    private fun targetIntent(route: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "$ACTION_OPEN_TOOL.$route"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ToolNotificationDataSource.EXTRA_TARGET_ROUTE, route)
        }
        return PendingIntent.getActivity(
            this,
            TOOL_CONTENT_REQUEST_BASE_CODE + requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun toolNotificationCollapsedView(item: ToolNotificationSpec): RemoteViews =
        RemoteViews(packageName, R.layout.notification_tool_collapsed).apply {
            setImageViewResource(R.id.iv_icon, item.iconRes)
            setTextViewText(R.id.tv_title, getString(item.titleRes))
            setTextViewText(R.id.tv_desc, getString(item.descriptionRes))
        }

    private fun toolNotificationExpandedView(item: ToolNotificationSpec): RemoteViews =
        RemoteViews(packageName, R.layout.notification_tool_item).apply {
            setImageViewResource(R.id.iv_icon, item.iconRes)
            setTextViewText(R.id.tv_title, getString(item.titleRes))
            setTextViewText(R.id.tv_desc, getString(item.descriptionRes))
            setTextViewText(R.id.tv_action, getString(item.actionRes))
        }

    private fun toolNotificationHeadsUpView(item: ToolNotificationSpec): RemoteViews =
        RemoteViews(packageName, R.layout.notification_tool_heads_up).apply {
            setImageViewResource(R.id.iv_icon, item.iconRes)
            setTextViewText(R.id.tv_title, getString(item.titleRes))
            setTextViewText(R.id.tv_desc, getString(item.descriptionRes))
            setTextViewText(R.id.tv_action, getString(item.actionRes))
        }

    private fun registerCommandReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_START)
            addAction(ACTION_ENABLE_MONITORING)
            addAction(ACTION_DISABLE_MONITORING)
            addAction(ACTION_APP_FOREGROUND)
            addAction(ACTION_APP_BACKGROUND)
            addAction(ACTION_RESTORE_PERSISTENT_NOTIFICATION)
            addAction(ACTION_STOP_SERVICE)
            addAction(ACTION_PASSWORD_SUCCESS)
            addAction(ACTION_LOCK_SCREEN_CANCELLED)
        }
        runCatching {
            ContextCompat.registerReceiver(
                this,
                commandReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    private fun registerSystemEventReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }
        runCatching {
            ContextCompat.registerReceiver(
                this,
                systemEventReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
        runCatching {
            ContextCompat.registerReceiver(
                this,
                packageEventReceiver,
                packageFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    private fun acquireWakeLock() {
        val powerManager = runCatching {
            getSystemService(Context.POWER_SERVICE) as PowerManager
        }.getOrNull() ?: return
        wakeLock = runCatching {
            powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "${javaClass.name}:Persistent"
            ).apply {
                runCatching { acquire(WAKE_LOCK_TIMEOUT_MS) }
            }
        }.getOrNull()
    }

    companion object {
        val isRunning: Boolean
            get() = _isRunning.get()
        private val _isRunning = AtomicBoolean(false)
        private val _startInFlight = AtomicBoolean(false)
        private val _stopRequested = AtomicBoolean(false)
        private val _appInForeground = AtomicBoolean(true)
        private val mainHandler = Handler(Looper.getMainLooper())

        private const val ACTION_START = "com.quickcleanpro.phonecleaner.notification.START"
        const val ACTION_ENABLE_MONITORING = "com.quickcleanpro.phonecleaner.applock.ENABLE_MONITORING"
        const val ACTION_DISABLE_MONITORING = "com.quickcleanpro.phonecleaner.applock.DISABLE_MONITORING"
        private const val ACTION_APP_FOREGROUND = "com.quickcleanpro.phonecleaner.notification.APP_FOREGROUND"
        private const val ACTION_APP_BACKGROUND = "com.quickcleanpro.phonecleaner.notification.APP_BACKGROUND"
        private const val ACTION_RESTORE_PERSISTENT_NOTIFICATION = "com.quickcleanpro.phonecleaner.notification.RESTORE_PERSISTENT"
        private const val ACTION_STOP_SERVICE = "com.quickcleanpro.phonecleaner.notification.STOP_SERVICE"
        const val ACTION_PASSWORD_SUCCESS = "com.quickcleanpro.phonecleaner.applock.PASSWORD_SUCCESS"
        const val ACTION_LOCK_SCREEN_CANCELLED = "com.quickcleanpro.phonecleaner.applock.LOCK_SCREEN_CANCELLED"
        private const val ACTION_OPEN_TOOL = "com.quickcleanpro.phonecleaner.notification.OPEN_TOOL"

        const val PERSISTENT_NOTIFICATION_ID = 17
        private const val PERSISTENT_NOTIFICATION_DELETE_REQUEST_CODE = 17
        private const val TOOL_NOTIFICATION_BASE_ID = 3000
        private const val TOOL_CONTENT_REQUEST_BASE_CODE = 3000
        private const val CHECK_INTERVAL_MS = 500L
        private const val EVENT_LOOKBACK_MS = 3_000L
        private const val STATS_LOOKBACK_MS = 10_000L
        private const val WAKE_LOCK_TIMEOUT_MS = 10L * 60L * 1000L
        private const val START_IN_FLIGHT_RESET_MS = 8_000L
        private const val PERSISTENT_NOTIFICATION_RESTORE_DELAY_MS = 1_000L
        private const val PERSISTENT_NOTIFICATION_CHECK_INTERVAL_MS = 60_000L
        private const val NOTIFICATION_TIMING_PREFS = "triggered_notification_timing"
        private const val KEY_PUSH_WINDOW_START = "push_window_start"
        private const val KEY_PUSH_WINDOW_COUNT = "push_window_count"
        private const val KEY_LAST_TRIGGERED_NOTIFICATION = "last_triggered_notification"
        private const val KEY_LAST_TRIGGER_PREFIX = "last_trigger_"
        private const val KEY_NEXT_TOOL_INDEX = "next_tool_index"
        private const val PUSH_WINDOW_MS = 24L * 60L * 60L * 1000L
//        private const val GLOBAL_TRIGGER_INTERVAL_MS = 30L * 60L * 1000L
        private const val GLOBAL_TRIGGER_INTERVAL_MS = 0L
        private const val DEFAULT_TRIGGER_INTERVAL_MS = 2L  * 60L * 60L * 1000L
        private const val BACKGROUND_TRIGGER_INTERVAL_MS = 60L * 60L * 1000L
        private const val POWER_TRIGGER_INTERVAL_MS = 3L * 60L * 60L * 1000L
        private const val SCREEN_ON_TRIGGER_DELAY_MS = 8_000L
        private const val MAX_TRIGGERED_NOTIFICATIONS_PER_DAY = 500

        fun start(context: Context) {
            val appContext = context.applicationContext
            _stopRequested.set(false)
            val intent = Intent(appContext, PersistentNotificationService::class.java).apply {
                action = ACTION_START
            }
            if (_isRunning.get()) {
                sendCommandBroadcast(appContext, ACTION_START)
                return
            }
            startForegroundCompat(appContext, intent)
        }

        fun enableMonitoring(context: Context) {
            val appContext = context.applicationContext
            _stopRequested.set(false)
            val intent = Intent(appContext, PersistentNotificationService::class.java).apply {
                action = ACTION_ENABLE_MONITORING
            }
            if (_isRunning.get()) {
                sendCommandBroadcast(appContext, ACTION_ENABLE_MONITORING)
                return
            }
            startForegroundCompat(appContext, intent)
        }

        fun disableMonitoring(context: Context) {
            val appContext = context.applicationContext
            _stopRequested.set(false)
            sendCommandBroadcast(appContext, ACTION_DISABLE_MONITORING)
        }


        fun stop(context: Context) {
            val appContext = context.applicationContext
            _stopRequested.set(true)
            sendCommandBroadcast(appContext, ACTION_STOP_SERVICE)
            if (_isRunning.get()) {
                runCatching {
                    appContext.stopService(
                        Intent(appContext, PersistentNotificationService::class.java)
                    )
                }
            }
        }
        fun setAppInForeground(inForeground: Boolean) {
            _appInForeground.set(inForeground)
        }

        fun notifyAppBackground(context: Context) {
            val appContext = context.applicationContext
            _appInForeground.set(false)
            sendCommandBroadcast(appContext, ACTION_APP_BACKGROUND)
        }

        private fun startForegroundCompat(appContext: Context, intent: Intent) {
            if (!_startInFlight.compareAndSet(false, true)) return
            val started = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(appContext, intent)
                } else {
                    appContext.startService(intent)
                }
            }.isSuccess
            if (!started) {
                _startInFlight.set(false)
                return
            }
            mainHandler.postDelayed({
                if (!_isRunning.get()) _startInFlight.set(false)
            }, START_IN_FLIGHT_RESET_MS)
        }

        private fun sendCommandBroadcast(appContext: Context, action: String) {
            runCatching {
                appContext.sendBroadcast(Intent(action).setPackage(appContext.packageName))
            }
        }
    }

    private enum class NotificationTrigger(
        val key: String,
        val intervalMs: Long,
        val delayMs: Long = 0L
    ) {
        ScreenOn("screen_on", DEFAULT_TRIGGER_INTERVAL_MS, SCREEN_ON_TRIGGER_DELAY_MS),
        UserPresent("user_present", DEFAULT_TRIGGER_INTERVAL_MS),
        AppBackground("app_background", BACKGROUND_TRIGGER_INTERVAL_MS),
        PowerConnected("power_connected", POWER_TRIGGER_INTERVAL_MS),
        PowerDisconnected("power_disconnected", POWER_TRIGGER_INTERVAL_MS),
        PackageAdded("package_added", DEFAULT_TRIGGER_INTERVAL_MS),
        PackageRemoved("package_removed", DEFAULT_TRIGGER_INTERVAL_MS)
    }
}

private fun Int.floorMod(other: Int): Int = ((this % other) + other) % other
