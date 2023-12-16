package com.example.travel_photo_sharing_app.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.example.travel_photo_sharing_app.databinding.ActivityAddPostBinding
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.repositories.PostRepository
import com.example.travel_photo_sharing_app.utils.AuthenticationHelper
import com.example.travel_photo_sharing_app.utils.CameraImageHelper
import com.example.travel_photo_sharing_app.utils.LocationHelper
import com.example.travel_photo_sharing_app.utils.getCategorySpinnerList
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class AddPostActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddPostBinding
    private var loggedInUser: User? = null
    private var postRepository = PostRepository()
    private lateinit var locationHelper: LocationHelper

    val tag = "Add Post"
    private val TAKE_PHOTO = 1
    private var base64UploadedImg: String? = null
    private var selectedPost: Post? = null

    var savedPosts: MutableList<Post> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationHelper = LocationHelper(applicationContext, this)

        binding.longitude.visibility = View.GONE
        binding.latitude.visibility = View.GONE
        binding.imageTaken.visibility = View.GONE

        AuthenticationHelper.instance!!.loggedInUser.observe(this) {user ->
            loggedInUser = user
        }
        Log.i(tag, "In AddPost, user: ${loggedInUser}")

        binding.addressCoordinatesSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // If the switch is ON, make the address field invisible
                binding.address.visibility = View.GONE
                binding.longitude.visibility = View.VISIBLE
                binding.latitude.visibility = View.VISIBLE
            } else {
                // If the switch is OFF, make the address field visible
                binding.address.visibility = View.VISIBLE
                binding.longitude.visibility = View.GONE
                binding.latitude.visibility = View.GONE
            }
        }

        var postId = intent.getStringExtra("POST")
        Log.d(tag, "postid in add post ${postId}")
        if (postId != null) {
            lifecycleScope.launch {
                postId = intent.getStringExtra("POST")!!
                Log.d(tag, "add post intent post id is ${postId}")

                selectedPost = postRepository.getPostById(postId!!)
                Log.i(tag, "selected post here $selectedPost")

                binding.address.setText(selectedPost!!.address)
                binding.categorySpinner.setSelection(getCategorySpinnerList(this@AddPostActivity).indexOf(selectedPost!!.type))
                binding.description.setText(selectedPost!!.description)
                binding.visibleToGuest.isChecked = selectedPost!!.visibleToGuest

                if(selectedPost!!.imageUrl != null){
                    binding.imageTaken.setImageBitmap(CameraImageHelper.base64ToBitmap(selectedPost!!.imageUrl!!))
                    binding.imageTaken.visibility = View.VISIBLE
                }
            }
        }

        this.binding.btnUploadPhoto.setOnClickListener {
//            val intent = Intent(this, CameraActivity::class.java)
            Log.d(tag, "clicked upload btn")
            if (hasPermissions() == false) {
                ActivityCompat.requestPermissions(this, CameraActivity.CAMERAX_PERMISSIONS, 0)
            }
            else{
                Log.d(tag, "camera opened")
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, TAKE_PHOTO)
            }
        }

        this.binding.saveBtn.setOnClickListener {
            this.saveData(selectedPost)
        }

        this.binding.cancelBtn.setOnClickListener {
            finish()
        }

        this.binding.currentLocationBtn.setOnClickListener {
            locationHelper.getCurrentLocation()
            locationHelper.currentLocation.observe(this) { currLocation ->
                val latitude: Double = currLocation.latitude
                val longitude: Double = currLocation.longitude
                binding.latitude.setText(latitude.toString())
                binding.longitude.setText(longitude.toString())
                binding.address.setText(locationHelper.coordinatesToAddress(latitude, longitude))
            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            if (requestCode == TAKE_PHOTO) {

                Log.d(tag, "return from camera ${data!!.extras!!["data"]}")
                val bitmap: Bitmap = data!!.extras!!["data"] as Bitmap

                binding.imageTaken.setImageBitmap(bitmap);
                binding.imageTaken.visibility = View.VISIBLE

                //upload
                val base64_img = CameraImageHelper.bitmapToBase64(bitmap)
                Log.d(tag, "encoded image is ${base64_img}")
                base64UploadedImg = base64_img
            }
        }

    }
    private fun saveData(selectedPost: Post?) {
        Log.d(tag, "saving address ${binding.address.text.toString()}, ${binding.latitude.text.toString().isNullOrEmpty()}, ${binding.longitude.text.toString()::class.java}")
        val userCoordinates: Boolean = binding.addressCoordinatesSwitch.isChecked
        var address: String? = null
        var latitude: Double? = null
        var longitude: Double? = null
        if(userCoordinates){
            try{
                latitude = binding.latitude.text.toString().toDouble()
                longitude = binding.latitude.text.toString().toDouble()
                Log.d(tag, "using coors ${latitude}, ${longitude}")
                address = locationHelper.coordinatesToAddress(latitude, longitude)
                Log.d(tag, "address ${address}")
            }
            catch(ex: Exception){
                Snackbar.make(binding.root, "Invalid coordinates", Snackbar.LENGTH_LONG).show()
                return
            }

        }
        else{
            try{
                address = binding.address.text.toString()
                Log.d(tag, "using address ${address}")
                val coordinates = locationHelper.addressToCoordinates(address)
                latitude = coordinates!!.latitude.toDouble()
                longitude = coordinates!!.longitude.toDouble()
                Log.d(tag, "coors ${latitude}, ${longitude}")
            }
            catch(ex: Exception){
                Snackbar.make(binding.root, "Invalid address", Snackbar.LENGTH_LONG).show()
                return
            }
        }

        var type: String? = this.binding.categorySpinner.selectedItem.toString()
        var authorEmail: String? = loggedInUser!!.email
        var desc: String? = this.binding.description.text.toString()
        var visibleToGuest: Boolean? = this.binding.visibleToGuest.isChecked
        var base64Img: String? = CameraImageHelper.bitmapToBase64(this.binding.imageTaken.drawable.toBitmap())
        Log.d(tag, "img in savedata, ${base64Img}")
        Log.d(tag, "spinner value is ${type}")
        // error check
        var hasEmptyField = false
        if (address.isNullOrEmpty()) {
            this.binding.address.setError("Address cannot be empty")
            hasEmptyField = true
        }
        if (latitude == null || latitude.isNaN()) {
            this.binding.latitude.setError("Latitude cannot be empty")
            hasEmptyField = true
        }
        if (longitude == null || longitude.isNaN()) {
            this.binding.longitude.setError("Longitude cannot be empty")
            hasEmptyField = true
        }

        if (desc.isNullOrEmpty()) {
            this.binding.description.setError("Description cannot be empty")
            hasEmptyField = true
        }
        if(hasEmptyField){
            Snackbar.make(binding.addPostParentLayout, "All fields are required.", Snackbar.LENGTH_LONG).show()
            return
        }

        val index = intent.getIntExtra("INDEX", -1)
        Log.i(tag, "selected post ${index}: ${selectedPost}")

        // update post
        if(selectedPost != null){
            Log.d(tag, "postId is ${selectedPost.idFromDb}")
            val postToEdit = selectedPost
            postToEdit.address = address!!
            postToEdit.type = type!!
            postToEdit.description = desc!!
            postToEdit.visibleToGuest = visibleToGuest!!
            postToEdit.latitude = latitude!!
            postToEdit.longitude = longitude!!
            postToEdit.imageUrl = base64Img!!
            Log.d(tag, "post to update is ${postToEdit}")

            postRepository.updatePost(selectedPost.idFromDb!!, postToEdit)
        }
        // create new post
        else {
            var postToAdd = Post(address!!, type!!, authorEmail!!, desc!!, visibleToGuest!!, latitude!!, longitude!!, base64Img!!)
            postRepository.addPost(postToAdd)
        }
        Snackbar.make(binding.addPostParentLayout, "Data Saved", Snackbar.LENGTH_LONG).show()
        finish()
    }

    private fun hasPermissions():Boolean {
        return CameraActivity.CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


}