package com.example.mynewsapp.MVVM

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynewsapp.RoomDatabase.ArticleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = Repository(application)

    private val _newsResponseLiveData = MutableLiveData<NewsResponse>()
    val newsResponseLiveData: LiveData<NewsResponse> = _newsResponseLiveData

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData

    private val _articlesLiveData = MutableLiveData<List<ArticleEntity>>()
    val articlesLiveData: LiveData<List<ArticleEntity>> = _articlesLiveData

    @SuppressLint("NullSafeMutableLiveData")
    fun getNewsData(country: String, apiKey: String) {
        repository.getNewsData(country, apiKey) { newsResponse, error ->

            if (newsResponse != null && newsResponse.status == "ok" && newsResponse.articles != null) {
                _newsResponseLiveData.postValue(newsResponse)

                // Save to Room DB
                viewModelScope.launch(Dispatchers.IO) {
                    repository.insertArticles(newsResponse.articles)
                }

            } else {
                _errorLiveData.postValue(newsResponse?.message ?: error ?: "Unexpected error!")
            }
        }
    }

    fun loadArticlesFromDb() {

        viewModelScope.launch(Dispatchers.IO) {
            val articles = repository.getArticlesFromDb()
            Log.e("TAG", "loadArticlesFromDb::::mainvieModel"+ articles )
            _articlesLiveData.postValue(articles)
        }
    }
}






//class MainViewModel : ViewModel() {
//
//    private val repository = Repository()
//
//    private val _newsResponseLiveData = MutableLiveData<NewsResponse>()
//    val newsResponseLiveData: LiveData<NewsResponse> = _newsResponseLiveData
//
//    private val _errorLiveData = MutableLiveData<String>()
//    val errorLiveData: LiveData<String> = _errorLiveData
//
//    fun getNewsData(country: String, apiKey: String) {
//        repository.getNewsData(country, apiKey) { newsResponse, error ->
//            Log.e(TAG, "getNewsData::::newsResponse:::::::: $newsResponse" )
//            if (newsResponse != null) {
//                if (newsResponse.status == "ok" && newsResponse.articles != null) {
//                    _newsResponseLiveData.postValue(newsResponse)
//                } else {
//                    _errorLiveData.postValue(newsResponse.message ?: "Unexpected error!")
//                }
//
//            } else {
//                _errorLiveData.postValue(error)
//            }
//        }
//    }
//}
