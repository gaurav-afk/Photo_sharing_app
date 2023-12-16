package com.example.travel_photo_sharing_app.screens

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.travel_photo_sharing_app.databinding.ActivityLoginBinding
import com.example.travel_photo_sharing_app.MainActivity
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.utils.AuthenticationHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

open class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var prefEditor: SharedPreferences.Editor
    open val tag = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // configure shared preferences
        this.sharedPreferences = getSharedPreferences("USERS", MODE_PRIVATE)
        this.prefEditor = this.sharedPreferences.edit()

        val isFromMain = this@LoginActivity.intent.extras != null &&
                        this@LoginActivity.intent.extras!!.containsKey("REFERER") &&
                        this@LoginActivity.intent.getStringExtra("REFERER") == "MainActivity"
        if(isFromMain){
            Log.i(tag, "login needed")
            this@LoginActivity.intent.removeExtra("REFERER")
            Snackbar.make(binding.root, "Please login for further actions.", Snackbar.LENGTH_LONG).show()
        }

        binding.loginBtn.setOnClickListener {
            val email = this.binding.emailInput.text.toString()
            val password = this.binding.passwordInput.text.toString()

            lifecycleScope.launch {
                AuthenticationHelper.instance!!.signIn(email, password)
            }
        }

        binding.signUpBtn.setOnClickListener {
            val intent = Intent(this@LoginActivity, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

}