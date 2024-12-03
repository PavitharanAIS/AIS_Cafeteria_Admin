package com.example.aiscafeteriaadmin


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aiscafeteriaadmin.UserDatabaseHelper
import com.example.aiscafeteriaadmin.databinding.ActivityCreateUserBinding
import com.example.aiscafeteriaadmin.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CreateUserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var dbHelper: UserDatabaseHelper

    private val binding: ActivityCreateUserBinding by lazy {
        ActivityCreateUserBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = Firebase.auth
        database = Firebase.database.reference
        dbHelper = UserDatabaseHelper(this)

        binding.createAccountButton.setOnClickListener {
            val email = binding.registerEmail.text.toString().trim()
            val username = binding.registerName.text.toString().trim()
            val password = binding.registerPassword.text.toString().trim()

            if (validateInputs(username, email, password)) {
                if (isDeviceOnline(this)) {
                    registerUser(username, email, password)
                } else {
                    showToast("Internet connection is required for registration.")
                }
            }
        }
    }

    private fun validateInputs(username: String, email: String, password: String): Boolean {
        return when {
            username.isBlank() -> {
                showToast("Please enter a username.")
                false
            }
            email.isBlank() -> {
                showToast("Please enter an email address.")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Please enter a valid email address.")
                false
            }
            password.isBlank() -> {
                showToast("Please enter a password.")
                false
            }
            password.length < 6 -> {
                showToast("Password must be at least 6 characters long.")
                false
            }
            else -> true
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                val user = UserModel(name = username, email = email, password = password, uid = userId)

                // Save to Firebase
                database.child("user").child(userId).setValue(user).addOnCompleteListener { dbTask ->
                    if (dbTask.isSuccessful) {
                        // Save to SQLite
                        dbHelper.addUser(user)
                        showToast("Registration successful.")
                    } else {
                        showToast("Failed to save user data to Firebase.")
                    }
                }
            } else {
                handleAuthError(task.exception)
            }
        }
    }

    private fun handleAuthError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthException -> showToast("Error: ${exception.message}")
            else -> showToast("Registration failed. Please try again.")
        }
    }

    private fun isDeviceOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

