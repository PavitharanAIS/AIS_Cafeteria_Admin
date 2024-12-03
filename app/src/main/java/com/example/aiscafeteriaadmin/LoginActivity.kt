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
import com.example.aiscafeteriaadmin.MainActivity
import com.example.aiscafeteriaadmin.UserDatabaseHelper
import com.example.aiscafeteriaadmin.databinding.ActivityLoginBinding
import com.example.aiscafeteriaadmin.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: UserDatabaseHelper

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = Firebase.auth
        dbHelper = UserDatabaseHelper(this)

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                if (isDeviceOnline(this)) {
                    loginWithFirebase(email, password)
                } else {
                    loginOffline(email, password)
                }
            }
        }

        binding.textViewLoginTextBelowBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
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
            else -> true
        }
    }

    private fun loginWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                val user = dbHelper.getUserByUid(userId)
                if (user != null) {
                    showToast("Login successful.")
                    navigateToHome(user)
                } else {
                    showToast("Local data missing. Please register again.")
                }
            } else {
                showToast("Failed to login with Firebase.")
            }
        }
    }

    private fun loginOffline(email: String, password: String) {
        val user = dbHelper.getUserByEmailAndPassword(email, password)
        if (user != null) {
            showToast("Offline login successful.")
            navigateToHome(user)
        } else {
            showToast("Offline login failed. User not found.")
        }
    }

    private fun navigateToHome(user: UserModel) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_UID", user.uid)
        }
        startActivity(intent)
        finish()
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
