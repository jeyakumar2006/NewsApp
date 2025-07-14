package com.example.mynewsapp.Utils

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

object Constant {

     val apiKey = "82a4324b18674f289c01f4706be70889"

     fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

     fun isNetworkAvailable(context: Context): Boolean {
          val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
          val network = connectivityManager.activeNetwork ?: return false
          val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

          return when {
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.d("NetworkCheck", "Connected via Wi-Fi")
                    true
               }
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.d("NetworkCheck", "Connected via Mobile Data")
                    true
               }
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.d("NetworkCheck", "Connected via Ethernet")
                    true
               }
               else -> false
          }
     }





}