package ec.edu.puce.lavozguamote

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LaVozGuamoteApp : Application() {

    companion object {
        const val RADIO_CHANNEL_ID = "radio_streaming_channel"
        const val RADIO_CHANNEL_NAME = "Radio Streaming"
        const val NOTIFICATIONS_CHANNEL_ID = "notifications_channel"
        const val NOTIFICATIONS_CHANNEL_NAME = "Notificaciones"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val radioChannel = NotificationChannel(
                RADIO_CHANNEL_ID,
                RADIO_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del streaming de radio"
                setShowBadge(false)
                enableVibration(false)
            }

            val notificationsChannel = NotificationChannel(
                NOTIFICATIONS_CHANNEL_ID,
                NOTIFICATIONS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de eventos, noticias y libros"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(radioChannel)
            notificationManager.createNotificationChannel(notificationsChannel)
        }
    }
}
