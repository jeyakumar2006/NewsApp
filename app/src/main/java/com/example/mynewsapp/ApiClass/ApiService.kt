package com.example.mynewsapp.ApiClass

import com.example.mynewsapp.MVVM.NewsResponse
import retrofit2.Call
import retrofit2.http.GET

import retrofit2.http.Query

interface ApiService {
    @GET("v2/top-headlines")
    fun getNews(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}
