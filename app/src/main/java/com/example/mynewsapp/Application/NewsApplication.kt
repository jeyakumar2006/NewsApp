package com.example.mynewsapp.Application

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mynewsapp.Utils.SharedPrefHelper
import com.example.mynewsapp.WorkManager.NewsWorker
import java.util.concurrent.TimeUnit


class NewsApplication :Application() {


    override fun onCreate() {
        super.onCreate()
        SharedPrefHelper.init(this)
        scheduleNewsSync()
    }


    private fun scheduleNewsSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<NewsWorker>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "news_sync_worker",
            ExistingPeriodicWorkPolicy.KEEP, // keeps the existing work if it's already scheduled
            request
        )
    }
}









