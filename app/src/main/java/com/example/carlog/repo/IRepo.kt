package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.data.ModelUser
import com.example.carlog.network.ResponseState

interface IRepo {
    suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter): ResponseState<List<BluetoothDevice>>
    suspend fun connectToDevice(bluetoothDevice: BluetoothDevice): ResponseState<BluetoothSocket>
    suspend fun getSpeed(bluetoothSocket: BluetoothSocket): ResponseState<Int>
    suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<Int>
    suspend fun login(email: String, password: String): ResponseState<ModelUser>
    suspend fun postTrip(
        date: String,
        acceleration: Int,
        deceleration: Int,
        tripTime: String,
        idling: Int,
        overSpeedTimes: Int,
        tripRate: Int,
        maxSpeed: Int,
        maxAcceleration: Int,
        maxBreaking: Int,
        maxIdling: Int,
    ): ResponseState<Int>
}