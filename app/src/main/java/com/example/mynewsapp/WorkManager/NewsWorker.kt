package com.example.mynewsapp.WorkManager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mynewsapp.MVVM.NewsResponse
import com.example.mynewsapp.MVVM.Repository
import com.example.mynewsapp.Utils.Constant.apiKey
import com.example.mynewsapp.Utils.Constant.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NewsWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val repository = Repository(context)

    override suspend fun doWork(): Result {
        return try {
            val country = "us"
            val apiKey = "82a4324b18674f289c01f4706be70889"

            val response = suspendCoroutine<NewsResponse?> { cont ->
                repository.getNewsData(country, apiKey) { newsResponse, _ ->
                    cont.resume(newsResponse)
                }
            }

            return if (response != null && response.status == "ok" && response.articles != null) {
                repository.clearArticles()
                repository.insertArticles(response.articles)
                Result.success()
            } else {
                Result.failure()
            }

        } catch (e: Exception) {
            Result.retry()
        }
    }


}


