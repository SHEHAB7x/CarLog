package com.example.carlog.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carlog.data.ModelAllTripsItem
import com.example.carlog.databinding.ItemHistoryBinding

class AdapterRecyclerTrips () :
    RecyclerView.Adapter<AdapterRecyclerTrips.Holder>() {
    var list: List<ModelAllTripsItem>? = null
    var listener : OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val data = list?.get(position)
        holder.bind(data!!)
    }

    inner class Holder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: ModelAllTripsItem) {
            binding.date.text = trip.date
            binding.rate.text = trip.tripRate.toString()
        }

        init {
            binding.root.setOnClickListener{
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    list?.get(position).let {
                        listener?.onItemClick(it!!)
                    }
                }
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(user: ModelAllTripsItem)
    }
}