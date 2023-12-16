package com.example.travel_photo_sharing_app.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.travel_photo_sharing_app.models.User
import com.google.gson.Gson
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.travel_photo_sharing_app.R
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.repositories.PostRepository
import com.example.travel_photo_sharing_app.repositories.UserRepository
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

lateinit var sharedPreferences: SharedPreferences
lateinit var prefEditor: SharedPreferences.Editor
val tag = "Utils"

fun saveDataToSharedPref(context: Context, file: String, key: String, data: Any, toJson: Boolean = false) {
    var dataString: String

    // configure shared preferences
    sharedPreferences = context.getSharedPreferences(file,
        AppCompatActivity.MODE_PRIVATE
    )
    prefEditor = sharedPreferences.edit()

    if(toJson){
        dataString = Gson().toJson(data)
    }
    else {
        dataString = data.toString()
    }
    prefEditor.putString(key, dataString)
    prefEditor.apply()
}

fun getAllUserPosts(context: Context): MutableList<Post> {
    val allPosts = mutableListOf<Post>()
    sharedPreferences = context.getSharedPreferences("POSTS", AppCompatActivity.MODE_PRIVATE)

    if(sharedPreferences.all.isNotEmpty()) {
        val gson = Gson()
        for(posts in sharedPreferences.all.values){
            val userPosts = gson.fromJson<List<Post>>(posts as String, object : TypeToken<List<Post>>() {}.type)
            allPosts.addAll(userPosts)
        }

    }
    Log.i(tag, "all posts: ${allPosts}")
    return allPosts
}



fun checkDuplicatedPost(newPost: Post, context: Context): Boolean {
    val allPost = getAllUserPosts(context)
    for(post in allPost){
        if(newPost.equals(post)) return true
    }
    return false

}

//suspend fun initializePosts(context: Context): MutableList<Post> {
//suspend fun initializePosts(context: Context): MutableLiveData<List<Post>> {
fun initializePosts(context: Context) {
    val postsToBeDisplay = mutableListOf<Post>()
    val sampleUser = User("johndoe@example.com", "123", "John Doe", mutableListOf(), mutableListOf())
    val email = sampleUser.email
    val postRepository = PostRepository()

    val samplePosts = mutableListOf(
        Post(
            type = "Condo",
            authorEmail = email,
            description = "Modern condo with 2 bedrooms and a great view of the city.",
            address = "123 Main St, Metropolis",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "condo",
        ),
        Post(
            type = "House",
            authorEmail = email,
            description = "Spacious house with a large backyard and modern amenities.",
            address = "456 Maple Ave, Springfield",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "house"
        ),
        Post(
            type = "Apartment",
            authorEmail = email,
            description = "Cozy apartment close to downtown and public transportation.",
            address = "789 River Rd, Riverdale",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "apartment"
        ),
        Post(
            type = "House",
            authorEmail = email,
            description = "Cozy suburban house with a beautiful garden and a family-friendly neighborhood.",
            address = "202 Maple Lane, Pleasantville",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "house_2"
        ),
        Post(
            type = "Apartment",
            authorEmail = email,
            description = "Stylish downtown apartment with easy access to nightlife and public transport.",
            address = "303 City Center Ave, Metropolis",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "apartment_2"
        ),
        Post(
            type = "House",
            authorEmail = email,
            description = "Traditional house in a historical neighborhood, featuring a large porch and backyard.",
            address = "404 Heritage Road, Oldtown",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "house_2"
        ),
        Post(
            type = "Condo",
            authorEmail = email,
            description = "Modern waterfront condo with panoramic ocean views and high-end finishes.",
            address = "505 Seaside Blvd, Bay City",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "condo_3"
        ),
        Post(
            type = "Apartment",
            authorEmail = email,
            description = "Compact and efficient apartment in a vibrant and trendy neighborhood.",
            address = "606 Downtown Ave, Night City",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "apartment_3"
        ),
        Post(
            type = "House",
            authorEmail = email,
            description = "Spacious country house with a large garden and serene natural surroundings.",
            address = "707 Countryside Lane, Greenfield",
            visibleToGuest = true,
            latitude = 0.0,
            longitude = 0.0,
            imageUrl = "house_3"
        )

    )

    // show the posts added by the users
    sharedPreferences = context.getSharedPreferences("POSTS", AppCompatActivity.MODE_PRIVATE)
    val allUserPosts = getAllUserPosts(context)
    Log.i(tag, "allMyPosts: ${allUserPosts}")
    postsToBeDisplay.addAll(samplePosts)

    for(post in samplePosts){
        postRepository.addPost(post)
    }

}

fun getCategorySpinnerList(context: Context): MutableList<String>{
    val categoryOptions = context.resources.getStringArray(R.array.category_options);
    Log.d(tag, "options are ${categoryOptions}")
    return categoryOptions.toMutableList()
}