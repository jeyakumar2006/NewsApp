package com.example.mynewsapp.NewDetailActivity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.mynewsapp.MVVM.Article
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.ActivityNewsDetailsBinding

class NewsDetailsActivity : AppCompatActivity() {

private lateinit var activityNewsDetailsBinding: ActivityNewsDetailsBinding

    private val article by lazy { intent.getParcelableExtra<Article>("article") }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        activityNewsDetailsBinding = ActivityNewsDetailsBinding.inflate(layoutInflater)
        setContentView(activityNewsDetailsBinding.root)

        activityNewsDetailsBinding.toolbar.backBtn.setOnClickListener {
            finish()
        }
        activityNewsDetailsBinding.toolbar.title.text = "Detail Page"
        Log.e("TAG", "onCreate::::::"+ article )

        article?.let {
            updateUI(it)
        } ?: run {

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }

    private fun updateUI( article: Article){

        activityNewsDetailsBinding.newsDescription.text = article.description?:""
        activityNewsDetailsBinding.newsTitle.text = article.title?:""
        activityNewsDetailsBinding.publistedAt.text = article.publishedAt?:""

        Glide.with(this)
            .load(article.urlToImage)
            .placeholder(R.drawable.ic_launcher_background) // Optional
            .error(R.drawable.ic_launcher_background) // Optional
            .into(activityNewsDetailsBinding.newsImage)

    }
}