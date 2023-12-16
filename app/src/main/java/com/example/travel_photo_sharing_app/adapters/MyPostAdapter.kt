package com.example.travel_photo_sharing_app.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_photo_sharing_app.R
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.utils.CameraImageHelper


class MyPostAdapter(
    private val postList:MutableList<Post>,
    private val context: Context,
    private val rowClickHandler: (Int) -> Unit,
    private val deleteBtnClickHandler: (Int) -> Unit,
    private val editBtnClickHandler: (Int) -> Unit) : RecyclerView.Adapter<MyPostAdapter.MyPostViewHolder>() {

    inner class MyPostViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) {
        init {
            itemView.setOnClickListener {
                rowClickHandler(adapterPosition)
            }
            itemView.findViewById<Button>(R.id.btnEdit).setOnClickListener {
                editBtnClickHandler(adapterPosition)
            }
            itemView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
                deleteBtnClickHandler(adapterPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_my_post, parent, false)
        return MyPostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: MyPostViewHolder, position: Int) {

        val currPost: Post = postList.get(position)

        val tvAddress = holder.itemView.findViewById<TextView>(R.id.post_address)
        tvAddress.text = currPost.address

        val tvCreated = holder.itemView.findViewById<TextView>(R.id.post_created_time)
        tvCreated.text = currPost.createdAt

        val image = currPost.imageUrl ?: "default_image"
        if(image == "default_image"){
            val res = context.resources.getIdentifier(image, "drawable", context.packageName)
            holder.itemView.findViewById<ImageView>(R.id.post_image).setImageResource(res)
        }
        else{
            val imgBitmap: Bitmap = CameraImageHelper.base64ToBitmap(image)
            holder.itemView.findViewById<ImageView>(R.id.post_image).setImageBitmap(imgBitmap)
        }

    }
}
