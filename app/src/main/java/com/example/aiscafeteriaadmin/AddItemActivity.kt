package com.example.aiscafeteriaadmin

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.aiscafeteriaadmin.databinding.ActivityAddItemBinding
import com.example.aiscafeteriaadmin.model.AllMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddItemActivity : AppCompatActivity() {

    // Food Item Details
    private lateinit var foodName: String
    private lateinit var foodPrice: String
    private lateinit var foodDescription: String
    private lateinit var foodIngredient: String
    private var foodImageUri: Uri? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val binding: ActivityAddItemBinding by lazy {
        ActivityAddItemBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.addItemButton.setOnClickListener {
            // Get data from fields
            foodName = binding.addFoodName.text.toString().trim()
            foodPrice = binding.addFoodPrice.text.toString().trim()
            foodDescription = binding.addItemDescription.text.toString().trim()
            foodIngredient = binding.addItemIngredients.text.toString().trim()

            // Validate input fields
            if (foodName.isBlank() || foodPrice.isBlank() || foodDescription.isBlank() || foodIngredient.isBlank()) {
                Toast.makeText(this, "Please fill all the required details.", Toast.LENGTH_SHORT).show()
            } else {
                uploadData()
            }
        }

        binding.addSelectImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun uploadData() {
        // Get a reference to the "menu" node in the database
        val menuRef: DatabaseReference = database.getReference("menu")
        val newItemKey = menuRef.push().key

        if (foodImageUri != null && newItemKey != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("menu_images/$newItemKey.jpg")
            val uploadTask = imageRef.putFile(foodImageUri!!)

            // Upload image and handle success/failure
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Create a new menu item
                    val newItem = AllMenu(
                        newItemKey,
                        foodName = foodName,
                        foodPrice = foodPrice,
                        foodDescription = foodDescription,
                        foodIngredient = foodIngredient,
                        foodImage = downloadUrl.toString()
                    )

                    menuRef.child(newItemKey).setValue(newItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Item added successfully.", Toast.LENGTH_SHORT).show()
                            finish()  // Close activity after upload
                        }
                        .addOnFailureListener { exception ->
                            handleUploadFailure(exception, "Failed to upload item data.")
                        }
                }
            }.addOnFailureListener { exception ->
                handleUploadFailure(exception, "Image upload failed.")
            }
        } else {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUploadFailure(exception: Exception, message: String) {
        // Logging the error for debugging purposes
        exception.printStackTrace()

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            binding.selectedImage.setImageURI(uri)
            foodImageUri = uri
        } else {
            Toast.makeText(this, "Failed to select image. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
