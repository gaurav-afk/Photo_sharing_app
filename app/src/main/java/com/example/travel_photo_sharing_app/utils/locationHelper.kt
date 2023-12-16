package com.example.travel_photo_sharing_app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class LocationHelper(val applicationContext: Context, val activity: AppCompatActivity) {
        private val tag = "LocationHelper"
        var locationPermissionGranted = false
        val REQUEST_LOCATION_CODE = 101
        private var fusedLocationProviderClient: FusedLocationProviderClient
        private val locationRequest: LocationRequest
        private val UPDATE_INTERVAL_IN_MILLISECONDS = 50000 //milliseconds
        private val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())
        var currentLocation = MutableLiveData<Location>()

        init {
            locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                UPDATE_INTERVAL_IN_MILLISECONDS.toLong()).build()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        }

        private val APP_PERMISSIONS_LIST = arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        private fun hasFineLocationPermission(context: Context): Boolean {
            val permissions = ContextCompat.checkSelfPermission(context.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            return permissions == PackageManager.PERMISSION_GRANTED
        }

        private fun hasCoarseLocationPermission(context: Context): Boolean {
            val permissions = ContextCompat.checkSelfPermission(context.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            return permissions == PackageManager.PERMISSION_GRANTED
        }


        fun checkPermissions(context: Context) {
            if (hasFineLocationPermission(context) == true && hasCoarseLocationPermission(context) == true) {
                this.locationPermissionGranted = true
            }
            Log.d(tag, "checkPermissions: Are location permissions granted? : " + this.locationPermissionGranted)

            if (this.locationPermissionGranted == false) {
                Log.d(tag, "Permissions not granted, so requesting permission now...")
                multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            }

        }

        private val multiplePermissionsResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { resultsList ->
            locationPermissionGranted = true
            for (item in resultsList.entries) {
                if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                    locationPermissionGranted = false
                }
            }
            if(locationPermissionGranted == true){
                getCurrentLocation()
            }
        }

        @SuppressLint("MissingPermission")
        fun getCurrentLocation(){
            // Before running fusedLocationClient.lastLocation, CHECK that the user gave you permission for FINE_LOCATION and COARSE_LOCATION
            if(locationPermissionGranted == false){
                checkPermissions(applicationContext)
            }

            fusedLocationProviderClient!!.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location === null) {
                        Log.d(tag, "Location is null")
                    }
                    Log.d(tag, "The device is located at: ${location?.latitude}, ${location?.longitude}")
                    currentLocation.postValue(location)
                }
        }

        fun addressToCoordinates(address: String): Address? {
            try {
                val searchResults:MutableList<Address>? = geocoder.getFromLocationName(address, 1)

                if (searchResults == null || searchResults.size == 0) {
                    Log.d(tag, "No results found")
                    return null
                } else {
                    val foundLocation: Address = searchResults.get(0)
                    Log.d(tag, "Coordinates of ${address} are: ${foundLocation.latitude}, ${foundLocation.longitude}")
                    return foundLocation
                }
            } catch(ex:Exception) {
                Log.e(tag, "Error encountered while getting coordinate location.")
                Log.e(tag, ex.toString())
                return null
            }
        }

        fun coordinatesToAddress(latitude: Double?, longitude: Double?) : String? {
            if(latitude == null || longitude == null){
                return ""
            }
            try {
                val searchResults:MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (searchResults == null || searchResults.size == 0) {
                    Log.d(tag, "No results found")
                    return null
                } else {
                    val foundLocation: Address = searchResults.get(0)
                    Log.d(tag, "coor ${latitude}, ${longitude} address is \"${foundLocation.subThoroughfare} ${foundLocation.thoroughfare}, ${foundLocation.locality}, ${foundLocation.adminArea}, ${foundLocation.countryName}")
                    return foundLocation.locality
                }
            } catch(ex:Exception) {
                Log.e(tag, "Error encountered while getting coordinate location.")
                Log.e(tag, ex.toString())
                return null
            }
        }
}