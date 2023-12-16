package com.example.travel_photo_sharing_app.models

import android.util.Log
import java.io.Serializable

open class User(
    val email: String,
    val password: String,
    val username: String,
    val savedPosts: MutableList<String> = mutableListOf(),
    val createdPosts: MutableList<String> = mutableListOf(),
    val followedBy: MutableList<String> = mutableListOf(), // use email as the id
    val following: MutableList<String> = mutableListOf(),
): Serializable {

    constructor(document: MutableMap<String, Any>): this(
        document["email"] as String,
        document["password"] as String,
        document["username"] as String,
        document["savedPosts"] as MutableList<String>,
        document["createdPosts"] as MutableList<String>,
        document["followedBy"] as MutableList<String>,
        document["following"] as MutableList<String>
        )
    {
        Log.d("User", "using constructor")
    }

    override fun toString(): String {
        return "User is ($email, $username, $password, ${followedBy}, ${following}, ${createdPosts}, $savedPosts)"
    }
}