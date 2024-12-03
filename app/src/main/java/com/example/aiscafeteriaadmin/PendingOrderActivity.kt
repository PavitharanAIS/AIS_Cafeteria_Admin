package com.example.aiscafeteriaadmin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aiscafeteriaadmin.adapter.DeliveryAdapter
import com.example.aiscafeteriaadmin.adapter.PendingOrderAdapter
import com.example.aiscafeteriaadmin.databinding.ActivityPendingOrderBinding
import com.example.aiscafeteriaadmin.model.OrderDetails
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PendingOrderActivity : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {
    private lateinit var binding: ActivityPendingOrderBinding

    private var listOfName: MutableList<String> = mutableListOf()
    private var listOfTotalPrice: MutableList<String> = mutableListOf()
    private var listOfImageFirstFoodOrder: MutableList<String> = mutableListOf()
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseOrderDetails: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialization of database
        database = FirebaseDatabase.getInstance()
        // Initialization of database reference
        databaseOrderDetails = database.reference.child("OrderDetails")

        try {
            getOrderDetails()
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error retrieving order details", e)
            Toast.makeText(this, "Failed to load orders. Please try again later.", Toast.LENGTH_SHORT).show()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun getOrderDetails() {
        // Retrieve order details from Firebase database
        databaseOrderDetails.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    for (orderSnapshot in snapshot.children) {
                        val orderDetails = orderSnapshot.getValue(OrderDetails::class.java)
                        orderDetails?.let {
                            listOfOrderItem.add(it)
                        }
                    }
                    addDataToListForRecyclerView()
                } catch (e: Exception) {
                    Log.e("PendingOrderActivity", "Error parsing order details", e)
                    Toast.makeText(this@PendingOrderActivity, "Failed to process orders.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PendingOrderActivity", "Database error: ${error.message}", error.toException())
                Toast.makeText(this@PendingOrderActivity, "Failed to load orders: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addDataToListForRecyclerView() {
        try {
            for (orderItem in listOfOrderItem) {
                // Add data to respective list for populating the RecyclerView
                orderItem.userName?.let { listOfName.add(it) }
                orderItem.totalPrice?.let { listOfTotalPrice.add(it) }
                orderItem.foodImages?.filterNot { it.isEmpty() }?.forEach {
                    listOfImageFirstFoodOrder.add(it)
                }
            }
            setAdapter()
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error adding data to RecyclerView lists", e)
            Toast.makeText(this, "Error displaying orders.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAdapter() {
        try {
            binding.pendingOrderRecyclerView.layoutManager = LinearLayoutManager(this)
            val adapter =
                PendingOrderAdapter(this, listOfName, listOfTotalPrice, listOfImageFirstFoodOrder, this)
            binding.pendingOrderRecyclerView.adapter = adapter
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error setting RecyclerView adapter", e)
            Toast.makeText(this, "Failed to display orders.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClickListener(position: Int) {
        try {
            val intent = Intent(this, OrderDetailsActivity::class.java)
            val userOrderDetails = listOfOrderItem[position]
            intent.putExtra("userOrderDetails", userOrderDetails)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error handling item click", e)
            Toast.makeText(this, "Failed to open order details.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemAcceptClickListener(position: Int) {
        try {
            val childItemPushKey = listOfOrderItem[position].itemPushKey
            val clickItemOrderReference = childItemPushKey?.let {
                database.reference.child("OrderDetails").child(it)
            }

            clickItemOrderReference?.child("orderAccepted")?.setValue(true)
                ?.addOnSuccessListener {
                    updateOrderAcceptStatus(position)
                }?.addOnFailureListener { e ->
                    Log.e("PendingOrderActivity", "Error accepting order", e)
                    Toast.makeText(this, "Failed to accept order.", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error handling order acceptance", e)
            Toast.makeText(this, "Failed to process order acceptance.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemDispatchClickListener(position: Int) {
        try {
            val dispatchItemPushKey = listOfOrderItem[position].itemPushKey
            val dispatchItemOrderReference = database.reference.child("CompletedOrder").child(dispatchItemPushKey!!)
            dispatchItemOrderReference.setValue(listOfOrderItem[position])
                .addOnSuccessListener {
                    deleteThisItemFromOrderDetails(dispatchItemPushKey)
                }
                .addOnFailureListener { e ->
                    Log.e("PendingOrderActivity", "Error dispatching order", e)
                    Toast.makeText(this, "Failed to dispatch order.", Toast.LENGTH_SHORT).show()
                }
            dispatchItemOrderReference.child("orderAccepted").setValue(true)
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error handling order dispatch", e)
            Toast.makeText(this, "Failed to process order dispatch.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteThisItemFromOrderDetails(dispatchItemPushKey: String) {
        try {
            val orderDetailsItemsReference =
                database.reference.child("OrderDetails").child(dispatchItemPushKey)
            orderDetailsItemsReference.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Order is Dispatched.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("PendingOrderActivity", "Error removing dispatched order", e)
                    Toast.makeText(this, "Failed to remove order from database.", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error handling order removal", e)
            Toast.makeText(this, "Failed to process order removal.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateOrderAcceptStatus(position: Int) {
        try {
            val userIdOfClickedItem = listOfOrderItem[position].userUid
            val pushKeyOfClickedItem = listOfOrderItem[position].itemPushKey
            val buyHistoryReference = database.reference.child("user").child(userIdOfClickedItem!!).child("OrderHistory").child(pushKeyOfClickedItem!!)
            buyHistoryReference.child("orderAccepted").setValue(true)
                .addOnFailureListener { e ->
                    Log.e("PendingOrderActivity", "Error updating order acceptance in user history", e)
                    Toast.makeText(this, "Failed to update order acceptance in user history.", Toast.LENGTH_SHORT).show()
                }
            databaseOrderDetails.child(pushKeyOfClickedItem).child("orderAccepted").setValue(true)
                .addOnFailureListener { e ->
                    Log.e("PendingOrderActivity", "Error updating order acceptance in OrderDetails", e)
                    Toast.makeText(this, "Failed to update order acceptance in OrderDetails.", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("PendingOrderActivity", "Error updating order acceptance", e)
            Toast.makeText(this, "Failed to process order acceptance update.", Toast.LENGTH_SHORT).show()
        }
    }
}
