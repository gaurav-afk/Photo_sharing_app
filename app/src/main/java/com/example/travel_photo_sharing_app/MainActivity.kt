package com.example.travel_photo_sharing_app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_photo_sharing_app.databinding.ActivityMainBinding
import com.example.travel_photo_sharing_app.adapters.PostAdapter
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.repositories.PostRepository
import com.example.travel_photo_sharing_app.screens.FollowerFolloweeActivity
import com.example.travel_photo_sharing_app.screens.LoginActivity
import com.example.travel_photo_sharing_app.screens.SavedPostsActivity
import com.example.travel_photo_sharing_app.screens.MyPostsActivity
import com.example.travel_photo_sharing_app.screens.PostDetailActivity
import com.example.travel_photo_sharing_app.utils.AuthenticationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

open class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var postAdapter: PostAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    private var postsToBeDisplayed: MutableList<Post> = mutableListOf()
    private var allPosts: MutableList<Post> = mutableListOf()
    private lateinit var allPublicPosts: MutableLiveData<List<Post>>
    private var loggedInUser: User? = null
    open val tag = "Main"
    private val postRepository = PostRepository()
    private val markerPostMap = HashMap<Marker, Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthenticationHelper.getInstance(this)
        AuthenticationHelper.instance!!.loggedInUser.observe(this) {user ->
            Log.d(tag, "oncreate log in: ${user}")
            loggedInUser = user
        }
        Log.d(tag, "in main, loggedin users is $loggedInUser")

        Log.i(tag, "post to be displayed ${allPosts}")

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Travel Photo Sharing App"

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.view?.visibility = View.GONE

        val justLoggedOut = (this.intent.getStringExtra("REFERER") ?: null) == "Signout"
        if(justLoggedOut){
            Snackbar.make(binding.root, "Logout Successful", Snackbar.LENGTH_LONG).show()
        }
        val justClearedData = (this.intent.getStringExtra("REFERER") ?: null) == "Toolbar"
        if(justClearedData){
            Snackbar.make(findViewById(R.id.root_layout), "Data erased!", Snackbar.LENGTH_LONG).show()
        }

        postRepository.getAllPublicPosts()

        binding.viewSelection.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            // Respond to button selection
            var view = findViewById<Button>(checkedId).text.toString()
            Log.d(tag, "view is ${view}")
            Log.d(tag, "view clicked ${toggleButton}, ${checkedId}, ${isChecked}")
            if(view == "List"){
                binding.mapViewBtn.setBackgroundColor(getColor(R.color.light_grey))
                binding.listViewBtn.setBackgroundColor(getColor(R.color.light_orange))

                binding.postsRecyclerView.visibility = View.VISIBLE
                mapFragment.view?.visibility = View.GONE
            }
            else if(view == "Map"){
                binding.listViewBtn.setBackgroundColor(getColor(R.color.light_grey))
                binding.mapViewBtn.setBackgroundColor(getColor(R.color.light_orange));

                binding.postsRecyclerView.visibility = View.GONE
                mapFragment.view?.visibility = View.VISIBLE
            }
        }

        postAdapter = PostAdapter(postsToBeDisplayed, loggedInUser, false, this@MainActivity)
        binding.postsRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.postsRecyclerView.adapter = postAdapter


        binding.searchButton.setOnClickListener {
            performLocalSearch(binding.searchEditText.text.toString())
        }

        binding.travelImage.setOnClickListener {
            triggerSearchWithType("Travel")
        }

        binding.foodImage.setOnClickListener {
            triggerSearchWithType("Food")
        }

        binding.selfieImage.setOnClickListener {
            triggerSearchWithType("Selfie")
        }
    }

    // Function to trigger search with type
    private fun triggerSearchWithType(type: String) {
        performSearchByType(type)
    }

    private fun performSearchByType(type: String) {
        val liveDataPosts = postRepository.getPostsByType(type)

        liveDataPosts.observe(this, Observer { posts ->
            postsToBeDisplayed.clear()
            postsToBeDisplayed.addAll(posts)
            addPostsToMap(postsToBeDisplayed)
            postAdapter.notifyDataSetChanged()
        })
    }

    override fun onResume() {
        AuthenticationHelper.instance!!.loggedInUser.observe(this) {user ->
            Log.d(tag, "onresume log in: ${user}")
            loggedInUser = user
            postAdapter.loggedInUser = loggedInUser
        }
        allPosts.clear()
//        allPosts = initializePosts(this)
//        initializePosts(this)
        postRepository.publicPosts.observe(this){ publicPosts ->
            Log.d(tag, "in observer ${publicPosts}")

            for(p in publicPosts){
                Log.d(tag,  "marking ${p.address}, id: ${p.idFromDb}")
            }

            postsToBeDisplayed.clear()
            postsToBeDisplayed.addAll(publicPosts)
            postAdapter.notifyDataSetChanged()
            addPostsToMap(postsToBeDisplayed)
        }

        super.onResume()
    }

    // options menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.options_menu, menu)
        Log.d(tag, "logged in user in menu ${loggedInUser}")
        if(loggedInUser == null) {
            menu.findItem(R.id.go_to_saved_posts).setVisible(false)
            menu.findItem(R.id.go_to_my_posts).setVisible(false)
            menu.findItem(R.id.followersOrFollowees).setVisible(false)
            menu.findItem(R.id.logout).setVisible(false)
        }
        else {
            menu.findItem(R.id.login).setVisible(false)
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.login -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.go_to_saved_posts -> {
                val intent = Intent(this, SavedPostsActivity::class.java)
                intent.putExtra("USER", loggedInUser?.username)
                startActivity(intent)
                return true
            }
            R.id.go_to_my_posts -> {
                if(loggedInUser != null) {
                    val intent = Intent(this, MyPostsActivity::class.java)
                    intent.putExtra("USER", loggedInUser?.username)
                    startActivity(intent)
                    return true
                }
                else {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("REFERER", "MainActivity")
                    startActivity(intent)
                    return true
                }
            }
            R.id.followersOrFollowees -> {
                val intent = Intent(this, FollowerFolloweeActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.logout -> {
                val intent = Intent(this, MainActivity::class.java)
                AuthenticationHelper.instance!!.signOut()
                intent.putExtra("REFERER", "MainActivity")
                startActivity(intent)
                return true
            }
            // for testing only. Remove this later
            R.id.delete_users -> {
                this.sharedPreferences = getSharedPreferences("USERS", MODE_PRIVATE)
                this.prefEditor = this.sharedPreferences.edit()

                prefEditor.clear()
                prefEditor.apply()

                // logs out the user after all users are deleted
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USER", "")
                intent.putExtra("REFERER", "Toolbar")
                startActivity(intent)
                return true
            }
            R.id.delete_posts -> {
                this.sharedPreferences = getSharedPreferences("POSTS", MODE_PRIVATE)
                this.prefEditor = this.sharedPreferences.edit()

                prefEditor.clear()
                prefEditor.apply()
                Snackbar.make(findViewById(R.id.root_layout), "post erased!", Snackbar.LENGTH_LONG).show()
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun performLocalSearch(searchText: String) {
        postRepository.getAllPublicPosts() // Ensure the latest public posts are fetched

        postRepository.publicPosts.observe(this, Observer { posts ->
            // Perform the local filtering
            val searchLowerCase = searchText.lowercase()
            val filteredPosts = posts.filter { post ->
                post.address.lowercase().contains(searchLowerCase) ||
                        post.authorEmail.lowercase().contains(searchLowerCase)
                // Add more conditions here if needed
            }

            // Update UI based on filtered posts
            if (filteredPosts.isEmpty()) {
                Toast.makeText(this, "No matching posts found", Toast.LENGTH_SHORT).show()
            } else {
                postsToBeDisplayed.clear()
                postsToBeDisplayed.addAll(filteredPosts)
                postAdapter.notifyDataSetChanged()
            }
        })
    }



    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(tag, "on map ready")
        mMap = googleMap
        Log.d(tag, "google map ${postsToBeDisplayed}")

        mMap.setOnInfoWindowClickListener {  marker ->
                Log.d(tag, "here info window clicked")
                markerClickedHandler(marker)
        }

        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.isTrafficEnabled = true

        val uiSettings = googleMap.uiSettings
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isCompassEnabled = true

    }

    private fun markerClickedHandler(marker: Marker): Boolean {
        Log.d(tag, "marker clicked: ${marker}, ${markerPostMap}")
        // popup post details
        val post = markerPostMap[marker]
        Log.d(tag, "marketPostMap post: ${post}")
        if(loggedInUser != null){
            Log.i(tag, "${loggedInUser} logged in")
            val intent = Intent(this@MainActivity, PostDetailActivity::class.java)
            intent.putExtra("POST", post!!.idFromDb)
            this@MainActivity.startActivity(intent)
        }
        else {
            Log.i(tag, "no one logged in")
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("REFERER", "MainActivity")
            this@MainActivity.startActivity(intent)
        }
        return false
    }

    private fun removeAllMarkersOnMap(){
        Log.d(tag, "removing markers")
        for((marker, Post)in markerPostMap){
            Log.d(tag, "removing marker ${marker}")
            marker.remove()
        }
    }
    private fun addPostsToMap(posts: MutableList<Post>){
        removeAllMarkersOnMap()
        Log.d(tag, "adding posts to map")
        var cameraSet = false  // set the camera position to the first post by default

        for(post in posts){
            val location = LatLng(post.latitude, post.longitude)
            Log.d(tag, "in addPostsToMap ${post}, ${location}")

            if(!cameraSet){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                cameraSet = true
            }

            val markerOption = MarkerOptions().position(location)
            markerOption.title(post.address)
            val marker: Marker = mMap.addMarker(markerOption)!!
            markerPostMap[marker] = post
            Log.d(tag, "Marker set is ${markerOption}")
        }
    }
}
