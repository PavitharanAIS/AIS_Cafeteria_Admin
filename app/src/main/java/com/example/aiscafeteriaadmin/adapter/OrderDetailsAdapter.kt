package com.example.aiscafeteriaadmin.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aiscafeteriaadmin.databinding.OrderdetailsItemsBinding
import java.util.ArrayList

class OrderDetailsAdapter(
    private var context: Context,
    private var foodNames: ArrayList<String>,
    private var foodImages: ArrayList<String>,
    private var foodQuantities: ArrayList<Int>,
    private var foodPrices: ArrayList<String>,
):RecyclerView.Adapter<OrderDetailsAdapter.OrderDetailsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val binding = OrderdetailsItemsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OrderDetailsViewHolder(binding)
    }



    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = foodNames.size

    inner class OrderDetailsViewHolder(private val binding : OrderdetailsItemsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding
                .apply {
                    orderDetailsFoodName.text = foodNames[position]
                    orderDetailsFoodQuantity.text = foodQuantities[position].toString()
                    val uriString = foodImages[position]
                    val uri = Uri.parse(uriString)
                    Glide.with(context).load(uri).into(orderDetailsFoodImage)
                    orderDetailsFoodPrice.text = foodPrices[position]
                }
        }
    }

}