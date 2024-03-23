package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState

interface IRepo {
    suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter) : ResponseState<List<BluetoothDevice>>
    suspend fun connectToDevice(bluetoothDevice: BluetoothDevice) : ResponseState<BluetoothSocket>
    suspend fun getSpeed(bluetoothSocket: BluetoothSocket) : ResponseState<Int>
}