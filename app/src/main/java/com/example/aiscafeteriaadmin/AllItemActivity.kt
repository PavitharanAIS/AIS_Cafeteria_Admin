package com.example.aiscafeteriaadmin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aiscafeteriaadmin.adapter.MenuItemAdapter
import com.example.aiscafeteriaadmin.databinding.ActivityAllItemBinding
import com.example.aiscafeteriaadmin.model.AllMenu
import com.google.firebase.database.*

class AllItemActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private var menuItems: ArrayList<AllMenu> = ArrayList()

    private val binding: ActivityAllItemBinding by lazy {
        ActivityAllItemBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        // Set up UI actions
        binding.backButton.setOnClickListener { finish() }

        // Retrieve menu items
        retrieveMenuItems()
    }

    private fun retrieveMenuItems() {
        val foodRef: DatabaseReference = databaseReference.child("menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing data before populating
                menuItems.clear()

                if (!snapshot.exists()) {
                    Toast.makeText(this@AllItemActivity, "No menu items available.", Toast.LENGTH_SHORT).show()
                    return
                }

                // Loop through each menu item
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let { menuItems.add(it) }
                }

                // Set up RecyclerView adapter
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseError", "Error retrieving data: ${error.message}", error.toException())
                Toast.makeText(this@AllItemActivity, "Failed to retrieve data. Try again later.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setAdapter() {
        if (menuItems.isEmpty()) {
            Toast.makeText(this, "No menu items to display.", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = MenuItemAdapter(this, menuItems, databaseReference) { position ->
            deleteMenuItem(position)
        }

        binding.menuRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.menuRecyclerView.adapter = adapter
    }

    private fun deleteMenuItem(position: Int) {
        val menuItemToDelete = menuItems[position]
        val menuItemKey = menuItemToDelete.key

        if (menuItemKey.isNullOrEmpty()) {
            Toast.makeText(this, "Failed to identify the item to delete.", Toast.LENGTH_SHORT).show()
            return
        }

        val foodMenuReference = databaseReference.child("menu").child(menuItemKey)

        foodMenuReference.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    menuItems.removeAt(position)
                    binding.menuRecyclerView.adapter?.notifyItemRemoved(position)
                    Toast.makeText(this, "Item deleted successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete item. Try again.", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteError", "Failed to delete item: ${task.exception?.message}", task.exception)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error deleting item: ${exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("DeleteError", "Exception while deleting item: ${exception.localizedMessage}", exception)
            }
    }
}
