package com.example.aiscafeteriaadmin.adapter

import android.content.Context
import android.net.Uri
import android.os.Message
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aiscafeteriaadmin.PendingOrderActivity
import com.example.aiscafeteriaadmin.databinding.PendingOrdersItemBinding

class PendingOrderAdapter(
    private val context: Context,
    private val customerNames: MutableList<String>,
    private val quantity: MutableList<String>,
    private val foodImages: MutableList<String>,
    private val itemClicked: OnItemClicked,

) : RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>() {

interface OnItemClicked {
    fun onItemClickListener(position: Int)
    fun onItemAcceptClickListener(position: Int)
    fun onItemDispatchClickListener(position: Int)
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderViewHolder {
        val binding =
            PendingOrdersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingOrderViewHolder(binding)
    }


    override fun onBindViewHolder(holder: PendingOrderViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = customerNames.size

    inner class PendingOrderViewHolder(private val binding: PendingOrdersItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isAccepted = false
        fun bind(position: Int) {
            binding.apply {
                pendingOrdersName.text = customerNames[position]
                pendingOrdersQuantity.text = quantity[position]

                var uriString = foodImages[position]
                var uri = Uri.parse(uriString)

                Glide.with(context).load(uri).into(pendingOrdersImage)

                pendingOrderAcceptButton.apply {
                    if (isAccepted) {
                        text = "Dispatch"
                    } else {
                        text = "Accept"
                    }
                    setOnClickListener {
                        if (!isAccepted) {
                            text = "Dispatch"
                            isAccepted = true
                            showToast("Order is accepted.")
                            itemClicked.onItemAcceptClickListener(position)

                        } else {
                            customerNames.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            showToast("Order is dispatched.")
                            itemClicked.onItemDispatchClickListener(position)
                        }
                    }
                }

                itemView.setOnClickListener {
                    itemClicked.onItemClickListener(position)
                }

            }
        }

        private fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    }
}