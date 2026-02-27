package com.example.musicplayer.api

import com.google.gson.annotations.SerializedName

data class DeezerResponse(
    @SerializedName("data") val data: List<DeezerTrack>
)

data class DeezerTrack(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("preview") val preview: String,
    @SerializedName("artist") val artist: DeezerArtist,
    @SerializedName("album") val album: DeezerAlbum
)

data class DeezerArtist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("picture_medium") val pictureMedium: String
)

data class DeezerAlbum(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("cover_medium") val coverMedium: String
)
