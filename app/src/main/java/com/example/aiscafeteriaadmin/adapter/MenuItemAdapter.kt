package com.example.aiscafeteriaadmin.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aiscafeteriaadmin.databinding.ItemAllitemBinding
import com.example.aiscafeteriaadmin.model.AllMenu
import com.google.firebase.database.DatabaseReference

class MenuItemAdapter(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>,
    databaseReference: DatabaseReference,
    private val onDeleteClickListener:(position : Int) -> Unit
) : RecyclerView.Adapter<MenuItemAdapter.AddItemViewHolder>() {

    private val itemQuantities = IntArray(menuList.size) { 1 }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddItemViewHolder {
        val binding = ItemAllitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddItemViewHolder(binding)
    }


    override fun onBindViewHolder(holder: AddItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuList.size

    inner class AddItemViewHolder(private val binding: ItemAllitemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
//                val quantity = itemQuantities[position]
                val menuItem = menuList[position]
                val uriString = menuItem.foodImage
                val uri = Uri.parse(uriString)

                cartFoodName.text = menuItem.foodName
                cartFoodPrice.text = menuItem.foodPrice
                Glide.with(context).load(uri).into(cartImage)
//                cartItemQuantity.text = quantity.toString()

//                minusButton.setOnClickListener {
//                    deceaseQuantity(position)
//                }
//                plusButton.setOnClickListener {
//                    increaseQuantity(position)
//                }
                deleteButton.setOnClickListener {
                   onDeleteClickListener(position)
                }

            }
        }

//        private fun increaseQuantity(position: Int) {
//            if (itemQuantities[position] < 10) {
//                itemQuantities[position]++
//                binding.cartItemQuantity.text = itemQuantities[position].toString()
//            }
//        }
//
//        private fun deceaseQuantity(position: Int) {
//            if (itemQuantities[position] > 1) {
//                itemQuantities[position]--
//                binding.cartItemQuantity.text = itemQuantities[position].toString()
//            }
//        }

        private fun deleteItem(position: Int) {
            menuList.removeAt(position)
            menuList.removeAt(position)
            menuList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, menuList.size)
        }

    }
}