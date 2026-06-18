package com.quickcleanpro.phonecleaner.data.source.applock

import android.animation.ObjectAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.quickcleanpro.phonecleaner.R
import com.quickcleanpro.phonecleaner.data.repository.AppLockRepositoryImpl
import com.quickcleanpro.phonecleaner.data.source.notification.PersistentNotificationService
import com.quickcleanpro.phonecleaner.utils.AppLockPermissionUtils
import com.quickcleanpro.phonecleaner.utils.AppPrefsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockScreenOverlayService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var currentPin = ""
    private var targetPackage = ""
    private var failedAttempts = 0
    private var lockedUntilMillis = 0L

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        targetPackage = intent?.getStringExtra(EXTRA_TARGET_PACKAGE).orEmpty()
        if (targetPackage.isBlank() || !AppLockPermissionUtils.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        showOverlay()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        removeOverlay()
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlayView != null) return
        val view = LayoutInflater.from(this).inflate(R.layout.layout_lock_screen_pin, null)
        view.systemUiVisibility =
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        overlayView = view
        view.findViewById<TextView>(R.id.tv_hint).text = getString(R.string.enter_pin_to_use)
        view.findViewById<TextView>(R.id.tv_error_hint).visibility = View.INVISIBLE
        bindAppInfo(view)
        bindKeypad(view)
        val params =
            WindowManager
                .LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    },
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.TRANSLUCENT,
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }
        runCatching {
            windowManager.addView(view, params)
        }.onFailure {
            overlayView = null
            stopSelf()
        }
    }

    private fun bindAppInfo(view: View) {
        serviceScope.launch {
            val appInfo = withContext(Dispatchers.IO) { loadAppInfo(targetPackage) }
            view.findViewById<TextView>(R.id.tv_app_name).text = appInfo.name
            appInfo.icon?.let { view.findViewById<ImageView>(R.id.iv_app_icon).setImageDrawable(it) }
        }
    }

    private fun bindKeypad(view: View) {
        val digitButtons =
            listOf(
                R.id.btn_1 to '1',
                R.id.btn_2 to '2',
                R.id.btn_3 to '3',
                R.id.btn_4 to '4',
                R.id.btn_5 to '5',
                R.id.btn_6 to '6',
                R.id.btn_7 to '7',
                R.id.btn_8 to '8',
                R.id.btn_9 to '9',
                R.id.btn_0 to '0',
            )
        digitButtons.forEach { (id, digit) ->
            view.findViewById<View>(id).setOnClickListener { onDigit(digit) }
        }
        view.findViewById<View>(R.id.btn_delete).setOnClickListener {
            currentPin = currentPin.dropLast(1)
            updatePinDots()
        }
        view.findViewById<TextView>(R.id.tv_cancel).setOnClickListener {
            sendLockBroadcast(PersistentNotificationService.ACTION_LOCK_SCREEN_CANCELLED)
            goHome()
            stopSelf()
        }
    }

    private fun onDigit(digit: Char) {
        if (System.currentTimeMillis() < lockedUntilMillis) {
            val remaining = ((lockedUntilMillis - System.currentTimeMillis()) / 1000L).coerceAtLeast(1L)
            Toast.makeText(this, "${getString(R.string.too_many_attempts)} ${remaining}s", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentPin.length >= AppLockRepositoryImpl.PIN_LENGTH) return
        if (isVibrationEnabled()) vibrate()
        currentPin += digit
        updatePinDots()
        if (currentPin.length == AppLockRepositoryImpl.PIN_LENGTH) {
            verifyPin()
        }
    }

    private fun verifyPin() {
        if (currentPin == AppPrefsUtils.getString(AppLockRepositoryImpl.KEY_PIN, "")) {
            sendLockBroadcast(PersistentNotificationService.ACTION_PASSWORD_SUCCESS)
            stopSelf()
        } else {
            failedAttempts += 1
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                failedAttempts = 0
                lockedUntilMillis = System.currentTimeMillis() + FAILED_LOCKOUT_MS
                Toast.makeText(this, getString(R.string.wait_seconds), Toast.LENGTH_LONG).show()
            } else {
                showErrorHint()
                if (isVibrationEnabled()) vibrate()
            }
            currentPin = ""
            updatePinDots()
        }
    }

    private fun isVibrationEnabled(): Boolean = AppPrefsUtils.getBoolean(AppLockRepositoryImpl.KEY_VIBRATE_ON_KEYPAD, true)

    private fun updatePinDots() {
        val view = overlayView ?: return
        listOf(R.id.circle1, R.id.circle2, R.id.circle3, R.id.circle4).forEachIndexed { index, id ->
            view.findViewById<View>(id).setBackgroundResource(
                if (index < currentPin.length) R.drawable.pin_dot_selected else R.drawable.pin_dot_unselected,
            )
        }
    }

    private fun showErrorHint() {
        overlayView?.findViewById<TextView>(R.id.tv_error_hint)?.let { errorView ->
            errorView.visibility = View.VISIBLE
            ObjectAnimator.ofFloat(errorView, "translationX", 0f, 20f, -20f, 20f, -20f, 0f).apply {
                duration = 400L
                interpolator = OvershootInterpolator()
                start()
            }
        }
    }

    private fun sendLockBroadcast(action: String) {
        sendBroadcast(Intent(action).setPackage(packageName))
    }

    private fun goHome() {
        runCatching {
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )
        }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        runCatching { windowManager.removeView(view) }
        overlayView = null
    }

    private fun loadAppInfo(packageName: String): TargetAppInfo {
        val packageManager = packageManager
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            TargetAppInfo(
                name = packageManager.getApplicationLabel(info).toString(),
                icon = packageManager.getApplicationIcon(info.packageName),
            )
        }.getOrElse {
            TargetAppInfo(
                name = packageName.substringAfterLast('.').ifBlank { getString(R.string.app_lock) },
                icon = null,
            )
        }
    }

    private fun vibrate() {
        val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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

    private data class TargetAppInfo(
        val name: String,
        val icon: android.graphics.drawable.Drawable?,
    )

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val FAILED_LOCKOUT_MS = 30_000L
    }
}
