package com.example.mynewsapp.SplashActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mynewsapp.R
import android.widget.MediaController
import com.example.mynewsapp.LoginActivity.LoginActivity
import com.example.mynewsapp.MainActivity.MainActivity
import com.example.mynewsapp.Utils.SharedPrefHelper
import com.example.mynewsapp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    lateinit var splashBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashBinding =  ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.splash}")
        splashBinding.videoplayer.setVideoURI(videoUri)

        splashBinding.videoplayer.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.start()
        }

        splashBinding.videoplayer.setOnCompletionListener { mediaPlayer ->
            navigateToActivity()
            Log.e("TAG", "onCreate::::::::::::finished::::::::: " )

        }

    }


    fun navigateToActivity(){
        val targetActivity = if (SharedPrefHelper.getBoolean("IsLoggedin",false)){
            MainActivity::class.java
        }else{
            LoginActivity::class.java
        }
        startActivity(Intent(this, targetActivity))
        finish()
    }



}