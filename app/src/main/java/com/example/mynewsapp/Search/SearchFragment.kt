package com.example.mynewsapp.Search

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynewsapp.MainViewModelFactory.MainViewModelFactory
import com.example.mynewsapp.R
import com.example.mynewsapp.Utils.Constant.apiKey
import com.example.mynewsapp.Utils.Constant.isNetworkAvailable
import com.example.mynewsapp.databinding.FragmentSearchBinding
import com.example.mynewsapp.MVVM.Article
import com.example.mynewsapp.MVVM.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private var _searchBinding: FragmentSearchBinding? = null
    private val searchBinding get() = _searchBinding!!

    private lateinit var viewModel: MainViewModel
    private var articles = mutableListOf<Article>()

    lateinit var searchAdapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _searchBinding = FragmentSearchBinding.inflate(inflater,container,false)

        return searchBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBinding.searchcyclerview.layoutManager = GridLayoutManager(requireContext(),3, RecyclerView.VERTICAL,false)
        searchAdapter = SearchAdapter(null)
        searchBinding.searchcyclerview.adapter = searchAdapter

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupObservers()

        if (isNetworkAvailable(requireContext())) {
            Log.e(TAG, "onViewCreated::++++:1111")
            viewModel.getNewsData("us", apiKey)
        } else {
            Log.e(TAG, "onViewCreated:+++::2222")
            viewModel.loadArticlesFromDb()

            Log.e(TAG, "onViewCreated::::" + viewModel.loadArticlesFromDb())
        }

        searchBinding.backbtn.setOnClickListener {
            hideKeyboard()
            findNavController().navigate(R.id.homeFragment)
        }


    }


    private fun setupSearchListener(){
        searchBinding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()

                if(!query.isNullOrEmpty()){
                    Log.e("TAG", "afterTextChanged:::::::::::1:::::: " )
                    val filteredList = articles.filter { article ->
                        article.source?.name?.contains(query, ignoreCase = true) == true
                    }
                    Log.e("TAG", "afterTextChanged:::::::::::filteredList::::: $filteredList" )

                    if (filteredList.isNotEmpty()){
                        Log.e("TAG", "afterTextChanged:::::::::::2:::::: " )
                        searchBinding.searchcyclerview.visibility = View.VISIBLE
                        searchBinding.notfound.visibility = View.GONE
                        searchAdapter.updateData(filteredList.toMutableList())
                    }else{
                        Log.e("TAG", "afterTextChanged:::::::::::3::::: " )
                        searchBinding.searchcyclerview.visibility = View.GONE
                        searchBinding.notfound.visibility = View.VISIBLE
                    }
                }else{
                    Log.e("TAG", "afterTextChanged:::::::::::4::::: " )
                    searchBinding.searchcyclerview.visibility = View.VISIBLE
                    searchBinding.notfound.visibility = View.GONE
                    searchAdapter.updateData(articles)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.e("TAG", "afterTextChanged:::::::::::5:::::: " )
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.e("TAG", "afterTextChanged:::::::::::6:::::: " )
            }
        })

    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus ?: View(requireContext())
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun setupObservers() {
        viewModel.newsResponseLiveData.observe(viewLifecycleOwner) { newsResponse ->
            val articles = newsResponse.articles?.toMutableList() ?: mutableListOf()

            Log.e("TAG", "setupObservers::::::::::::::::articles::::::::: $articles")

            viewLifecycleOwner.lifecycleScope.launch {
                delay(2000)
                searchAdapter.updateData(articles)
                setupSearchListener()

            }

        }

        viewModel.errorLiveData.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            Log.e(ContentValues.TAG, "API Error: $errorMessage")
        }

        viewModel.articlesLiveData.observe(viewLifecycleOwner) { articleEntities ->
            Log.e(TAG, "setupObservers::::::::RD:::articleEntities:::::: $articleEntities")
            Log.e(TAG, "Room DB articles count: ${articleEntities.size}")

            val articles = articleEntities.map {
                Article(
                    author = it.author,
                    title = it.title,
                    description = it.description,
                    url = it.url,
                    urlToImage = it.urlToImage,
                    publishedAt = it.publishedAt,
                    content = it.content,
                    source = com.example.mynewsapp.MVVM.Source(id = null, name = it.sourceName)
                )
            }

            searchAdapter.updateData(articles.toMutableList())

            Log.e(TAG, "setupObservers::::" + articles)

        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _searchBinding = null
    }


}