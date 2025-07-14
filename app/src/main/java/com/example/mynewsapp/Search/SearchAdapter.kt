package com.example.mynewsapp.Search

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.mynewsapp.R
import com.example.mynewsapp.databinding.SearchFrameBinding
import com.example.mynewsapp.databinding.ShimmerSearchBinding
import com.example.mynewsapp.MVVM.Article

class SearchAdapter (private var datalist: MutableList<Article>?,): RecyclerView.Adapter<ViewHolder>() {

    private val VIEW_TYPE_SHIMMER = 0
    private val VIEW_TYPE_NORMAL = 1
    private var isLoading = true

    inner class SearchViewHolder(val adapterbinding: SearchFrameBinding) : ViewHolder(adapterbinding.root){
        fun bind(article: Article){
            Glide.with(adapterbinding.root).load(article.urlToImage).error(R.drawable.placeholder_img).into(adapterbinding.newsimage)
            adapterbinding.author.text = article.author
        }
    }

    inner class ShimmerViewHolder(val shimmerbinding: ShimmerSearchBinding) : ViewHolder(shimmerbinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.e("TAG", "onCreateViewHolder::::::::::isLoading::::::: $isLoading" )
        return if (viewType == VIEW_TYPE_SHIMMER){
            Log.e("TAG", "onCreateViewHolder::::::::::1::::::: " )
            val shimmerbinding = ShimmerSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ShimmerViewHolder(shimmerbinding)
        } else{
            Log.e("TAG", "onCreateViewHolder::::::::::2::::::: " )
            val adapterbinding = SearchFrameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SearchViewHolder(adapterbinding)
        }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        Log.e("TAG", "onCreateViewHolder::::::::::3::::::: " )
        if (holder is SearchViewHolder && datalist != null) {
            holder.bind(datalist!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else datalist?.size ?:0
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_NORMAL
    }

    fun updateData(newData: MutableList<Article>?) {
        isLoading = false
        datalist = newData
        notifyDataSetChanged()
    }


}