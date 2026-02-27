package com.example.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import com.example.musicplayer.ui.screens.MusicListScreen
import com.example.musicplayer.ui.screens.MusicDetailScreen
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity2 : ComponentActivity() {
/*
    private var musicService: MusicService2? = null
    private var isBound = false
    private var player: Player? = null

    private val songs = mutableStateListOf<Song>()
    private val nowPlayingSong = mutableStateOf<Song?>(null)
    private val isPlaying = mutableStateOf(false)
    private val currentPosition = mutableStateOf(0L)
    private val duration = mutableStateOf(0L)
    private val isShuffleEnabled = mutableStateOf(false)
    private val repeatMode = mutableStateOf(Player.REPEAT_MODE_OFF)
    private val showDetailScreen = mutableStateOf(false)

    private val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService2.LocalBinder
            musicService = binder.getService()
            player = musicService?.getPlayer()
            isBound = true

            player?.addListener(playerListener)
            updateUiWithCurrentState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            player?.removeListener(playerListener)
            musicService = null
            player = null
            isBound = false
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying.value = playing
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val mediaId = mediaItem?.mediaId ?: return
            nowPlayingSong.value = songs.find { it.id.toString() == mediaId }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            isShuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(mode: Int) {
            repeatMode.value = mode
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[audioPermission] == true) {
            loadSongs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs()
        } else {
            requestPermissionsLauncher.launch(arrayOf(audioPermission))
        }

        setContent {
            MusicPlayerTheme {
                LaunchedEffect(isPlaying.value) {
                    while (isPlaying.value) {
                        player?.let {
                            currentPosition.value = it.currentPosition
                            duration.value = it.duration.coerceAtLeast(0L)
                        }
                        delay(500)
                    }
                }

                if (showDetailScreen.value && nowPlayingSong.value != null) {
                    MusicDetailScreen(
                        song = nowPlayingSong.value!!,
                        isPlaying = isPlaying.value,
                        currentPosition = currentPosition.value,
                        duration = duration.value,
                        isShuffleEnabled = isShuffleEnabled.value,
                        repeatMode = repeatMode.value,
                        onPlayPauseClick = { if (isPlaying.value) player?.pause() else player?.play() },
                        onPreviousClick = { player?.seekToPrevious() },
                        onNextClick = { player?.seekToNext() },
                        onSeekTo = { pos -> player?.seekTo(pos) },
                        onShuffleToggle = { player?.shuffleModeEnabled = !isShuffleEnabled.value },
                        onRepeatModeChange = {
                            val next = when(repeatMode.value) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                                else -> Player.REPEAT_MODE_OFF
                            }
                            player?.repeatMode = next
                        },
                        onBackClick = { showDetailScreen.value = false }
                    )
                } else {
                    MusicListScreen(
                        songs = songs,
                        nowPlayingSong = nowPlayingSong.value,
                        isPlaying = isPlaying.value,
                        onSongClick = { index ->
                            player?.let { p ->
                                p.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
                                p.prepare()
                                p.play()
                            }
                        },
                        onPlayPauseClick = { if (isPlaying.value) player?.pause() else player?.play() },
                        onNowPlayingBarClick = { showDetailScreen.value = true }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService2::class.java)
        ContextCompat.startForegroundService(this, intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun updateUiWithCurrentState() {
        player?.let { p ->
            isPlaying.value = p.isPlaying
            isShuffleEnabled.value = p.shuffleModeEnabled
            repeatMode.value = p.repeatMode
            currentPosition.value = p.currentPosition
            duration.value = p.duration.coerceAtLeast(0L)
            val currentId = p.currentMediaItem?.mediaId
            nowPlayingSong.value = songs.find { it.id.toString() == currentId }
        }
    }

    private fun loadSongs() {
        lifecycleScope.launch(Dispatchers.IO) {
            val loadedSongs = mutableListOf<Song>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
            )
            val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("%/Download/%")
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    loadedSongs.add(Song(id, cursor.getString(titleCol), cursor.getString(artistCol), uri, null))
                }
            }
            withContext(Dispatchers.Main) {
                songs.clear()
                songs.addAll(loadedSongs)
            }
        }
    }*/
}
