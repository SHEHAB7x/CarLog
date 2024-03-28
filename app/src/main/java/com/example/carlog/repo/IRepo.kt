package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState
import com.github.eltonvs.obd.command.ObdResponse

interface IRepo {
    suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter) : ResponseState<List<BluetoothDevice>>
    suspend fun connectToDevice(bluetoothDevice: BluetoothDevice) : ResponseState<BluetoothSocket>
    suspend fun getSpeed(bluetoothSocket: BluetoothSocket) : ResponseState<ObdResponse>
    suspend fun getRPM(bluetoothSocket: BluetoothSocket) : ResponseState<ObdResponse>
}