package com.amaze.fileutilities.audio_player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.amaze.fileutilities.R
import com.amaze.fileutilities.audio_player.AudioPlayerService

abstract class AudioPlayerNotification {
    private var notifyMode = NOTIFY_MODE_BACKGROUND
    private var notificationManager: NotificationManagerCompat? = null
    lateinit var service: AudioPlayerService
    var stopped = false

    @Synchronized
    fun init(service: AudioPlayerService) {
        this.service = service
        notificationManager = NotificationManagerCompat.from(service.applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    abstract fun update()

    @Synchronized
    fun stop() {
        stopped = true
        service.stopForeground(true)
        notificationManager!!.cancel(NOTIFICATION_ID)
    }

    fun updateNotifyModeAndPostNotification(notification: Notification?) {
        val newNotifyMode: Int = if (service.isPlaying()) {
            NOTIFY_MODE_FOREGROUND
        } else {
            NOTIFY_MODE_BACKGROUND
        }
        if (notifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            service.stopForeground(false)
        }
        if (newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            service.startForeground(NOTIFICATION_ID, notification)
        } else if (newNotifyMode == NOTIFY_MODE_BACKGROUND && notification != null) {
            notificationManager!!.notify(NOTIFICATION_ID, notification)
        }
        notifyMode = newNotifyMode
    }

    @RequiresApi(26)
    private fun createNotificationChannel() {
        var notificationChannel = notificationManager!!.getNotificationChannel(
            NOTIFICATION_CHANNEL_ID
        )
        if (notificationChannel == null) {
            notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                service.getString(R.string.amaze_audio_player),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.description =
                service.getString(R.string.audio_player_service_description)
            notificationChannel.enableLights(false)
            notificationChannel.enableVibration(false)
            notificationManager!!.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "playing_notification"
        private const val NOTIFY_MODE_FOREGROUND = 1
        private const val NOTIFY_MODE_BACKGROUND = 0
    }
}
