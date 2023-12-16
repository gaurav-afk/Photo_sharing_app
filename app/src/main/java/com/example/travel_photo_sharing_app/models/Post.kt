package com.example.travel_photo_sharing_app.models

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import java.io.Serializable
import java.time.LocalDateTime

class Post(
    var address: String,
    var type: String,
//    val author: User,
    val authorEmail: String,
    var description: String,
    var visibleToGuest: Boolean,
    var latitude: Double,
    var longitude: Double,
    var imageUrl: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val idFromDb: String? = null
): Serializable {

    constructor(document: DocumentSnapshot): this(
        document.data!!["address"] as String,
        document.data!!["type"] as String,
        document.data!!["authorEmail"] as String,
        document.data!!["description"] as String,
        document.data!!["visibleToGuest"] as Boolean,
        document.data!!["latitude"].toString().toDouble(),
        document.data!!["longitude"].toString().toDouble(),
        document.data!!["imageUrl"] as String?,
        document.data!!["createdAt"] as String,
        document.id!!
    )
    {
        Log.d("Post", "using constructor ${this}")
    }
    fun matchesQuery(query: String): Boolean {
        val lowerCaseQuery = query.lowercase()

        val matchFound = type.lowercase().contains(lowerCaseQuery) ||
                description.lowercase().contains(lowerCaseQuery) ||
                authorEmail.lowercase().contains(lowerCaseQuery) ||
                address.lowercase().contains(lowerCaseQuery) ||
                matchesNumericQuery(lowerCaseQuery)

        return matchFound
    }

    override fun toString(): String {
        return "Post(${idFromDb}) is $address, $type, $description, $imageUrl"
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        if(other !is Post) return false
        if(this.address != other.address) return false

        return true
    }
    private fun matchesNumericQuery(query: String): Boolean {
        val queryAsNumber = query.toIntOrNull()
        return queryAsNumber != null
    }
    fun toHashMap(): HashMap<String, Any?>{
        val result = HashMap<String, Any?>()

        result["address"] = address
        result["type"] = type
        result["authorEmail"] = authorEmail
        result["description"] = description
        result["visibleToGuest"] = visibleToGuest
        result["latitude"] = latitude
        result["longitude"] = longitude
        result["imageUrl"] = imageUrl
        result["createdAt"] = createdAt

        return result
    }
}
