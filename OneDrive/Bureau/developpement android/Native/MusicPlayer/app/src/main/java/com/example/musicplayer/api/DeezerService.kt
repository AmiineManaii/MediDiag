package com.example.musicplayer.api

import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerService {
    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30
    ): DeezerResponse

    companion object {
        const val BASE_URL = "https://api.deezer.com/"
    }
}
