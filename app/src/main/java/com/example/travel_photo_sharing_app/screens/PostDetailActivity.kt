package com.example.travel_photo_sharing_app.screens

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.travel_photo_sharing_app.databinding.ActivityPostDetailBinding
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.repositories.PostRepository
import com.example.travel_photo_sharing_app.repositories.UserRepository
import com.example.travel_photo_sharing_app.utils.AuthenticationHelper
import com.example.travel_photo_sharing_app.utils.CameraImageHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var post: Post
    private lateinit var postId: String
    private var loggedInUser: User? = null
    private val userRepository = UserRepository()
    private val postRepository = PostRepository()
    private val tag = "Post detail"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Travel Photo Sharing App"

        binding.returnBtn.setOnClickListener {
            finish()
        }

        AuthenticationHelper.instance!!.loggedInUser.observe(this) {user ->
            loggedInUser = user
        }
        Log.d(tag, "in post detail, logged in user ${loggedInUser}")

        val hasPost: Boolean = this@PostDetailActivity.intent.extras?.containsKey("POST") != null
        if (hasPost) {
            lifecycleScope.launch {
                postId = intent.getStringExtra("POST")!!
                Log.d(tag, "intent post id is ${postId}")

                post = postRepository.getPostById(postId)!!
                Log.i(tag, "has post $post")

                if (post != null) {
                    binding.postAddress.setText(post.address)
                    binding.postDescription.setText(post.description)
                    binding.authorEmail.setText(post.authorEmail)
                    binding.postCategory.setText(post.type)

            val image = post.imageUrl ?: "default_image"
            if(image == "default_image"){
                val res = resources.getIdentifier(image, "drawable", this@PostDetailActivity.packageName)
                binding.postImage.setImageResource(res)
            }
            else{
                val imgBitmap: Bitmap = CameraImageHelper.base64ToBitmap(image)
                binding.postImage.setImageBitmap(imgBitmap)
            }
                }

                val authorEmail = binding.authorEmail.text.toString()
                if(loggedInUser!!.email == authorEmail){
                    binding.followBtn.visibility = View.GONE
                    binding.unfollowBtn.visibility = View.GONE
                }
                else if(loggedInUser!!.following.contains(authorEmail)){
                    binding.followBtn.visibility = View.GONE
                    binding.unfollowBtn.visibility = View.VISIBLE
                }
                else{
                    binding.followBtn.visibility = View.VISIBLE
                    binding.unfollowBtn.visibility = View.GONE
                }

                binding.followBtn.setOnClickListener {
                    userRepository.follow(loggedInUser!!.email, authorEmail)
                    binding.followBtn.visibility = View.GONE
                    binding.unfollowBtn.visibility = View.VISIBLE
                    Snackbar.make(binding.root, "Followed User ${authorEmail}", Snackbar.LENGTH_LONG).show()
                }

                binding.unfollowBtn.setOnClickListener {
                    userRepository.unfollow(loggedInUser!!.email, authorEmail)
                    binding.followBtn.visibility = View.VISIBLE
                    binding.unfollowBtn.visibility = View.GONE
                    Snackbar.make(binding.root, "Unfollowed User ${authorEmail}", Snackbar.LENGTH_LONG).show()
                }

            }
        }

    }
}