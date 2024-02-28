package com.example.carlog.utils

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class MyBluetoothManager @Inject constructor(private val context: Context) {
    private var bluetoothSocket: BluetoothSocket? = null
    fun setBluetoothSocket(socket: BluetoothSocket) {
        bluetoothSocket = socket
    }

    fun getBluetoothSocket(): BluetoothSocket? {
        return bluetoothSocket
    }

}