package com.smartalerter.notification.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.smartalerter.notification.ui.screens.ActiveAlertActivity

class AlarmForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initVibrator()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_DISMISS_ALARM) {
            stopAlarm()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (action == ACTION_START_ALARM) {
            val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Smart Alerter"
            val title = intent.getStringExtra(EXTRA_TITLE) ?: "High Priority Alert"
            val body = intent.getStringExtra(EXTRA_BODY) ?: ""
            val soundUriStr = intent.getStringExtra(EXTRA_SOUND_URI)
            val vibrationPattern = intent.getStringExtra(EXTRA_VIBRATION_PATTERN) ?: "SOS"
            val bypassDnd = intent.getBooleanExtra(EXTRA_BYPASS_DND, true)
            val volume = intent.getIntExtra(EXTRA_VOLUME, 100)

            // 1. Post Foreground Notification with Full Screen Intent
            val notification = buildForegroundNotification(appName, title, body)
            startForeground(NOTIFICATION_ID, notification)

            // 2. Play Looping Audio on Alarm Stream
            playAlarmSound(soundUriStr, bypassDnd, volume)

            // 3. Start Vibration
            startVibrating(vibrationPattern)
        }

        return START_NOT_STICKY
    }

    private fun playAlarmSound(soundUriStr: String?, bypassDnd: Boolean, volumePercent: Int) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            if (bypassDnd) {
                val maxAlarmVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val targetVol = ((volumePercent / 100f) * maxAlarmVol).toInt().coerceAtLeast(1)
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVol, 0)
            }

            val soundUri = if (!soundUriStr.isNullOrEmpty()) {
                Uri.parse(soundUriStr)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(applicationContext, soundUri)
                isLooping = true
                prepare()
                start()
            }
            Log.i(TAG, "Emergency Ringtone started playing continuously.")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio, falling back to default alarm ringtone", e)
            playFallbackTone()
        }
    }

    private fun playFallbackTone() {
        try {
            val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(applicationContext, fallbackUri)?.apply {
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal audio fallback failure", e)
        }
    }

    private fun startVibrating(patternName: String) {
        val pattern = when (patternName) {
            "CONTINUOUS" -> longArrayOf(0, 1000, 200)
            "RAPID_PULSE" -> longArrayOf(0, 150, 100, 150, 400)
            "HEARTBEAT" -> longArrayOf(0, 200, 150, 350, 600)
            "SOS" -> longArrayOf(0, 100, 100, 100, 100, 100, 200, 300, 100, 300, 100, 300, 200, 100, 100, 100, 100, 100, 500)
            else -> null
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, 0)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun buildForegroundNotification(appName: String, title: String, body: String): Notification {
        val fullScreenIntent = Intent(this, ActiveAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_APP_NAME, appName)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmForegroundService::class.java).apply {
            action = ACTION_DISMISS_ALARM
        }
        val dismissPendingIntent = PendingIntent.getService(
            this, 1, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🚨 EMERGENCY ALERT: $appName")
            .setContentText("$title - $body")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Silence Ringtone", dismissPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Smart Emergency Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Continuous emergency audio and vibration alerts for matched notifications"
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun initVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SmartAlerter::AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun stopAlarm() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            vibrator?.cancel()
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up alarm resources", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    companion object {
        const val CHANNEL_ID = "emergency_alarm_channel"
        const val NOTIFICATION_ID = 9001

        const val ACTION_START_ALARM = "com.smartalerter.action.START_ALARM"
        const val ACTION_DISMISS_ALARM = "com.smartalerter.action.DISMISS_ALARM"

        const val EXTRA_RULE_ID = "extra_rule_id"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_SOUND_URI = "extra_sound_uri"
        const val EXTRA_VIBRATION_PATTERN = "extra_vibration_pattern"
        const val EXTRA_BYPASS_DND = "extra_bypass_dnd"
        const val EXTRA_VOLUME = "extra_volume"
        private const val TAG = "AlarmForegroundService"
    }
}