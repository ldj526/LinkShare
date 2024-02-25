package com.example.linkshare.util

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface LocalSearchService {
    @GET("v1/search/local")
    fun searchLocal(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 5,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "random"
    ): Call<LocalSearchResponse>
}