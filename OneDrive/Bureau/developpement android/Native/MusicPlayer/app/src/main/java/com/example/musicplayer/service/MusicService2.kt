package com.example.musicplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.MainActivity2

class MusicService2 : Service() {

    private var player: ExoPlayer? = null
    private val binder = LocalBinder()

    companion object {
        const val ACTION_PREVIOUS = "com.example.musicplayer.PREVIOUS"
        const val ACTION_PLAY_PAUSE = "com.example.musicplayer.PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.musicplayer.NEXT"
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService2 = this@MusicService2
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updateNotification()
                }
                override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                    updateNotification()
                }
            })
        }
        startForeground(1, createNotification())
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PREVIOUS -> player?.seekToPrevious()
            ACTION_PLAY_PAUSE -> {
                if (player?.isPlaying == true) player?.pause() else player?.play()
            }
            ACTION_NEXT -> player?.seekToNext()
        }
        // START_STICKY dit au système de recréer le service s'il est tué par manque de mémoire
        return START_REDELIVER_INTENT
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Cette méthode est appelée quand on "swipe" l'appli ou "sweep all"
        // Si la musique joue, on ne fait rien pour que le service survive au swipe.
        // Si elle est en pause, on peut arrêter le service pour libérer la mémoire.

        super.onTaskRemoved(rootIntent)
    }

    fun getPlayer(): ExoPlayer? = player

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, createNotification())
    }

    private fun createNotification(): Notification {
        val channelId = "music_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Music Playback", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Open MainActivity2 on click
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity2::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        // Playback Actions
        val prevIntent = PendingIntent.getService(this, 1, Intent(this, MusicService2::class.java).apply { action = ACTION_PREVIOUS }, PendingIntent.FLAG_IMMUTABLE)
        val playPauseIntent = PendingIntent.getService(this, 2, Intent(this, MusicService2::class.java).apply { action = ACTION_PLAY_PAUSE }, PendingIntent.FLAG_IMMUTABLE)
        val nextIntent = PendingIntent.getService(this, 3, Intent(this, MusicService2::class.java).apply { action = ACTION_NEXT }, PendingIntent.FLAG_IMMUTABLE)

        val playPauseIcon = if (player?.isPlaying == true) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val currentTitle = player?.currentMediaItem?.mediaMetadata?.title ?: "Music Player"
        val currentArtist = player?.currentMediaItem?.mediaMetadata?.artist ?: "Service actif"

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(currentTitle.toString())
            .setContentText(currentArtist.toString())
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Précédent", prevIntent)
            .addAction(playPauseIcon, "Play/Pause", playPauseIntent)
            .addAction(android.R.drawable.ic_media_next, "Suivant", nextIntent)
            .setStyle(MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .build()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
