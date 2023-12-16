package com.example.travel_photo_sharing_app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

class CameraImageHelper {
    private val tag = "CameraImageHelper"
    companion object {
        fun base64ToBitmap(base64: String): Bitmap {
            Log.d(tag, "string to be decoded ${base64}")
            val decodedImg: ByteArray = Base64.decode(base64, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedImg, 0, decodedImg.size)
        }
        fun bitmapToBase64(bitmap: Bitmap): String {
            val byteOutStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteOutStream)
            return Base64.encodeToString(byteOutStream.toByteArray(), Base64.DEFAULT)
        }
    }
}