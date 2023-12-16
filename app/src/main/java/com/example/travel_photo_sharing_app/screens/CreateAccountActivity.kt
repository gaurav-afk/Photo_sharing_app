package com.example.travel_photo_sharing_app.screens

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.travel_photo_sharing_app.databinding.ActivityCreateAccountBinding
import com.example.travel_photo_sharing_app.models.User
import com.example.travel_photo_sharing_app.utils.AuthenticationHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreateAccountActivity : LoginActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var authenticationHelper: AuthenticationHelper
    override val tag = "Create Account"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createAccountBtn.setOnClickListener {
            val username: String = this.binding.usernameInput.text.toString()
            val email: String = this.binding.emailInput.text.toString()
            val password: String = this.binding.passwordInput.text.toString()
            val confirmPassword: String = this.binding.confirmPasswordInput.text.toString()

            // configure shared preferences
            this.sharedPreferences = getSharedPreferences("USERS", MODE_PRIVATE)

            this.authenticationHelper = AuthenticationHelper(this)

            // check if the username has already been used
            val userJson = sharedPreferences.getString(username, "")
            val userAlreadyExist = if(userJson == "") false else true
            Log.i(tag, "user exist ? ${userJson} ${userAlreadyExist}")

            if(username == "") {
                Snackbar.make(binding.root, "Please enter a user name", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(email == "") {
                Snackbar.make(binding.root, "Please enter an email address", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(password == "" || confirmPassword == "") {
                Snackbar.make(binding.root, "Please enter a password and the confirm password", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(userAlreadyExist) {
                Snackbar.make(binding.root, "User already exist!", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // check if the confirm password matches the password
            if(confirmPassword != password) {
                Snackbar.make(binding.root, "Confirm Password does not match the password", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val newUser = User(email, password, username)

            lifecycleScope.launch {
                authenticationHelper.signUp(newUser)
            }
        }
    }

}