package com.quickcleanpro.phonecleaner.data.source.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quickcleanpro.phonecleaner.BuildConfig
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.config.AppConfig
import com.quickcleanpro.phonecleaner.data.repository.AppLockRepositoryImpl
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistoryOwner
import com.quickcleanpro.phonecleaner.data.source.battery.BatteryHistorySampler
import com.quickcleanpro.phonecleaner.presentation.common.route.Screen
import com.quickcleanpro.phonecleaner.utils.NotificationChannelManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Long-running foreground service that keeps the app alive for:
 * - Persistent notification
 * - AppLock foreground monitoring (delegated to [AppLockMonitorHandler])
 * - Triggered tool notifications (delegated to [TriggeredNotificationEngine])
 * - Battery history sampling (delegated to [BatteryHistorySampler])
 */
class PersistentNotificationService : Service() {
    private val batteryHistorySampler: BatteryHistorySampler by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var persistentNotificationJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var repository: AppLockRepositoryImpl
    private lateinit var monitorHandler: AppLockMonitorHandler
    private lateinit var notificationEngine: TriggeredNotificationEngine

    // ========== lifecycle ==========

    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createAllChannels(this)
        startAsForeground()
        _isRunning.set(true)
        startInFlight.set(false)
        repository = AppLockRepositoryImpl(this)

        monitorHandler = AppLockMonitorHandler(this, repository, serviceScope)
        notificationEngine = TriggeredNotificationEngine(
            context = this,
            serviceScope = serviceScope,
            appInForeground = appInForeground,
            timingPrefs = getSharedPreferences(
                TriggeredNotificationEngine.NOTIFICATION_TIMING_PREFS, Context.MODE_PRIVATE,
            ),
            onPackageEvent = { action, pkg ->
                when (action) {
                    PackageSyncAction.Added -> runCatching { repository.handlePackageAdded(pkg) }
                    PackageSyncAction.Removed -> runCatching { repository.handlePackageRemoved(pkg) }
                }
                monitorHandler.syncMonitoringState()
            },
        )

        if (stopRequested.get()) {
            monitorHandler.disableMonitoring()
            stopForegroundAndSelf()
            return
        }
        startBatteryHistorySampling()
        acquireWakeLock()
        runCatching { registerCommandReceiver() }
        notificationEngine.start()
        startPersistentNotificationWatchdog()
        monitorHandler.syncMonitoringState()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        startAsForeground()
        startPersistentNotificationWatchdog()
        _isRunning.set(true)
        startInFlight.set(false)
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                stopRequested.set(true)
                monitorHandler.disableMonitoring()
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                stopRequested.set(false)
                monitorHandler.syncMonitoringState()
            }
            ACTION_ENABLE_MONITORING -> {
                stopRequested.set(false)
                monitorHandler.enableMonitoring()
            }
            ACTION_DISABLE_MONITORING -> {
                stopRequested.set(false)
                monitorHandler.disableMonitoring()
            }
            ACTION_APP_FOREGROUND -> appInForeground.set(true)
            ACTION_APP_BACKGROUND -> {
                appInForeground.set(false)
                notificationEngine.triggerAppBackground()
            }
            ACTION_RESTORE_PERSISTENT_NOTIFICATION -> schedulePersistentNotificationRestore()
            else -> monitorHandler.syncMonitoringState()
        }
        startBatteryHistorySampling()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopBatteryHistorySampling()
        monitorHandler.disableMonitoring()
        notificationEngine.stop()
        persistentNotificationJob?.cancel()
        persistentNotificationJob = null
        serviceScope.cancel()
        runCatching { wakeLock?.takeIf { it.isHeld }?.release() }
        runCatching { unregisterReceiver(commandReceiver) }
        _isRunning.set(false)
        startInFlight.set(false)
        stopRequested.set(false)
        super.onDestroy()
    }

    // ========== foreground / persistent notification ==========

    private fun startAsForeground() {
        val notification = buildPersistentNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(PERSISTENT_NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(PERSISTENT_NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundAndSelf() {
        stopBatteryHistorySampling()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
        stopSelf()
    }

    private fun buildPersistentNotification(): Notification =
        if (BuildConfig.VARIANT_KEY == VARIANT_STORAGE_CLEANER) {
            buildStorageCleanerPersistentNotification()
        } else {
            buildDefaultPersistentNotification()
        }

    private fun buildDefaultPersistentNotification(): Notification =
        persistentNotificationBuilder()
            .build()

    private fun buildStorageCleanerPersistentNotification(): Notification {
        val layoutId = storageCleanerResourceId("layout", "notification_persistent_shortcuts")
        if (layoutId == 0) return buildDefaultPersistentNotification()

        val views =
            RemoteViews(packageName, layoutId).apply {
                setTextIfPresent("persistent_notification_title", getString(R.string.app_name))
                setTextIfPresent("persistent_notification_desc", getString(R.string.running_in_background))
                setShortcutIfPresent(
                    idName = "persistent_shortcut_clean",
                    route = Screen.Scan.route,
                    requestCode = PERSISTENT_SHORTCUT_CLEAN_REQUEST_CODE,
                )
                setShortcutIfPresent(
                    idName = "persistent_shortcut_file",
                    route = ToolNotificationIntentFactory.ROUTE_HOME_FILE_MANAGER,
                    requestCode = PERSISTENT_SHORTCUT_FILE_REQUEST_CODE,
                )
                setShortcutIfPresent(
                    idName = "persistent_shortcut_tools",
                    route = ToolNotificationIntentFactory.ROUTE_HOME_TOOLBOX,
                    requestCode = PERSISTENT_SHORTCUT_TOOLS_REQUEST_CODE,
                )
                setShortcutIfPresent(
                    idName = "persistent_shortcut_battery",
                    route = Screen.BatteryInfo.route,
                    requestCode = PERSISTENT_SHORTCUT_BATTERY_REQUEST_CODE,
                )
            }

        return persistentNotificationBuilder()
            .setContentIntent(
                ToolNotificationIntentFactory.pendingIntent(
                    context = this,
                    route = Screen.Home.route,
                    requestCode = PERSISTENT_NOTIFICATION_CONTENT_REQUEST_CODE,
                ),
            )
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()
    }

    private fun persistentNotificationBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(this, NotificationChannelManager.PERSISTENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_n_notification_cleaner)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.running_in_background))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(persistentNotificationDeletedIntent())

    private fun RemoteViews.setTextIfPresent(
        idName: String,
        text: CharSequence,
    ) {
        val viewId = storageCleanerResourceId("id", idName)
        if (viewId != 0) setTextViewText(viewId, text)
    }

    private fun RemoteViews.setShortcutIfPresent(
        idName: String,
        route: String,
        requestCode: Int,
    ) {
        val viewId = storageCleanerResourceId("id", idName)
        if (viewId == 0) return
        setOnClickPendingIntent(
            viewId,
            ToolNotificationIntentFactory.pendingIntent(
                context = this@PersistentNotificationService,
                route = route,
                requestCode = requestCode,
            ),
        )
    }

    private fun storageCleanerResourceId(
        type: String,
        name: String,
    ): Int =
        resources.getIdentifier(name, type, packageName)

    private fun persistentNotificationDeletedIntent(): PendingIntent {
        val intent = Intent(ACTION_RESTORE_PERSISTENT_NOTIFICATION).setPackage(packageName)
        return PendingIntent.getBroadcast(
            this, PERSISTENT_NOTIFICATION_DELETE_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun schedulePersistentNotificationRestore() {
        if (stopRequested.get()) return
        serviceScope.launch {
            delay(PERSISTENT_NOTIFICATION_RESTORE_DELAY_MS)
            if (!stopRequested.get() && AppConfig.hasPostNotificationsPermission(this@PersistentNotificationService)) {
                runCatching { startAsForeground() }
            }
        }
    }

    private fun startPersistentNotificationWatchdog() {
        if (persistentNotificationJob?.isActive == true) return
        persistentNotificationJob = serviceScope.launch {
            while (isActive) {
                delay(PERSISTENT_NOTIFICATION_CHECK_INTERVAL_MS)
                if (stopRequested.get()) break
                if (AppConfig.hasPostNotificationsPermission(this@PersistentNotificationService) && !isPersistentNotificationActive()) {
                    runCatching { startAsForeground() }
                }
            }
        }
    }

    private fun isPersistentNotificationActive(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        return runCatching {
            val manager = getSystemService(NotificationManager::class.java)
            manager.activeNotifications.any { it.id == PERSISTENT_NOTIFICATION_ID && it.packageName == packageName }
        }.getOrDefault(true)
    }

    // ========== battery ==========

    private fun startBatteryHistorySampling() {
        runCatching { batteryHistorySampler.start(BatteryHistoryOwner.Service) }
    }

    private fun stopBatteryHistorySampling() {
        runCatching { batteryHistorySampler.stop(BatteryHistoryOwner.Service) }
    }

    // ========== command receiver ==========

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.`package` != packageName) return
            when (intent?.action) {
                ACTION_STOP_SERVICE -> {
                    stopRequested.set(true)
                    monitorHandler.disableMonitoring()
                    stopForegroundAndSelf()
                }
                ACTION_ENABLE_MONITORING -> monitorHandler.enableMonitoring()
                ACTION_DISABLE_MONITORING -> monitorHandler.disableMonitoring()
                ACTION_APP_FOREGROUND -> appInForeground.set(true)
                ACTION_APP_BACKGROUND -> {
                    appInForeground.set(false)
                    notificationEngine.triggerAppBackground()
                }
                ACTION_RESTORE_PERSISTENT_NOTIFICATION -> schedulePersistentNotificationRestore()
                ACTION_START -> monitorHandler.syncMonitoringState()
                ACTION_PASSWORD_SUCCESS, ACTION_LOCK_SCREEN_CANCELLED -> monitorHandler.dismissLockScreen()
            }
        }
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
        ContextCompat.registerReceiver(this, commandReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    // ========== wake lock ==========

    private fun acquireWakeLock() {
        val powerManager = runCatching {
            getSystemService(Context.POWER_SERVICE) as PowerManager
        }.getOrNull() ?: return
        wakeLock = runCatching {
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${javaClass.name}:Persistent")
                .apply { runCatching { acquire(WAKE_LOCK_TIMEOUT_MS) } }
        }.getOrNull()
    }

    // ========== companion (external API) ==========

    companion object {
        val isRunning: Boolean get() = _isRunning.get()
        private val _isRunning = AtomicBoolean(false)
        private val startInFlight = AtomicBoolean(false)
        private val stopRequested = AtomicBoolean(false)
        private val appInForeground = AtomicBoolean(true)
        private val mainHandler = Handler(Looper.getMainLooper())

        // action constants — shared by broadcast sender/receiver
        private const val ACTION_START = "com.quickcleanpro.phonecleaner.notification.START"
        const val ACTION_ENABLE_MONITORING = "com.quickcleanpro.phonecleaner.applock.ENABLE_MONITORING"
        const val ACTION_DISABLE_MONITORING = "com.quickcleanpro.phonecleaner.applock.DISABLE_MONITORING"
        private const val ACTION_APP_FOREGROUND = "com.quickcleanpro.phonecleaner.notification.APP_FOREGROUND"
        private const val ACTION_APP_BACKGROUND = "com.quickcleanpro.phonecleaner.notification.APP_BACKGROUND"
        private const val ACTION_RESTORE_PERSISTENT_NOTIFICATION = "com.quickcleanpro.phonecleaner.notification.RESTORE_PERSISTENT"
        private const val ACTION_STOP_SERVICE = "com.quickcleanpro.phonecleaner.notification.STOP_SERVICE"
        const val ACTION_PASSWORD_SUCCESS = "com.quickcleanpro.phonecleaner.applock.PASSWORD_SUCCESS"
        const val ACTION_LOCK_SCREEN_CANCELLED = "com.quickcleanpro.phonecleaner.applock.LOCK_SCREEN_CANCELLED"

        const val PERSISTENT_NOTIFICATION_ID = 17
        private const val PERSISTENT_NOTIFICATION_DELETE_REQUEST_CODE = 17
        private const val PERSISTENT_NOTIFICATION_CONTENT_REQUEST_CODE = 1710
        private const val PERSISTENT_SHORTCUT_CLEAN_REQUEST_CODE = 1711
        private const val PERSISTENT_SHORTCUT_FILE_REQUEST_CODE = 1712
        private const val PERSISTENT_SHORTCUT_TOOLS_REQUEST_CODE = 1713
        private const val PERSISTENT_SHORTCUT_BATTERY_REQUEST_CODE = 1714
        private const val WAKE_LOCK_TIMEOUT_MS = 10L * 60L * 1000L
        private const val START_IN_FLIGHT_RESET_MS = 8_000L
        private const val PERSISTENT_NOTIFICATION_RESTORE_DELAY_MS = 1_000L
        private const val PERSISTENT_NOTIFICATION_CHECK_INTERVAL_MS = 60_000L
        private const val VARIANT_STORAGE_CLEANER = "storagecleaner"

        fun start(context: Context) {
            val appContext = context.applicationContext
            stopRequested.set(false)
            val intent = Intent(appContext, PersistentNotificationService::class.java).apply { action = ACTION_START }
            if (_isRunning.get()) {
                sendCommandBroadcast(appContext, ACTION_START)
                return
            }
            startForegroundCompat(appContext, intent)
        }

        fun enableMonitoring(context: Context) {
            val appContext = context.applicationContext
            stopRequested.set(false)
            val intent = Intent(appContext, PersistentNotificationService::class.java).apply { action = ACTION_ENABLE_MONITORING }
            if (_isRunning.get()) {
                sendCommandBroadcast(appContext, ACTION_ENABLE_MONITORING)
                return
            }
            startForegroundCompat(appContext, intent)
        }

        fun disableMonitoring(context: Context) {
            val appContext = context.applicationContext
            stopRequested.set(false)
            sendCommandBroadcast(appContext, ACTION_DISABLE_MONITORING)
        }

        fun stop(context: Context) {
            val appContext = context.applicationContext
            stopRequested.set(true)
            sendCommandBroadcast(appContext, ACTION_STOP_SERVICE)
            if (_isRunning.get()) {
                runCatching { appContext.stopService(Intent(appContext, PersistentNotificationService::class.java)) }
            }
        }

        fun setAppInForeground(inForeground: Boolean) {
            appInForeground.set(inForeground)
        }

        fun notifyAppBackground(context: Context) {
            val appContext = context.applicationContext
            appInForeground.set(false)
            sendCommandBroadcast(appContext, ACTION_APP_BACKGROUND)
        }

        private fun startForegroundCompat(appContext: Context, intent: Intent) {
            if (!startInFlight.compareAndSet(false, true)) return
            val started = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(appContext, intent)
                } else {
                    appContext.startService(intent)
                }
            }.isSuccess
            if (!started) {
                startInFlight.set(false)
                return
            }
            mainHandler.postDelayed({ if (!_isRunning.get()) startInFlight.set(false) }, START_IN_FLIGHT_RESET_MS)
        }

        private fun sendCommandBroadcast(appContext: Context, action: String) {
            runCatching { appContext.sendBroadcast(Intent(action).setPackage(appContext.packageName)) }
        }
    }
}
