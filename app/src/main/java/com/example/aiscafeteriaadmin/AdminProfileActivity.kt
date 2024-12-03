package com.example.aiscafeteriaadmin

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aiscafeteriaadmin.databinding.ActivityAdminProfileBinding
import com.example.aiscafeteriaadmin.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminProfileActivity : AppCompatActivity() {

    private val binding: ActivityAdminProfileBinding by lazy {
        ActivityAdminProfileBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var adminReference: DatabaseReference
    private lateinit var userDatabaseHelper: UserDatabaseHelper
    private lateinit var networkReceiver: NetworkReceiver
    private var isNetworkAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        adminReference = FirebaseDatabase.getInstance().reference.child("user")
        userDatabaseHelper = UserDatabaseHelper(this)

        // Initialize the NetworkReceiver
        networkReceiver = NetworkReceiver {
            syncLocalDataWithFirebase()  // Sync when the network is available
        }

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        binding.backButton.setOnClickListener { finish() }
        binding.saveInformationButton.setOnClickListener { updateUserData() }

        setupProfileEditState(isEditable = false)
        binding.profileEditButton.setOnClickListener {
            val isCurrentlyEditable = binding.profileName.isEnabled
            setupProfileEditState(isEditable = !isCurrentlyEditable)
        }

        loadUserProfile()
    }

    private fun setupProfileEditState(isEditable: Boolean) {
        binding.profileName.isEnabled = isEditable
        binding.profileAddress.isEnabled = isEditable
        binding.profileEmail.isEnabled = isEditable
        binding.profilePhone.isEnabled = isEditable
        binding.profilePassword.isEnabled = isEditable
        binding.saveInformationButton.isEnabled = isEditable

        if (isEditable) binding.profileName.requestFocus()
    }

    private fun loadUserProfile() {
        val currentUserUid = auth.currentUser?.uid ?: return showToast("User not logged in.")

        if (isNetworkAvailable) {
            Log.d("NetworkAvailabilityCondition","$isNetworkAvailable")

            adminReference.child(currentUserUid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            userDatabaseHelper.updateUser(user)
                            displayUserProfile(user)
                        }
                    } else {
                        showToast("No data found for user.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to fetch user data: ${error.message}")
                }
            })
        } else {
            Log.d("NetworkAvailability","False")
            val user = userDatabaseHelper.getUserByUid(currentUserUid)
            if (user != null) {
                displayUserProfile(user)
            } else {
                showToast("No local data found for user.")
            }
        }
    }

    private fun displayUserProfile(user: UserModel) {
        binding.profileName.setText(user.name ?: "")
        binding.profileEmail.setText(user.email ?: "")
        binding.profilePassword.setText(user.password ?: "")
        binding.profilePhone.setText(user.phone ?: "")
        binding.profileAddress.setText(user.address ?: "")
    }

    private fun updateUserData() {
        val currentUserUid = auth.currentUser?.uid ?: return showToast("Failed to update profile. User not logged in.")

        val updatedUser = UserModel(
            uid = currentUserUid,
            name = binding.profileName.text.toString(),
            email = binding.profileEmail.text.toString(),
            password = binding.profilePassword.text.toString(),
            phone = binding.profilePhone.text.toString(),
            address = binding.profileAddress.text.toString()
        )

        if (isNetworkAvailable) {
            adminReference.child(currentUserUid).setValue(updatedUser).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Profile updated successfully.")
                    userDatabaseHelper.updateUser(updatedUser)
                } else {
                    showToast("Failed to update profile in Firebase.")
                }
            }
        } else {
            val rowsUpdated = userDatabaseHelper.updateUser(updatedUser)
            if (rowsUpdated > 0) {
                showToast("Profile updated locally.")
            } else {
                showToast("Failed to update profile locally.")
            }
        }
    }

    private fun syncLocalDataWithFirebase() {
        val currentUserUid = auth.currentUser?.uid ?: return
        val user = userDatabaseHelper.getUserByUid(currentUserUid)

        if (user != null) {
            adminReference.child(currentUserUid).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Profile synced with Firebase.")
                } else {
                    showToast("Failed to sync profile with Firebase.")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}










