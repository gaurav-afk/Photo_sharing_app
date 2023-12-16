package com.example.travel_photo_sharing_app.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travel_photo_sharing_app.MainActivity
import com.example.travel_photo_sharing_app.adapters.PostAdapter
import com.example.travel_photo_sharing_app.databinding.ActivitySavedPostsBinding
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.repositories.PostRepository
import com.example.travel_photo_sharing_app.utils.AuthenticationHelper
import kotlinx.coroutines.launch

class SavedPostsActivity : MainActivity() {
    private lateinit var binding: ActivitySavedPostsBinding
    private var loggedInUser: User? = null
    override val tag = "Shortlist"
    private val postRepository = PostRepository()
    private lateinit var adapter: PostAdapter
    private val savedPosts :MutableList<Post> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "My Favourite Posts"


//        binding.returnBtn.setOnClickListener {
//            finish()
//        }

        AuthenticationHelper.instance!!.loggedInUser.observe(this){user ->
            loggedInUser = user
        }

        lifecycleScope.launch {
                adapter = PostAdapter(savedPosts, loggedInUser, true, this@SavedPostsActivity)
                this@SavedPostsActivity.binding.shortlistRv.adapter = adapter
                this@SavedPostsActivity.binding.shortlistRv.layoutManager = LinearLayoutManager(this@SavedPostsActivity)
                this@SavedPostsActivity.binding.shortlistRv.addItemDecoration(
                    DividerItemDecoration(
                        this@SavedPostsActivity,
                        LinearLayoutManager.VERTICAL
                    )
                )
        }

    }

    override fun onResume() {
        super.onResume()
        AuthenticationHelper.instance!!.loggedInUser.observe(this){user ->
            loggedInUser = user
        }

        lifecycleScope.launch {
            for(postId in loggedInUser!!.savedPosts){
                val post = postRepository.getPostById(postId)
                if(post != null){
                    savedPosts.add(post)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}