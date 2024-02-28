package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class Repo
@Inject constructor() : IRepo {

    override suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter): ResponseState<List<BluetoothDevice>> {
        return if (bluetoothAdapter == null) {
            ResponseState.Error("Adapter is NUll")
        } else {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            ResponseState.Success(ArrayList(pairedDevices))
        }
    }

    override suspend fun connectToDevice(
        bluetoothDevice: BluetoothDevice
    ): ResponseState<BluetoothSocket> {
        val uuid: UUID = UUID.fromString(Const.UUID)
        bluetoothDevice.createRfcommSocketToServiceRecord(uuid).apply {
            connect()
            return if (isConnected) {
                ResponseState.Success(this)
            } else {
                ResponseState.Error("Connection Failed")
            }
        }
    }

    override suspend fun getSpeed(bluetoothSocket: BluetoothSocket): ResponseState<Int> {
        return withContext(Dispatchers.IO) {
            val outputStream = bluetoothSocket.outputStream
            val inputStream = bluetoothSocket.inputStream

            outputStream.write(Const.SPEED.toByteArray())
            outputStream.flush()

            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            val result = String(buffer, 0, bytesRead)

            parseSpeedResult(result)
        }
    }


    private fun parseSpeedResult(result: String): ResponseState<Int> {
        val responseParts = result.split(' ')
        if (responseParts.size > 2) {
            try {
                val speedHex = responseParts[2]
                return ResponseState.Success(Integer.parseInt(speedHex, 16))
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        return ResponseState.Error("-1")
    }
}