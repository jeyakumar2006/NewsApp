package com.example.mynewsapp.Home

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.mynewsapp.Utils.Constant.apiKey
import com.example.mynewsapp.Utils.Constant.dpToPx
import com.example.mynewsapp.Utils.Constant.isNetworkAvailable
import com.example.mynewsapp.Utils.SharedPrefHelper
import com.example.mynewsapp.databinding.FragmentHomeBinding
import com.example.mynewsapp.MVVM.Article
import com.example.mynewsapp.MVVM.MainViewModel
import com.example.mynewsapp.NewDetailActivity.NewsDetailsActivity
import com.example.mynewsapp.R
import com.facebook.shimmer.BuildConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class HomeFragment : Fragment(), OnArticleActionListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private var _homebinding: FragmentHomeBinding? = null
    private val homebinding get() = _homebinding!!


    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewModel: MainViewModel




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    private fun setupViewPager() {
        homebinding.viewpager.apply {
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 3


            setPageTransformer { page, position ->
                page.alpha = 0.25f + (1 - Math.abs(position))
            }


            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)


                    Log.e(ContentValues.TAG, "onPageSelected::::4444444")


                    if (::viewPagerAdapter.isInitialized && position in 0 until viewPagerAdapter.itemCount) {
                        val article = viewPagerAdapter.getArticleAt(position)
                        Log.d(
                            ContentValues.TAG,
                            "Swiped to position: $position - ${article?.title}"
                        )
                    }
                }
            })
        }
    }


    private fun setupObservers() {
        viewModel.newsResponseLiveData.observe(viewLifecycleOwner) { newsResponse ->
            val articles = newsResponse.articles?.toMutableList() ?: mutableListOf()


            Log.e("TAG", "setupObservers::::::::::::::::articles::::::::: $articles")


            viewLifecycleOwner.lifecycleScope.launch {
                delay(2000)
                swipeGetsures()
                viewPagerAdapter.updateData(articles)
                homebinding.swipeRefreshLayout.isRefreshing = false
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


            viewPagerAdapter.updateData(articles.toMutableList())
            swipeGetsures()


            Log.e(TAG, "setupObservers::::" + articles)


        }




    }


    override fun onArticleSwipeRight(article: Article) {
        val intent = Intent(requireContext(), NewsDetailsActivity::class.java)
        intent.putExtra("article", article)
        requireContext().startActivity(intent)
    }


    override fun onArticleRemoved(position: Int) {

    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _homebinding = FragmentHomeBinding.inflate(inflater, container, false)
//        return inflater.inflate(R.layout.fragment_home, container, false)


        homebinding.profileImg.profilelay.layoutParams.width = 60.dpToPx()
        homebinding.profileImg.profilelay.layoutParams.height = 60.dpToPx()



        homebinding.swipeRefreshLayout.setOnRefreshListener {
            viewPagerAdapter.makeloading()
            viewModel.getNewsData("us", apiKey)
        }
        return homebinding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewPagerAdapter = ViewPagerAdapter(requireContext(), null, this)
        homebinding.viewpager.adapter = viewPagerAdapter


        setupObservers()
        setupViewPager()




        if (isNetworkAvailable(requireContext())) {
            Log.e(TAG, "onViewCreated::++++:1111")
            viewModel.getNewsData("us", apiKey)
        } else {
            Log.e(TAG, "onViewCreated:+++::2222")

            viewModel.loadArticlesFromDb()


            Log.e(TAG, "onViewCreated::::" + viewModel.loadArticlesFromDb())
        }
    }


    fun swipeGetsures() {
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {


            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition


                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        viewPagerAdapter.removePage(position)
                    }


                    ItemTouchHelper.RIGHT -> {
                        val context = viewHolder.itemView.context
                        val data = viewPagerAdapter.getArticleAt(position) // Get your object/string
                        Log.e(TAG, "onSwiped:::::::::data:::::::: $data" )


                        val intent = Intent(requireContext(), NewsDetailsActivity::class.java)
                        intent.putExtra("article", data)
                        context.startActivity(intent)
                        viewPagerAdapter.notifyItemChanged(position)
                    }
                }
            }


            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView


                if (dX < 0) {
                    val background = ColorDrawable(Color.RED)
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)

                    val originalIcon = ContextCompat.getDrawable(itemView.context, R.drawable.delete)

                    if (originalIcon != null) {
                        val icon = originalIcon.mutate()
                        val scaledWidth = 64
                        val scaledHeight = 64

                        val iconLeft = itemView.right - (Math.abs(dX.toInt()) / 2) - (scaledWidth / 2)
                        val iconTop = itemView.top + (itemView.height - scaledHeight) / 2
                        val iconRight = iconLeft + scaledWidth
                        val iconBottom = iconTop + scaledHeight

                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        icon.draw(c)
                    }

                }


                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }


        }


        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(
            homebinding.viewpager.getChildAt(0) as RecyclerView)
    }


    override fun onResume() {
        super.onResume()
        Glide.with(requireContext()).load(SharedPrefHelper.getString("ProfileImage"))
            .into(homebinding.profileImg.profilelay)

        if (::viewPagerAdapter.isInitialized && viewPagerAdapter.itemCount > 0) {
            Log.e(TAG, "onResume::::::::::::::::::: $" )
            viewPagerAdapter.notifyDataSetChanged()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _homebinding = null
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}



