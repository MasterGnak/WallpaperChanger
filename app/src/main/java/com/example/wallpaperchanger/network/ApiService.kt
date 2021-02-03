package com.example.wallpaperchanger.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


private val moshi = Moshi.Builder()
    .add(WallpaperJsonAdapter())
    .add(KotlinJsonAdapterFactory())
    .build()

const val BASE_URL = "https://api.bing.microsoft.com/"
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ApiService {
    @Headers("Ocp-Apim-Subscription-Key:db6bc20543ef4a9f914fb7bfe39bd1eb")
    @GET("v7.0/images/search")
    suspend fun getImages(@Query("q") searchRequest: String,
                          @Query("size") size: String,
                          @Query("aspect") aspect: String): List<NetworkWallpaper>
}

object Api {
    val retrofitService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}