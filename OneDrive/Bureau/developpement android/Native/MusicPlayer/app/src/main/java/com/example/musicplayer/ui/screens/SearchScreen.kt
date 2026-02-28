package com.example.musicplayer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicplayer.api.DeezerTrack
import com.example.musicplayer.api.RetrofitInstance
import kotlinx.coroutines.launch

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onPlayTrack: (DeezerTrack) -> Unit,
    onDownloadTrack: (DeezerTrack) -> Unit,
    onAddToQueue: (DeezerTrack) -> Unit,
    onAddToPlaylist: (DeezerTrack) -> Unit,
    isDownloaded: (DeezerTrack) -> Boolean,
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<DeezerTrack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rechercher des musiques") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Retour", modifier = Modifier.size(24.dp))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Rechercher sur Jamendo...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    scope.launch {
                        isLoading = true
                        try {
                            val response = RetrofitInstance.deezerService.searchTracks(query = searchQuery)
                            searchResults = response.data
                            if (searchResults.isEmpty()) {
                                android.widget.Toast.makeText(context, "Aucun résultat trouvé", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.widget.Toast.makeText(context, "Erreur de connexion : ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }),
                trailingIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val response = RetrofitInstance.deezerService.searchTracks(query = searchQuery)
                                searchResults = response.data
                                if (searchResults.isEmpty()) {
                                    android.widget.Toast.makeText(context, "Aucun résultat trouvé", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, "Erreur de connexion : ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Rechercher")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { track ->
                        TrackItem(
                            track = track,
                            isDownloaded = isDownloaded(track),
                            onPlay = { onPlayTrack(track) },
                            onDownload = { onDownloadTrack(track) },
                            onAddToQueue = { onAddToQueue(track) },
                            onAddToPlaylist = { onAddToPlaylist(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrackItem(
    track: DeezerTrack,
    isDownloaded: Boolean,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.album.coverMedium,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(track.artist.name, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
            if (!isDownloaded) {
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = "Télécharger")
                }
            }
            IconButton(onClick = onAddToQueue) {
                Icon(Icons.Default.QueueMusic, contentDescription = "Ajouter à la file")
            }
            IconButton(onClick = onAddToPlaylist) {
                Icon(Icons.Default.PlaylistAdd, contentDescription = "Ajouter à une playlist")
            }
        }
    }
}
