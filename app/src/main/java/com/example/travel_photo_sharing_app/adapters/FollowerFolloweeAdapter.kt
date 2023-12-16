package com.example.travel_photo_sharing_app.adapters;

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_photo_sharing_app.R
import com.example.travel_photo_sharing_app.models.Post
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.utils.tag

class FollowerFolloweeAdapter(
    private var userList: MutableList<User>,
    private val unfollowBtnClickHandler: ((String) -> Unit)?,
) : RecyclerView.Adapter<FollowerFolloweeAdapter.UserViewHolder>() {
        private val tag = "Followee/Follower Adapter"

        inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder (itemView) {
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_follower_followee, parent, false)
            return UserViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userList.size
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

            val currUser: User = userList.get(position)
            Log.d(tag, "onbind currUser ${currUser}")
            val username_tv = holder.itemView.findViewById<TextView>(R.id.username)
            username_tv.text = currUser.username

            val email_tv = holder.itemView.findViewById<TextView>(R.id.user_email)
            email_tv.text = currUser.email

            Log.d(tag, "handler is null ${unfollowBtnClickHandler != null}")
            if(unfollowBtnClickHandler != null){
                holder.itemView.findViewById<Button>(R.id.unfollow_btn).visibility = View.VISIBLE
                holder.itemView.findViewById<Button>(R.id.unfollow_btn).setOnClickListener {
                    Log.d(tag, "unfollow btn clicked")
                    unfollowBtnClickHandler!!(currUser.email)

                    userList.removeAt(position)
                    this.notifyDataSetChanged()
                }
            }
            else {
                holder.itemView.findViewById<Button>(R.id.unfollow_btn).visibility = View.GONE
            }


        }
    }
