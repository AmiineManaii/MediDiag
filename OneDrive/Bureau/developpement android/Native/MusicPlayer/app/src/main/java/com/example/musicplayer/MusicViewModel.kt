package com.example.musicplayer

import android.app.Application
import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.api.DeezerTrack
import com.example.musicplayer.model.Song
import com.example.musicplayer.model.Playlist
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Type

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val songs = mutableStateListOf<Song>()
    val playlists = mutableStateListOf<Playlist>()
    val nowPlayingSong = mutableStateOf<Song?>(null)
    val isPlaying = mutableStateOf(false)
    val currentPosition = mutableStateOf(0L)
    val duration = mutableStateOf(0L)
    val isShuffleEnabled = mutableStateOf(false)
    val repeatMode = mutableStateOf(0) // Player.REPEAT_MODE_OFF
    val showDetailScreen = mutableStateOf(false)
    val currentScreen = mutableStateOf(Screen.HOME)
    val selectedPlaylist = mutableStateOf<Playlist?>(null)
    val songToAddToPlaylist = mutableStateOf<Song?>(null)
    val showPlaylistSelectionDialog = mutableStateOf(false)

    val queueItems = mutableStateListOf<Song>()
    val downloadingSongs = mutableStateListOf<Long>()

    private val contentResolver: ContentResolver = application.contentResolver
    private val sharedPreferences = application.getSharedPreferences("music_player_prefs", Context.MODE_PRIVATE)
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, object : JsonSerializer<Uri>, JsonDeserializer<Uri> {
            override fun serialize(src: Uri?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
                return JsonPrimitive(src?.toString() ?: "")
            }
            override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Uri {
                val uriString = json?.asString
                return if (uriString.isNullOrEmpty()) Uri.EMPTY else Uri.parse(uriString)
            }
        })
        .create()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        try {
            val json = sharedPreferences.getString("playlists", null)
            if (json != null) {
                val type = object : TypeToken<List<Playlist>>() {}.type
                val savedPlaylists: List<Playlist> = gson.fromJson(json, type)
                playlists.clear()
                playlists.addAll(savedPlaylists)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun savePlaylists() {
        try {
            val json = gson.toJson(playlists.toList())
            sharedPreferences.edit().putString("playlists", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(name = name)
        playlists.add(newPlaylist)
        savePlaylists()
    }

    fun deletePlaylist(playlist: Playlist) {
        playlists.remove(playlist)
        savePlaylists()
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        val index = playlists.indexOf(playlist)
        if (index != -1) {
            val updatedSongs = playlists[index].songs.toMutableList()
            if (!updatedSongs.any { it.id == song.id }) {
                updatedSongs.add(song)
                playlists[index] = playlists[index].copy(songs = updatedSongs)
                savePlaylists()
            }
        }
    }

    fun removeSongFromPlaylist(playlist: Playlist, song: Song) {
        val index = playlists.indexOf(playlist)
        if (index != -1) {
            val updatedSongs = playlists[index].songs.toMutableList()
            updatedSongs.removeIf { it.id == song.id }
            playlists[index] = playlists[index].copy(songs = updatedSongs)
            savePlaylists()
        }
    }

    fun loadSongs(forceRefresh: Boolean = false) {
        if (songs.isNotEmpty() && !forceRefresh) return

        viewModelScope.launch(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
            )
            val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("%/Download/%")

            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            val loadedSongs = mutableListOf<Song>()

            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    
                    var finalTitle = displayName.replace(".mp3", "", ignoreCase = true)
                    var finalArtist = artist ?: "Artiste inconnu"
                    
                    if (finalArtist == "<unknown>" || finalArtist.contains("unknown", ignoreCase = true)) {
                        if (displayName.contains(" - ")) {
                            val parts = displayName.split(" - ", limit = 2)
                            finalArtist = parts[0]
                            finalTitle = parts[1].replace(".mp3", "", ignoreCase = true)
                        }
                    }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )

                    var albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/media"), id
                    ).buildUpon().appendPath("albumart").build()

                    val audioFile = File(dataPath)
                    val imageFile = File(audioFile.parent, audioFile.nameWithoutExtension + ".jpg")
                    if (imageFile.exists()) {
                        albumArtUri = Uri.fromFile(imageFile)
                    }

                    loadedSongs.add(Song(id, finalTitle, finalArtist, contentUri, albumArtUri))
                }
            }
            withContext(Dispatchers.Main) {
                songs.clear()
                songs.addAll(loadedSongs)
            }
        }
    }

    fun downloadTrack(track: DeezerTrack) {
        if (!downloadingSongs.contains(track.id)) {
            downloadingSongs.add(track.id)
        }

        val baseFileName = "${track.artist.name} - ${track.title}".replace("/", "_").replace("\\", "_")
        val mp3FileName = "$baseFileName.mp3"
        val imgFileName = "$baseFileName.jpg"
        
        val downloadManager = getApplication<Application>().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val audioRequest = DownloadManager.Request(Uri.parse(track.preview))
            .setTitle(track.title)
            .setDescription(track.artist.name)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mp3FileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        downloadManager.enqueue(audioRequest)

        val imageRequest = DownloadManager.Request(Uri.parse(track.album.coverMedium))
            .setTitle("Cover: ${track.title}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imgFileName)
        downloadManager.enqueue(imageRequest)

        Toast.makeText(getApplication(), "Téléchargement de '${track.title}' commencé...", Toast.LENGTH_SHORT).show()
    }

    fun deleteLocalSong(song: Song) {
        try {
            contentResolver.delete(song.uri, null, null)
            songs.remove(song)
            Toast.makeText(getApplication(), "Musique supprimée", Toast.LENGTH_SHORT).show()
            loadSongs(forceRefresh = true)
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
        }
    }

    fun isSongDownloaded(song: Song?): Boolean {
        if (song == null) return false
        if (!song.uri.toString().startsWith("http")) return true
        
        val isLocal = songs.any { it.title == song.title && it.artist == song.artist }
        val isDownloading = downloadingSongs.contains(song.id)
        
        return isLocal || isDownloading
    }
}
