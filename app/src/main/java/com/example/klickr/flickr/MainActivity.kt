package com.example.klickr.flickr

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.SearchView.OnQueryTextListener
import android.support.v7.widget.SearchView.VISIBLE
import android.text.TextUtils
import android.view.*
import android.view.View.GONE
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.klickr.flickr.data.DataModel
import com.example.klickr.flickr.data.Interfaces
import com.example.klickr.flickr.data.RetrofitService
import com.example.klickr.flickr.data.Utils
import com.example.klickr.flickr.data.Utils.Companion.VIEW_TYPE_ITEM
import com.example.klickr.flickr.data.Utils.Companion.VIEW_TYPE_LOADING
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.image_list_item.view.*
import kotlinx.android.synthetic.main.progress_bar_item.view.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var fabOpen: Animation? = null
    private var fabClose: Animation? = null
    private var rotateForward: Animation? = null
    private var rotateBackward: Animation? = null
    private var isFabOpen = true

    private var searchView: SearchView? = null
    private var searchMenuItem: MenuItem? = null

    private var imagesList = ArrayList<DataModel.PhotoModel>()
    private var adapter: ImagesAdapter? = null
    private var disposable: Disposable? = null
    private val retrofitService by lazy { RetrofitService.create() }
    private var isSearch = false
    private var page = 1
    private var searchText = "yes"
    private var mGridLayoutManager: GridLayoutManager? = null

    private var isLoading: Boolean = false
    private val visibleThreshold = 5
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0
    private var mOnLoadMoreListener: Interfaces.OnLoadMoreListener? = null
    private var spanCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView?.setNavigationItemSelectedListener(this)

        setFAB()
        getData()
        setAdapterAndRecyclerView()
    }

    //SetUp Recycler And Adapter
    private fun setAdapterAndRecyclerView() {
        mGridLayoutManager = GridLayoutManager(this, spanCount)
        imagesRV?.layoutManager = mGridLayoutManager
        adapter = ImagesAdapter()
        imagesRV?.adapter = adapter

        //Add Scroll Listener
        imagesRV?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = mGridLayoutManager?.itemCount ?: 0
                lastVisibleItem = mGridLayoutManager?.findLastVisibleItemPosition() ?: 0

                if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener?.onLoadMore()
                    }
                    isLoading = true
                }
            }
        })

        //Set Span Size for Loading Item
        mGridLayoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter?.getItemViewType(position)) {
                    VIEW_TYPE_LOADING -> spanCount
                    VIEW_TYPE_ITEM -> 1
                    else -> -1
                }
            }
        }

        //Ad empty item and show progress bar at the bottom
        adapter?.setOnLoadMoreListener(object : Interfaces.OnLoadMoreListener {
            override fun onLoadMore() {
                imagesList.add(DataModel.PhotoModel())
                adapter?.notifyItemInserted(imagesList.size - 1)
                Handler().postDelayed({
                    imagesList.removeAt(imagesList.size - 1)
                    adapter?.notifyItemRemoved(imagesList.size - 1)
                    isSearch = false
                    getData()
                }, 1500)
            }
        })
    }

    //Get Data from Api using Search and get 30 items at a time
    private fun getData() {
        if (Utils.checkInternet(this))
            disposable = retrofitService.searchImages(searchText, page, 30)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { response ->
                                progressBarMain?.visibility = View.GONE
                                imagesRV.visibility = View.VISIBLE
                                if (response.photos?.photo != null && response.photos.photo.isNotEmpty()) {
                                    noImagesTV?.visibility = View.GONE
                                    if (isSearch)
                                        imagesList.clear()
                                    page++
                                    imagesList.addAll(response.photos.photo)
                                    adapter?.notifyDataSetChanged()
                                    adapter?.setLoaded()
                                } else if (page == 1) {
                                    imagesRV.visibility = View.GONE
                                    noImagesTV?.visibility = View.VISIBLE
                                }
                            },
                            { error ->
                                progressBarMain?.visibility = View.GONE
                                Utils.showDialog(this, null, getString(R.string.oops),
                                        error.message ?: "", getString(R.string.ok), "")
                            })
        else {
            progressBarMain?.visibility = View.GONE
            Utils.showSnackBar(this, object : Interfaces.SnackBarListener {
                override fun onRetryClickedFromSnackBar() {
                    getData()
                }

            }, getString(R.string.snackBar_internet_connection), drawerLayout, true)
        }
    }

    // Setup FloatingActionButton
    private fun setFAB() {
        fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        rotateForward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_forward)
        rotateBackward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_backward)

        animateFAB()

        fab?.setOnClickListener {
            animateFAB()
        }
    }

    //Animate FloatingActionButton
    private fun animateFAB() {
        if (isFabOpen) {
            fab?.startAnimation(rotateBackward)
            fabItem?.startAnimation(fabClose)
            fabItem?.isClickable = false
            isFabOpen = false
        } else {
            fab?.startAnimation(rotateForward)
            fabItem?.startAnimation(fabOpen)
            fabItem?.isClickable = true
            isFabOpen = true
        }
    }

    //Recycler view adapter
    inner class ImagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int = imagesList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is Holder)
                if (holder.itemView.imageView != null)
                    Glide.with(this@MainActivity)
                            .applyDefaultRequestOptions(RequestOptions()
                                    .placeholder(R.mipmap.ic_launcher)
                                    .centerCrop()
                                    .error(R.mipmap.ic_launcher))
                            .load(imagesList[position].constructURL())
                            .into(holder.itemView.imageView)
                else if (holder is LoadingViewHolder)
                    holder.view.progressBar.visibility = View.VISIBLE

        }

        fun setOnLoadMoreListener(onLoadMoreListener: Interfaces.OnLoadMoreListener) {
            mOnLoadMoreListener = onLoadMoreListener
        }

        fun setLoaded() {
            isLoading = false
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                if (viewType == VIEW_TYPE_LOADING)
                    LoadingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.progress_bar_item, parent, false))
                else
                    Holder(LayoutInflater.from(parent.context).inflate(R.layout.image_list_item, parent, false))

        override fun getItemViewType(position: Int): Int = if (TextUtils.isEmpty(imagesList[position].constructURL()))
            VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    //Holders for Recycler View Items
    class Holder(val view: View) : RecyclerView.ViewHolder(view)

    class LoadingViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //Add Search View and Items in settings
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        searchMenuItem = menu.findItem(R.id.action_search)
        searchView = searchMenuItem?.actionView as SearchView
        searchView?.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                progressBarMain?.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(query)) {
                    isSearch = true
                    searchText = query!!
                    page = 1
                    getData()
                } else
                    Toast.makeText(this@MainActivity, getString(R.string.search_field_empty_text), Toast.LENGTH_LONG).show()
                searchView?.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_2 -> {
                spanCount = 2
                mGridLayoutManager?.spanCount = spanCount
                true
            }
            R.id.action_3 -> {
                spanCount = 3
                mGridLayoutManager?.spanCount = spanCount
                true
            }
            R.id.action_4 -> {
                spanCount = 4
                mGridLayoutManager?.spanCount = spanCount
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {

            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
