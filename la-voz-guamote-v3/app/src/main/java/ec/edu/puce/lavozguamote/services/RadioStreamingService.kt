package ec.edu.puce.lavozguamote.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import ec.edu.puce.lavozguamote.BuildConfig
import ec.edu.puce.lavozguamote.LaVozGuamoteApp
import ec.edu.puce.lavozguamote.R
import ec.edu.puce.lavozguamote.ui.MainActivity

class RadioStreamingService : Service() {

    private val binder = RadioBinder()
    private var exoPlayer: ExoPlayer? = null
    private var isPlaying = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_PLAY = "ec.edu.puce.lavozguamote.PLAY"
        private const val ACTION_PAUSE = "ec.edu.puce.lavozguamote.PAUSE"
        private const val ACTION_STOP = "ec.edu.puce.lavozguamote.STOP"
    }

    inner class RadioBinder : Binder() {
        fun getService(): RadioStreamingService = this@RadioStreamingService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            val mediaItem = MediaItem.fromUri(BuildConfig.STREAMING_URL)
            setMediaItem(mediaItem)
            prepare()

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    this@RadioStreamingService.isPlaying = playing
                    updateNotification()
                }
            })
        }
    }

    fun play() {
        exoPlayer?.let { player ->
            if (!player.isPlaying) {
                player.play()
                startForeground(NOTIFICATION_ID, createNotification(true))
            }
        }
    }

    fun pause() {
        exoPlayer?.pause()
        updateNotification()
    }

    fun stop() {
        exoPlayer?.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true

    private fun createNotification(isPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, RadioStreamingService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RadioStreamingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_radio_logo)

        return NotificationCompat.Builder(this, LaVozGuamoteApp.RADIO_CHANNEL_ID)
            .setContentTitle("La Voz de Guamote")
            .setContentText(if (isPlaying) "Reproduciendo en vivo" else "Pausado")
            .setSmallIcon(R.drawable.ic_radio)
            .setLargeIcon(largeIcon)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pausar" else "Reproducir",
                playPausePendingIntent
            )
            .addAction(R.drawable.ic_stop, "Detener", stopPendingIntent)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification(isPlaying)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}
