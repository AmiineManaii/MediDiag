package com.example.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicplayer.model.Song
import com.example.musicplayer.ui.components.NowPlayingBar
import com.example.musicplayer.ui.components.SongList

import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search

@Composable
fun MusicListScreen(
    songs: List<Song>,
    nowPlayingSong: Song?,
    isPlaying: Boolean,
    isDownloaded: Boolean,
    onSongClick: (Int) -> Unit,
    onPlayPauseClick: () -> Unit,
    onNowPlayingBarClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDeleteSong: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowQueue: () -> Unit,
    onShowPlaylists: () -> Unit,
    onDownloadClick: (Song) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (nowPlayingSong != null) {
                NowPlayingBar(
                    song = nowPlayingSong,
                    onPlayPauseClick = onPlayPauseClick,
                    onBarClick = onNowPlayingBarClick,
                    onDownloadClick = onDownloadClick,
                    isPlaying = isPlaying,
                    isDownloaded = isDownloaded
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeHeader(
                nowPlayingSong = nowPlayingSong,
                onSearchClick = onSearchClick,
                onShowQueue = onShowQueue,
                onShowPlaylists = onShowPlaylists
            )
            
            SongList(
                songs = songs,
                nowPlayingSong = nowPlayingSong,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                onSongClick = onSongClick,
                onDeleteSong = onDeleteSong,
                onAddToQueue = onAddToQueue,
                onAddToPlaylist = onAddToPlaylist
            )
        }
    }
}

@Composable
private fun HomeHeader(
    nowPlayingSong: Song?,
    onSearchClick: () -> Unit,
    onShowQueue: () -> Unit,
    onShowPlaylists: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bonjour,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    text = "Ma Musique",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Row {
                IconButton(
                    onClick = onShowPlaylists,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistPlay,
                        contentDescription = "Playlists",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onShowQueue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "File d'attente",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Featured Card or Quick Action
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            ) {
                // Background Image with blur effect (optional, here just showing image if exists)
                if (nowPlayingSong?.uri != null) {
                    AsyncImage(
                        model = nowPlayingSong.albumArtUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.3f
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Song Image or Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (nowPlayingSong?.albumArtUri != null) {
                            AsyncImage(

                                model = nowPlayingSong.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = if (nowPlayingSong != null) "Reprendre : ${nowPlayingSong.title}" else "Reprendre la lecture",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = nowPlayingSong?.artist ?: "Votre playlist préférée vous attend",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
