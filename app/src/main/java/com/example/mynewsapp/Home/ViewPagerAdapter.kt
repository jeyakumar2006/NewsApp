package com.example.mynewsapp.Home


import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mynewsapp.R
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mynewsapp.MVVM.Article
import com.example.mynewsapp.databinding.ShimmerViewpagerBinding
import com.example.mynewsapp.databinding.ViewPagerItemBinding




interface OnArticleActionListener {
    fun onArticleSwipeRight(article: Article)
    fun onArticleRemoved(position: Int)
}


class ViewPagerAdapter(
    private val context: Context,
    private var articleList: MutableList<Article>?,
    private val listener: OnArticleActionListener
) : RecyclerView.Adapter<ViewHolder>() {


    private val VIEW_TYPE_SHIMMER = 0
    private val VIEW_TYPE_NORMAL = 1
    private var isLoading = true


    inner class PageViewHolder(val pagerItemBinding: ViewPagerItemBinding) : ViewHolder(pagerItemBinding.root) {
        val textView: TextView = itemView.findViewById(R.id.slide_screen_tv)
        val imageView: ImageView = itemView.findViewById(R.id.slide_screen_item_iv)
    }


    inner class ShimmerViewHolder(val shimmerViewpagerBinding: ShimmerViewpagerBinding): ViewHolder(shimmerViewpagerBinding.root)




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.e("TAG", "onViewCreated::::::1-5::::isLoading::::: $isLoading" )
        return if (viewType == VIEW_TYPE_SHIMMER){
            Log.e("TAG", "onViewCreated::::::2::::::::: " )
            val shimmerViewpagerBinding = ShimmerViewpagerBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            ShimmerViewHolder(shimmerViewpagerBinding)
        }else{
            Log.e("TAG", "onViewCreated::::::3::::::::: " )
            val pagerItemBinding = ViewPagerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            PageViewHolder(pagerItemBinding)
        }


    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is PageViewHolder && articleList!=null){
            holder.itemView.translationX = 0f
            holder.itemView.alpha = 1f


            val article = articleList!![position]
            Log.e("TAG", "onBindViewHolder::::::::::::::::::article:::::::: $article" )
            holder.textView.text = article.title
            Glide.with(context)
                .load(article.urlToImage)
                .placeholder(R.drawable.placeholder_img)
                .error(R.drawable.placeholder_img)
                .into(holder.imageView)


        }


    }




    override fun getItemCount(): Int {
        return if (!isLoading) articleList?.size ?:0 else 1
    }


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) VIEW_TYPE_SHIMMER else VIEW_TYPE_NORMAL
    }


    fun getArticleAt(position: Int): Article? = articleList?.get(position)


    fun updateData(newData: MutableList<Article>?) {
//        articleList?.clear()
        isLoading = false
        articleList = newData
        notifyDataSetChanged()
    }


    fun makeloading(){
        isLoading = true
        articleList = null
        notifyDataSetChanged()
    }


    fun removePage(position: Int) {
        articleList?.removeAt(position)
        notifyItemRemoved(position)
    }


//    fun getItem(position: Int): Article {
//        return articleList!![position]
//    }






}
