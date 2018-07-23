package com.example.klickr.flickr

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.SearchView.OnQueryTextListener
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.klickr.flickr.data.FlickrResponse
import com.example.klickr.flickr.data.RetrofitService
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.image_list_item.view.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var fabOpen: Animation? = null
    private var fabClose: Animation? = null
    private var rotateForward: Animation? = null
    private var rotateBackward: Animation? = null
    private var isFabOpen = false

    private var searchView: SearchView? = null
    private var searchMenuItem: MenuItem? = null

    private var imagesList = ArrayList<FlickrResponse>()

    private var disposable: Disposable? = null
    //private var retrofitService  by lazy { RetrofitService.create() }

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

/*
        disposable = retrofitService.searchImages("", 1, 30)
                .subscribe()
*/


        imagesRV?.layoutManager = GridLayoutManager(this, 2)
        imagesRV?.adapter = ImagesAdapter()

    }

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

    inner class ImagesAdapter : RecyclerView.Adapter<Holder>() {

        override fun getItemCount(): Int = imagesList.size

        override fun onBindViewHolder(holder: Holder, position: Int) {

            if (holder.view.imageView != null)
                Glide.with(this@MainActivity)
                        .applyDefaultRequestOptions(RequestOptions()
                                .placeholder(R.mipmap.ic_launcher)
                                .centerCrop()
                                .error(R.mipmap.ic_launcher))
                        .load(imagesList[position])
                        .into(holder.view.imageView)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
                LayoutInflater.from(parent.context).inflate(R.layout.image_list_item, parent, false))

    }

    class Holder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        searchMenuItem = menu.findItem(R.id.action_search)
        searchView = searchMenuItem?.actionView as SearchView
        searchView?.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
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
                imagesRV?.layoutManager = GridLayoutManager(this, 2)
                true
            }
            R.id.action_3 -> {
                imagesRV?.layoutManager = GridLayoutManager(this, 3)
                true
            }
            R.id.action_4 -> {
                imagesRV?.layoutManager = GridLayoutManager(this, 4)
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
