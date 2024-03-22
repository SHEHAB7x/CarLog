package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState
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

                sendCommand(outputStream, Const.SPEED)

                val result = readResult(inputStream).toString()

                parseSpeedResult(result)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get speed: ${e.localizedMessage}")
            }
        }
    }


    override suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<String> {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = bluetoothSocket.outputStream
                val inputStream = bluetoothSocket.inputStream

                sendCommand(outputStream)
                val result = readResult(inputStream)

                ResponseState.Success(result.toString())
            } catch (e: IOException) {
                ResponseState.Error("Failed to get RPM: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun sendCommand(outputStream: OutputStream, command: String) {
        withContext(Dispatchers.IO) {
            setProtocolToAuto(outputStream)
            outputStream.write(command.toByteArray())
            outputStream.flush()
        }
        delay(600)
    }

    private suspend fun setProtocolToAuto(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            // AT SP 0 is the command for automatic protocol selection
            val autoProtocolCommand = "AT SP 0\r"
            outputStream.write(autoProtocolCommand.toByteArray())
            outputStream.flush()
        }
        delay(100) // Give some time for the device to process the command
    }

    private suspend fun sendCommand(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            outputStream.write(Const.RPM.toByteArray())
            outputStream.flush()
        }
        delay(300)
    }

    private suspend fun readResult(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            val buffer = ByteArray(1024)
            val bytesRead = inputStream.read(buffer)
            String(buffer, 0, bytesRead)
        }
    }

    private fun parseSpeedResult(result: String): ResponseState<Int> {
        try {
            val speedInHex = result.trim()
            val speed = speedInHex.toInt(16)
            return ResponseState.Success(speed)
        } catch (e: NumberFormatException) {
            return ResponseState.Error("Failed to parse speed: ${e.localizedMessage}")
        }
    }
}