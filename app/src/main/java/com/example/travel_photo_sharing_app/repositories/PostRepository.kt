package com.example.travel_photo_sharing_app.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.travel_photo_sharing_app.models.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.EventListener

class PostRepository {
    private val tag = "Post Repo"
    private val db = Firebase.firestore

    private val COLLECTION_POSTS = "posts"
    private val COLLECTION_USERS = "Users"
    private val FIELD_CREATED_POSTS = "createdPosts"
    private val FIELD_ADDRESS = "address"
    private val FIELD_AUTHOR_EMAIL = "authorEmail"
    private val FIELD_DESC = "description"
    private val FIELD_VISIBLE_TO_GUEST = "visibleToGuest"
    private val FIELD_IMAGE_URL = "imageUrl"
    private val FIELD_CREATED_AT = "createdAt"

    var publicPosts : MutableLiveData<List<Post>> = MutableLiveData<List<Post>>()

    suspend fun getPostById(postId: String): Post?{
        return try{
            val result = db.collection(COLLECTION_POSTS).document(postId).get().await()

            Log.d(tag, "getPostByEmail result ${result.data}")
            if(result.data != null){
                Post(result)
            }
            else{
                null
            }

        }catch (ex : Exception){
            Log.e(tag, "removeFromFav failed: $ex")
            null
        }
    }

    fun getAllPublicPosts(){
        try{
            db.collection(COLLECTION_POSTS)
                .addSnapshotListener(EventListener{ result, error ->
                    val publicPostsLiveData = mutableListOf<Post>()
                    for(docRef in result!!.documents) {
                        Log.d(tag, "in pub post snapshot listener ${docRef}")
                        if (error != null) {
                            Log.e(
                                tag,
                                "getAllPublicPosts: Listening to all public posts collection failed due to error : $error",
                            )
                            return@EventListener
                        }

                        if (docRef != null) {
                            Log.d(tag, "visible? ${docRef["visibleToGuest"]}")
                            if(docRef["visibleToGuest"] == true){
                                publicPostsLiveData.add(Post(docRef))
                            }
                        } else {
                            Log.d(
                                tag,
                                "getAllPublicPosts: No data in the result after updating"
                            )
                        }
                    }
                    publicPosts.postValue(publicPostsLiveData)
                })


        }catch (ex : Exception){
            Log.e(tag, "removeFromFav failed: $ex")
        }
    }

    fun addPost(newPost: Post){
        Log.d(tag, "adding new post")
        try{
            val data : MutableMap<String, Any?> = newPost.toHashMap()

            // save new Post
            val document = db.collection(COLLECTION_POSTS).document()
                Log.d(tag, "new Post id is ${document.id}")
                db.collection(COLLECTION_POSTS)
                .document(document.id)
                .set(data)
                .addOnSuccessListener { docRef ->
                    Log.d(tag, "Added ${docRef} to Posts")

                    // record the new post in the corresponding user
                    db.collection(COLLECTION_USERS).document(newPost.authorEmail)
                    .update("createdPosts", FieldValue.arrayUnion(document.id))
                        .addOnSuccessListener {
                            Log.d(tag, "created posts added ${newPost.authorEmail}")
                        }
                        .addOnFailureListener {ex ->
                            Log.d(tag, "failed to add to created posts: ${ex}")
                        }
                }
                .addOnFailureListener { ex ->
                    Log.e(tag, "Failed to add ${newPost}: $ex")
                }

        }catch (ex : Exception){
            Log.e(tag, "addPost failed: $ex", )
        }
    }
    fun getPostsByType(type: String): MutableLiveData<List<Post>> {
        val postsByType: MutableLiveData<List<Post>> = MutableLiveData()

        db.collection(COLLECTION_POSTS)
            .whereEqualTo("type", type)
            .get()
            .addOnSuccessListener { documents ->
                val posts: MutableList<Post> = mutableListOf()
                for (document in documents) {
                    try {
                        document.data?.let {
                            val post = Post(document)
                            posts.add(post)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error converting document to Post", e)
                    }
                }
                postsByType.value = posts
            }
            .addOnFailureListener { exception ->
                Log.e(tag, "Error getting documents by type: $type", exception)
            }

        return postsByType
    }

    fun getPostsByAddress(searchAddress: String): MutableLiveData<List<Post>> {
        val postsByAddress: MutableLiveData<List<Post>> = MutableLiveData()

        db.collection(COLLECTION_POSTS)
            .whereEqualTo("address", searchAddress)
            .get()
            .addOnSuccessListener { documents ->
                val posts: MutableList<Post> = mutableListOf()
                for (document in documents) {
                    try {
                        document.data?.let {
                            val post = Post(document)
                            posts.add(post)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error converting document to Post", e)
                    }
                }
                postsByAddress.value = posts
            }
            .addOnFailureListener { exception ->
                Log.e(tag, "Error getting documents by address: $searchAddress", exception)
                postsByAddress.value = emptyList() // Set empty list in case of failure
            }

        return postsByAddress
    }



    fun updatePost(postId: String, newPost: Post){
        Log.d(tag, "updating post ${postId}, to ${newPost}")
        try{
            val data : MutableMap<String, Any?> = newPost.toHashMap()

            db.collection(COLLECTION_POSTS)
                .document(postId)
                .set(data)
                .addOnSuccessListener { docRef ->
                    Log.d(tag, "Updated ${docRef} to Posts")
                }
                .addOnFailureListener { ex ->
                    Log.e(tag, "Failed to update ${newPost}: $ex")
                }

        }catch (ex : Exception){
            Log.e(tag, "updatePost failed: $ex", )
        }
    }

    fun deletePost(postId: String, authorEmail: String){
            Log.d(tag, "in deletePost ${postId}, ${authorEmail}")
            try {
                db.collection(COLLECTION_USERS)
                    .document(authorEmail)
                    .update("createdPosts", FieldValue.arrayRemove(postId))
                    .addOnSuccessListener {
                        Log.d(tag, "deleted post id ${postId}")

                        db.collection(COLLECTION_POSTS)
                            .document(postId)
                            .delete()
                            .addOnSuccessListener {
                                Log.d(tag, "post ${postId} deleted by ${authorEmail}")
                            }
                            .addOnFailureListener {
                                Log.d(tag, "failed to delete post ${postId}")
                            }
                    }
                    .addOnFailureListener {
                        Log.d(tag, "failed to delete post ${postId}")
                    }

            }catch (ex : Exception){
                Log.e(tag, "unSavePost: Couldn't delete post $ex", )
            }

    }


}