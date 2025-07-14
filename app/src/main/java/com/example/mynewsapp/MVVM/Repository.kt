package com.example.mynewsapp.MVVM

import android.content.Context
import android.util.Log
import com.example.mynewsapp.ApiClass.ApiClient
import com.example.mynewsapp.RoomDatabase.AppDatabase
import com.example.mynewsapp.RoomDatabase.ArticleEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Repository( context: Context) {
    private val articleDao = AppDatabase.getDatabase(context).articleDao()

    fun getNewsData(
        country: String,
        apiKey: String,
        onResult: (NewsResponse?, String?) -> Unit
    ) {
        try {
            val call = ApiClient.apiService.getNews(country, apiKey)
            call.enqueue(object : Callback<NewsResponse> {
                override fun onResponse(
                    call: Call<NewsResponse>,
                    response: Response<NewsResponse>
                ) {
                    if (response.isSuccessful) {
                        val newsResponse = response.body()
                        newsResponse?.articles?.let { articles ->

                            Log.e("TAG", "onResponse::::11"+ newsResponse.articles )
                            CoroutineScope(Dispatchers.IO).launch {
                                clearArticles()
                                insertArticles(articles)
                            }
                        }
                        Log.e("TAG", "onResponse:::::222" )
                        onResult(newsResponse, null)
                    } else {
                        val errorResponse = try {
                            Gson().fromJson(
                                response.errorBody()?.charStream(),
                                NewsResponse::class.java
                            )
                        } catch (e: Exception) {
                            null
                        }
                        onResult(errorResponse, errorResponse?.message ?: "Unknown error")
                    }
                }

                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                    onResult(null, "Network error: ${t.message}")
                }
            })
        } catch (e: Exception) {
            onResult(null, "Unexpected error: ${e.message}")
        }
    }

    suspend fun clearArticles() {
        articleDao.clearArticles()
    }

    suspend fun insertArticles(articles: List<Article>) {
        val entities = articles.map {
            ArticleEntity(
                author = it.author,
                title = it.title,
                description = it.description,
                url = it.url,
                urlToImage = it.urlToImage,
                publishedAt = it.publishedAt,
                content = it.content,
                sourceName = it.source?.name
            )
        }
        articleDao.insertArticles(entities)
    }

    suspend fun getArticlesFromDb(): List<ArticleEntity> {
        return articleDao.getAllArticles()
    }
}



















//class Repository (private val context: Context){
//
//    fun getNewsData(
//        country: String,
//        apiKey: String,
//        onResult: (NewsResponse?, String?) -> Unit
//    ) {
//        try {
//            val call = ApiClient.apiService.getNews(country, apiKey)
//            call.enqueue(object : Callback<NewsResponse> {
//                override fun onResponse(
//                    call: Call<NewsResponse>,
//                    response: Response<NewsResponse>
//                ) {
//                    try {
//                        if (response.isSuccessful) {
//                            val newsResponse = response.body()
//                            onResult(newsResponse, null)
//                        } else {
//                            val errorResponse = Gson().fromJson(
//                                response.errorBody()?.charStream(),
//                                NewsResponse::class.java
//                            )
//                            onResult(errorResponse, errorResponse?.message ?: "Unknown error")
//                        }
//
//                    } catch (e: Exception) {
//                        onResult(null, "Parsing error: ${e.message}")
//                    }
//                }
//
//                override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
//                    onResult(null, "Network error: ${t.message}")
//                }
//            })
//        } catch (e: Exception) {
//            onResult(null, "Unexpected error: ${e.message}")
//        }
//    }
//}
