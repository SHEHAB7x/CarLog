package com.example.carlog.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carlog.data.Message
import com.example.carlog.data.ModelMessages
import com.example.carlog.databinding.ItemChatBinding

class AdapterRecyclerMessages(
    var modelMessages: ModelMessages? = null
) : RecyclerView.Adapter<AdapterRecyclerMessages.Holder>() {

    var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return modelMessages?.getMessages?.flatMap { it.messages }?.size ?: 0
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val message = modelMessages?.getMessages?.flatMap { it.messages }?.get(position)
        holder.bind(message)
    }

    inner class Holder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message?) {
            message?.let {
                binding.message.text = it.body
            }
        }

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    modelMessages?.let {
                        listener?.onItemClick(it)
                    }
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(modelMessages: ModelMessages)
    }
}
