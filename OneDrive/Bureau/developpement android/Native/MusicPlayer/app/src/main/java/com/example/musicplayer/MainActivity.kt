package com.example.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.example.musicplayer.ui.theme.MusicPlayerTheme
import com.example.musicplayer.ui.screens.*
import com.example.musicplayer.ui.components.*
import com.example.musicplayer.model.Song
import com.example.musicplayer.service.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Environment
import com.example.musicplayer.api.DeezerTrack

import java.io.File

import androidx.lifecycle.viewmodel.compose.viewModel

enum class Screen {
    HOME, SEARCH, QUEUE, PLAYLISTS, PLAYLIST_DETAIL
}

class MainActivity : ComponentActivity() {

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private lateinit var viewModel: MusicViewModel

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            viewModel.isPlaying.value = playing
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            val controller = mediaController ?: return
            val currentItem = controller.currentMediaItem ?: return
            val mediaId = currentItem.mediaId
            
            // On cherche d'abord dans les chansons locales
            var foundSong = viewModel.songs.find { it.id.toString() == mediaId }
            
            // Si non trouvé, on cherche dans les playlists
            if (foundSong == null) {
                for (playlist in viewModel.playlists) {
                    foundSong = playlist.songs.find { it.id.toString() == mediaId }
                    if (foundSong != null) break
                }
            }
            
            // Si toujours non trouvé, on cherche dans la file d'attente
            if (foundSong == null) {
                foundSong = viewModel.queueItems.find { it.id.toString() == mediaId }
            }

            if (foundSong != null) {
                viewModel.nowPlayingSong.value = foundSong
            } else {
                viewModel.nowPlayingSong.value = Song(
                    id = mediaId.toLongOrNull() ?: mediaId.hashCode().toLong(),
                    title = mediaMetadata.title?.toString() ?: "Inconnu",
                    artist = mediaMetadata.artist?.toString() ?: "Artiste inconnu",
                    uri = currentItem.localConfiguration?.uri ?: Uri.EMPTY,
                    albumArtUri = mediaMetadata.artworkUri
                )
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            viewModel.isShuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(mode: Int) {
            viewModel.repeatMode.value = mode
        }

        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
            // Quand une musique se termine ou on passe à la suivante
            updateQueueItems()
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            // Appelé quand la file d'attente change (ajout, suppression, déplacement)
            updateQueueItems()
        }
    }

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id != -1L) {
                Toast.makeText(this@MainActivity, "Téléchargement terminé", Toast.LENGTH_SHORT).show()
                viewModel.loadSongs(forceRefresh = true)
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[audioPermission] == true) {
            viewModel.loadSongs()
        } else {
            handlePermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionsAndLoadSongs()

        setContent {
            viewModel = viewModel()
            MusicPlayerTheme {
                PlayerStateUpdater()
                MainContent()
            }
        }
    }

    @Composable
    private fun PlayerStateUpdater() {
        LaunchedEffect(viewModel.isPlaying.value) {
            while (viewModel.isPlaying.value) {
                mediaController?.let {
                    viewModel.currentPosition.value = it.currentPosition
                    viewModel.duration.value = it.duration.coerceAtLeast(0L)
                }
                delay(100)
            }
        }

        LaunchedEffect(viewModel.nowPlayingSong.value) {
            mediaController?.let {
                viewModel.currentPosition.value = it.currentPosition
                viewModel.duration.value = it.duration.coerceAtLeast(0L)
            }
        }
    }

    @Composable
    private fun MainContent() {
        if (viewModel.showDetailScreen.value && viewModel.nowPlayingSong.value != null) {
            DetailScreenWrapper()
        } else {
            ScreenSelector()
        }

        if (viewModel.showPlaylistSelectionDialog.value) {
            PlaylistSelectionDialog(
                playlists = viewModel.playlists,
                onPlaylistSelected = { playlist ->
                    viewModel.songToAddToPlaylist.value?.let { song ->
                        viewModel.addSongToPlaylist(playlist, song)
                        Toast.makeText(this, "Ajouté à ${playlist.name}", Toast.LENGTH_SHORT).show()
                    }
                    viewModel.showPlaylistSelectionDialog.value = false
                },
                onCreateNewPlaylist = {
                    viewModel.showPlaylistSelectionDialog.value = false
                    viewModel.currentScreen.value = Screen.PLAYLISTS
                },
                onDismiss = { viewModel.showPlaylistSelectionDialog.value = false }
            )
        }
    }

    @Composable
    private fun DetailScreenWrapper() {
        val song = viewModel.nowPlayingSong.value!!
        MusicDetailScreen(
            song = song,
            isPlaying = viewModel.isPlaying.value,
            currentPosition = viewModel.currentPosition.value,
            duration = viewModel.duration.value,
            isShuffleEnabled = viewModel.isShuffleEnabled.value,
            repeatMode = viewModel.repeatMode.value,
            isDownloaded = viewModel.isSongDownloaded(song),
            onPlayPauseClick = { togglePlayback() },
            onPreviousClick = { mediaController?.seekToPrevious() },
            onNextClick = { mediaController?.seekToNext() },
            onSeekTo = { pos -> mediaController?.seekTo(pos) },
            onShuffleToggle = { toggleShuffle() },
            onRepeatModeChange = { cycleRepeatMode() },
            onBackClick = { viewModel.showDetailScreen.value = false },
            onDownloadClick = { s -> downloadSong(s) },
            queueItems = viewModel.queueItems,
            onRemoveFromQueue = { index ->
                mediaController?.removeMediaItem(index)
                updateQueueItems()
            },
            onPlayFromQueue = { index ->
                mediaController?.seekToDefaultPosition(index)
                mediaController?.play()
            }
        )
    }

    @Composable
    private fun ScreenSelector() {
        when (viewModel.currentScreen.value) {
            Screen.HOME -> HomeContent()
            Screen.SEARCH -> SearchContent()
            Screen.PLAYLISTS -> PlaylistsContent()
            Screen.PLAYLIST_DETAIL -> PlaylistDetailContent()
            else -> HomeContent()
        }
    }

    @Composable
    private fun HomeContent() {
        MusicListScreen(
            songs = viewModel.songs,
            nowPlayingSong = viewModel.nowPlayingSong.value,
            isPlaying = viewModel.isPlaying.value,
            isDownloaded = viewModel.isSongDownloaded(viewModel.nowPlayingSong.value),
            onSongClick = { index -> playLocalSong(index) },
            onPlayPauseClick = { togglePlayback() },
            onNowPlayingBarClick = { viewModel.showDetailScreen.value = true },
            onSearchClick = { viewModel.currentScreen.value = Screen.SEARCH },
            onDeleteSong = { song -> viewModel.deleteLocalSong(song) },
            onAddToQueue = { song -> addLocalSongToQueue(song) },
            onAddToPlaylist = { song ->
                viewModel.songToAddToPlaylist.value = song
                viewModel.showPlaylistSelectionDialog.value = true
            },
            onShowPlaylists = {
                viewModel.currentScreen.value = Screen.PLAYLISTS
            },
            onDownloadClick = { song -> downloadSong(song) }
        )
    }

    @Composable
    private fun SearchContent() {
        SearchScreen(
            onPlayTrack = { track -> playRemoteTrack(track) },
            onDownloadTrack = { track -> viewModel.downloadTrack(track) },
            onAddToQueue = { track -> addTrackToQueue(track) },
            onAddToPlaylist = { track ->
                val song = Song(
                    id = track.id,
                    title = track.title,
                    artist = track.artist.name,
                    uri = Uri.parse(track.preview),
                    albumArtUri = Uri.parse(track.album.coverMedium)
                )
                viewModel.songToAddToPlaylist.value = song
                viewModel.showPlaylistSelectionDialog.value = true
            },
            isDownloaded = { track ->
                viewModel.songs.any { it.title == track.title && it.artist == track.artist.name } || 
                viewModel.downloadingSongs.contains(track.id)
            },
            onBackClick = { viewModel.currentScreen.value = Screen.HOME }
        )
    }


    @Composable
    private fun PlaylistsContent() {
        PlaylistScreen(
            playlists = viewModel.playlists,
            onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
            onPlaylistClick = { playlist ->
                viewModel.selectedPlaylist.value = playlist
                viewModel.currentScreen.value = Screen.PLAYLIST_DETAIL
            },
            onDeletePlaylist = { playlist -> viewModel.deletePlaylist(playlist) },
            onBackClick = { viewModel.currentScreen.value = Screen.HOME }
        )
    }

    @Composable
    private fun PlaylistDetailContent() {
        viewModel.selectedPlaylist.value?.let { playlist ->
            PlaylistDetailScreen(
                playlist = playlist,
                nowPlayingSong = viewModel.nowPlayingSong.value,
                onSongClick = { index ->
                    playPlaylist(playlist, index)
                },
                onRemoveSong = { song ->
                    viewModel.removeSongFromPlaylist(playlist, song)
                },
                onAddToQueue = { song -> addLocalSongToQueue(song) },
                onBackClick = { viewModel.currentScreen.value = Screen.PLAYLISTS }
            )
        }
    }

    // Logic Methods
    private fun togglePlayback() {
        if (viewModel.isPlaying.value) mediaController?.pause()
        else mediaController?.play()
    }

    private fun toggleShuffle() {
        val newShuffle = !(mediaController?.shuffleModeEnabled ?: viewModel.isShuffleEnabled.value)
        mediaController?.shuffleModeEnabled = newShuffle
        val msg = if (newShuffle) "Lecture aléatoire activée" else "Lecture aléatoire désactivée"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun cycleRepeatMode() {
        val newMode = when (mediaController?.repeatMode ?: viewModel.repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
            else                   -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = newMode
        val msg = when (newMode) {
            Player.REPEAT_MODE_ALL -> "Répéter tout"
            Player.REPEAT_MODE_ONE -> "Répéter 1 titre"
            else                   -> "Répétition désactivée"
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun playLocalSong(index: Int) {
        mediaController?.let { controller ->
            val savedShuffle = controller.shuffleModeEnabled
            val savedRepeat = controller.repeatMode
            controller.setMediaItems(viewModel.songs.map { it.toMediaItem() }, index, 0L)
            controller.shuffleModeEnabled = savedShuffle
            controller.repeatMode = savedRepeat
            controller.prepare()
            controller.play()
            updateQueueItems()
        }
    }

    private fun addLocalSongToQueue(song: Song) {
        mediaController?.addMediaItem(song.toMediaItem())
        updateQueueItems()
        Toast.makeText(this, "Ajouté à la file d'attente", Toast.LENGTH_SHORT).show()
    }

    private fun playPlaylist(playlist: com.example.musicplayer.model.Playlist, startIndex: Int) {
        mediaController?.let { controller ->
            val savedShuffle = controller.shuffleModeEnabled
            val savedRepeat = controller.repeatMode
            controller.setMediaItems(playlist.songs.map { it.toMediaItem() }, startIndex, 0L)
            controller.shuffleModeEnabled = savedShuffle
            controller.repeatMode = savedRepeat
            controller.prepare()
            controller.play()
            updateQueueItems()
        }
    }

    private fun playRemoteTrack(track: DeezerTrack) {
        val song = Song(
            id = track.id,
            title = track.title,
            artist = track.artist.name,
            uri = Uri.parse(track.preview),
            albumArtUri = Uri.parse(track.album.coverMedium)
        )
        viewModel.nowPlayingSong.value = song
        mediaController?.setMediaItem(song.toMediaItem())
        mediaController?.prepare()
        mediaController?.play()
        updateQueueItems()
    }

    private fun addTrackToQueue(track: DeezerTrack) {
        val song = Song(
            id = track.id,
            title = track.title,
            artist = track.artist.name,
            uri = Uri.parse(track.preview),
            albumArtUri = Uri.parse(track.album.coverMedium)
        )
        mediaController?.addMediaItem(song.toMediaItem())
        updateQueueItems()
        Toast.makeText(this, "Ajouté à la file d'attente", Toast.LENGTH_SHORT).show()
    }

    private fun downloadSong(song: Song) {
        val track = DeezerTrack(
            id = song.id,
            title = song.title,
            preview = song.uri.toString(),
            artist = com.example.musicplayer.api.DeezerArtist(0, song.artist, ""),
            album = com.example.musicplayer.api.DeezerAlbum(0, "", song.albumArtUri.toString())
        )
        viewModel.downloadTrack(track)
    }

    private fun updateQueueItems() {
        mediaController?.let { controller ->
            viewModel.queueItems.clear()
            for (i in 0 until controller.mediaItemCount) {
                val item = controller.getMediaItemAt(i)
                viewModel.queueItems.add(Song(
                    id = item.mediaId.toLongOrNull() ?: 0L,
                    title = item.mediaMetadata.title?.toString() ?: "",
                    artist = item.mediaMetadata.artist?.toString() ?: "",
                    uri = item.localConfiguration?.uri ?: Uri.EMPTY,
                    albumArtUri = item.mediaMetadata.artworkUri
                ))
            }
        }
    }

    private fun checkPermissionsAndLoadSongs() {
        if (ContextCompat.checkSelfPermission(this, audioPermission) != PackageManager.PERMISSION_GRANTED) {
            val permissionsToRequest = mutableListOf(audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun handlePermissionDenied() {
        if (!shouldShowRequestPermissionRationale(audioPermission)) {
            Toast.makeText(this, "Permission refusée définitivement. Activez-la dans les paramètres.", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        } else {
            Toast.makeText(this, "L'accès aux fichiers est nécessaire pour charger la musique.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            downloadCompleteReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()
                mediaController?.addListener(playerListener)
                updateUiWithCurrentState()
                viewModel.loadSongs()
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(downloadCompleteReceiver)
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
    }

    private fun updateUiWithCurrentState() {
        mediaController?.let { controller ->
            viewModel.isPlaying.value = controller.isPlaying
            viewModel.isShuffleEnabled.value = controller.shuffleModeEnabled
            viewModel.repeatMode.value = controller.repeatMode
            viewModel.currentPosition.value = controller.currentPosition
            viewModel.duration.value = controller.duration.coerceAtLeast(0L)

            val currentItem = controller.currentMediaItem
            if (currentItem != null) {
                val mediaId = currentItem.mediaId
                
                // On cherche d'abord dans les chansons locales
                var foundSong = viewModel.songs.find { it.id.toString() == mediaId }
                
                // Si non trouvé, on cherche dans les playlists (pour les titres API)
                if (foundSong == null) {
                    for (playlist in viewModel.playlists) {
                        foundSong = playlist.songs.find { it.id.toString() == mediaId }
                        if (foundSong != null) break
                    }
                }
                
                // Si toujours non trouvé, on cherche dans la file d'attente actuelle du ViewModel
                if (foundSong == null) {
                    foundSong = viewModel.queueItems.find { it.id.toString() == mediaId }
                }

                if (foundSong != null) {
                    viewModel.nowPlayingSong.value = foundSong
                } else {
                    // Fallback si vraiment introuvable
                    viewModel.nowPlayingSong.value = Song(
                        id = mediaId.toLongOrNull() ?: 0L,
                        title = currentItem.mediaMetadata.title?.toString() ?: "Inconnu",
                        artist = currentItem.mediaMetadata.artist?.toString() ?: "Artiste inconnu",
                        uri = currentItem.localConfiguration?.uri ?: Uri.EMPTY,
                        albumArtUri = currentItem.mediaMetadata.artworkUri
                    )
                }
            }
        }
    }
}
