package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ObdRawResponse
import com.example.carlog.network.ResponseState
import com.example.carlog.obd.ObdDeviceConnection
import com.example.carlog.utils.App
import com.example.carlog.utils.Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

class Repo
@Inject constructor() : IRepo {

    private var obdDeviceConnection : ObdDeviceConnection? = null

    override suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter): ResponseState<List<BluetoothDevice>> {
        return run {
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
            try {
                val outputStream = bluetoothSocket.outputStream

                val inputStream = bluetoothSocket.inputStream

                obdDeviceConnection = ObdDeviceConnection(inputStream,outputStream)

                sendCommand(outputStream, Const.SPEED)

                val result = readResult(inputStream).toString()

                parseSpeedResult(result)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get speed: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun sendCommand(outputStream: OutputStream, command: String) {
        withContext(Dispatchers.IO) {
            outputStream.write(command.toByteArray())
            outputStream.flush()
        }
        delay(600)
    }

    private suspend fun readResult(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            String(buffer, 0, bytesRead)
        }
    }

    private fun parseSpeedResult(result: String): ResponseState<Int> {
        return try {
            val speedInHex = result.trim()
            val speed = speedInHex.toInt(16)
            ResponseState.Success(speed)
        } catch (e: NumberFormatException) {
            ResponseState.Error("Failed to parse speed: ${e.localizedMessage}")
        }
    }
}