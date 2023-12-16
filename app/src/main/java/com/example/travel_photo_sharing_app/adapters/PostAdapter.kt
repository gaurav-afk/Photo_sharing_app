package com.example.travel_photo_sharing_app.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.screens.LoginActivity
import com.example.travel_photo_sharing_app.screens.PostDetailActivity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.travel_photo_sharing_app.databinding.ItemPostBinding
import com.example.travel_photo_sharing_app.repositories.PostRepository
import com.example.travel_photo_sharing_app.repositories.UserRepository
import com.example.travel_photo_sharing_app.utils.CameraImageHelper
import kotlinx.coroutines.launch

class PostAdapter(private var posts: MutableList<Post>, var loggedInUser: User?, private val showShortlistOnly: Boolean, context: Context) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private val userRepository = UserRepository()
    private val postRepository = PostRepository()
    private val tag = "Post Adapter"

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, context: Context, pos: Int) {
            Log.d(tag, "in tag, post and loggedInUser ${post}, ${loggedInUser}")

            // Assuming you want to display the post type as the title
            binding.postAuthor.text = post.authorEmail
            binding.postType.text = post.type
            binding.postDescriptionTextView.text = post.description
            binding.postAddressTextView.text = post.address
            binding.postId.text = post.idFromDb

            Log.d(tag, "post image is ${post.imageUrl}")
            val image = post.imageUrl ?: "default_image"
            if(image == "default_image"){
                val res = context.resources.getIdentifier(image, "drawable", context.packageName)
                this.binding.postImage.setImageResource(res)
            }
            else{
                val imgBitmap: Bitmap = CameraImageHelper.base64ToBitmap(image)
                this.binding.postImage.setImageBitmap(imgBitmap)
            }

            Log.d(tag, "savedpost and idfromdb ${loggedInUser?.savedPosts}, ${post.idFromDb}")
            if(loggedInUser != null && loggedInUser!!.savedPosts.contains(post.idFromDb)){
                binding.removeBtn.visibility = View.VISIBLE
                binding.savePostBtn.visibility = View.GONE
            }
            else if(loggedInUser != null){
                binding.savePostBtn.visibility = View.VISIBLE
                binding.removeBtn.visibility = View.GONE
            }
            else {
                binding.savePostBtn.visibility = View.GONE
                binding.removeBtn.visibility = View.GONE
            }


            binding.postCard.setOnClickListener {
                // popup post details
                if(this@PostAdapter.loggedInUser != null){
                    Log.i(tag, "${loggedInUser} logged in")
                    val intent = Intent(context, PostDetailActivity::class.java)
                    intent.putExtra("POST", post.idFromDb) // pass only the email since passing the whole object is too large, which will cause an error
                    context.startActivity(intent)
                }
                else {
                    Log.i(tag, "no one logged in")
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.putExtra("REFERER", "MainActivity")
                    context.startActivity(intent)
                }
            }

            binding.savePostBtn.setOnClickListener {
                (context as AppCompatActivity).lifecycleScope.launch {
                    val postId = post.idFromDb
                    userRepository.savePost(loggedInUser!!.email, postId!!)
                    loggedInUser!!.savedPosts.add(postId!!)
                    Log.d(tag, "post id is ${postId}")
                    this@PostAdapter.notifyDataSetChanged()
                }
            }

            binding.removeBtn.setOnClickListener {
                val postIdToBeRemoved = post.idFromDb
                (context as AppCompatActivity).lifecycleScope.launch {
                    Log.d(tag,"removing saved post")
                    userRepository.unSavePost(loggedInUser!!.email, postIdToBeRemoved!!)
                }
                for(i in 0..< loggedInUser!!.savedPosts.size){
                    Log.d(tag, "savedPosts id is ${loggedInUser!!.savedPosts[i]}, idFromDb is ${post.idFromDb}")
                    if(loggedInUser!!.savedPosts[i] == post.idFromDb){
                        loggedInUser!!.savedPosts.removeAt(i)
                        if(showShortlistOnly){
                            posts.removeAt(i)
                        }
                        break
                    }
                }

                this@PostAdapter.notifyDataSetChanged()


            }

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context
        holder.bind(post, context, position)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

}
