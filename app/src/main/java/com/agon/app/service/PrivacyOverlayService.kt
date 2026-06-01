package com.agon.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.agon.app.MainActivity
import com.agon.app.R
import com.agon.app.data.PrivacySettings
import com.agon.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Foreground Service that owns the system overlay window and keeps the privacy
 * filter alive above all apps. It observes [SettingsRepository] reactively so
 * any change in the UI (or QS tile) is reflected instantly with no polling —
 * keeping CPU/battery cost near-zero when idle.
 */
class PrivacyOverlayService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var windowManager: WindowManager
    private var overlayView: PrivacyOverlayView? = null
    private lateinit var repo: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        repo = SettingsRepository.get(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createChannel()
        startForeground(NOTIF_ID, buildNotification(enabled = true))
        addOverlay()
        observeSettings()
    }

    private fun observeSettings() {
        repo.settings.onEach { s ->
            overlayView?.update(s)
            notifyUpdate(s.enabled)
            if (!s.enabled) {
                stopSelfClean()
            }
        }.launchIn(scope)
    }

    private fun addOverlay() {
        if (overlayView != null) return
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.TOP or Gravity.START }

        val view = PrivacyOverlayView(this)
        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayView = null
    }

    private fun stopSelfClean() {
        removeOverlay()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") stopForeground(true)
        }
        stopSelf()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) }
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(enabled: Boolean): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val toggleIntent = PendingIntent.getBroadcast(
            this, 1,
            Intent(this, PrivacyActionReceiver::class.java).setAction(PrivacyActionReceiver.ACTION_TOGGLE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_privacy_tile)
            .setContentTitle("Private Screen")
            .setContentText(if (enabled) "Privacy filter active — protecting your screen" else "Privacy filter paused")
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(0, if (enabled) "Turn off" else "Turn on", toggleIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun notifyUpdate(enabled: Boolean) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(enabled))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "privacy_overlay_channel"
        private const val NOTIF_ID = 4711

        fun start(context: Context) {
            val intent = Intent(context, PrivacyOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PrivacyOverlayService::class.java))
        }
    }
}
