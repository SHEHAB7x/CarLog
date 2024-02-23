package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import retrofit2.Response
import java.util.UUID
import javax.inject.Inject

class Repo
    @Inject constructor()
    : IRepo {

    override suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter): ResponseState<List<BluetoothDevice>> {
        return if(bluetoothAdapter == null){
            ResponseState.Error("Adapter is NUll")
        }else{
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            ResponseState.Success(ArrayList(pairedDevices))
        }
    }

    override suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice
    ): ResponseState<BluetoothSocket> {
        val uuid : UUID = UUID.fromString(Const.UUID)
        bluetoothDevice.createRfcommSocketToServiceRecord(uuid).apply {
            connect()
            return if(isConnected){
                ResponseState.Success(this)
            }else{
                ResponseState.Error("Connection Failed")
            }
        }

    }


}