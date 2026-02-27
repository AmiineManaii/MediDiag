package com.example.musicplayer.model

import android.net.Uri

data class Playlist(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val songs: List<Song> = emptyList()
)
