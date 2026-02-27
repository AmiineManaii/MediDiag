package com.example.musicplayer.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val deezerService: DeezerService by lazy {
        Retrofit.Builder()
            .baseUrl(DeezerService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeezerService::class.java)
    }
}
