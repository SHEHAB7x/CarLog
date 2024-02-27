package com.example.carlog.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class BluetoothManager @Inject constructor(private val context: Context) {
    private var bluetoothSocket: BluetoothSocket? = null

    fun connectToDevice(device: BluetoothDevice): BluetoothSocket? {
        // Assuming you already have a BluetoothDevice instance from the user's selection
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(Const.UUID)) // Replace MY_UUID with your actual UUID
            bluetoothSocket?.connect()
            return bluetoothSocket
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

}