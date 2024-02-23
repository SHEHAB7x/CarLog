package com.example.carlog.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carlog.databinding.ItemDeviceBinding
import com.example.carlog.utils.Const

class AdapterRecyclerDevices : RecyclerView.Adapter<AdapterRecyclerDevices.Holder>() {

    var list : List<BluetoothDevice>? = null
    var listener : OnItemClickListener? = null

    inner class Holder(private val binding: ItemDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device : BluetoothDevice) {
            binding.deviceName.text = device.name ?: Const.UNKNOWN_DEVICE

        }

        init {
            binding.root.setOnClickListener {
                list?.get(adapterPosition)?.let { device ->
                    listener?.onItemClicked(device)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(list!![position])
    }

    interface OnItemClickListener{
        fun onItemClicked(bluetoothDevice: BluetoothDevice)
    }
}