package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import com.example.carlog.utils.Const.Companion.OBD_ACTIVATE_AUTO_PROTOCOL_SEARCH
import com.example.carlog.utils.Const.Companion.OBD_RESET
import com.example.carlog.utils.Const.Companion.OBD_SPEED
import com.example.carlog.utils.Const.Companion.OBD_SPEED_RESPONSE
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.control.TimingAdvanceCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.fuel.FuelLevelCommand
import com.github.eltonvs.obd.command.removeAll
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class Repo
@Inject constructor() : IRepo {

    override suspend fun getSpeed(bluetoothSocket: BluetoothSocket): ResponseState<Int> {
        val speedResponse = sendCommand(bluetoothSocket.inputStream, bluetoothSocket.outputStream, OBD_SPEED)
        if (!speedResponse.contains(OBD_SPEED_RESPONSE)) {
            ResponseState.Error("OBD_ERROR\", \"Invalid response for speed command: $speedResponse")
        }

        val speed = parseSpeed(speedResponse)
        return ResponseState.Success(speed)
    }
    suspend fun resetDevice(inputStream: InputStream?, outputStream: OutputStream?) {
        if (inputStream == null || outputStream == null) {
            Log.e("INIT_ERROR", "Socket not set")
            return
        }

        sendCommand(inputStream, outputStream, OBD_RESET)
        sendCommand(inputStream, outputStream, OBD_ACTIVATE_AUTO_PROTOCOL_SEARCH)
    }


    private suspend fun sendCommand(
        inputStream: InputStream,
        outputStream: OutputStream,
        command: String
    ): String {
        val sendData = command.toByteArray()

        withContext(Dispatchers.IO) {
            try {
                outputStream.write(sendData)
            } catch (e: IOException) {
                ResponseState.Error("\"OBD_ERROR\", \"write: Disconnected by obd\"")
            }
        }

        withContext(Dispatchers.IO) {
            try {
                outputStream.flush()
            } catch (e: IOException) {
                ResponseState.Error("\"OBD_ERROR\", \"flush: Disconnected by obd\"")
            }
        }
        delay(500)

        val buffer = ByteArray(1024)

        val bytesRead = withContext(Dispatchers.IO) {
            try {
                inputStream.read(buffer)
            } catch (e: IOException) {
                ResponseState.Error("Error read buffer ${e.localizedMessage}")
                Log.e("dosifdsj","sdfsd")
            }
        }

        return String(buffer, 0, bytesRead).trim()
    }

    private fun parseSpeed(response: String): Int {
        val cleanedResponse = response.replace("\r", "").replace("\n", "").replace(">", "")
        val dataFields = cleanedResponse.split(" ")
        if (dataFields.size < 4) {
            return -1
        }
        val hexResult = dataFields[3].replace(">", "")
        return hexResult.toInt(16)
    }


 /*   override suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val obdConnection =
                    ObdDeviceConnection(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
                val rpmResponse =
                    obdConnection.run(RPMCommand(), delayTime = 1000L, maxRetries = 50)

                ResponseState.Success(rpmResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get RPM: ${e.localizedMessage}")
            }
        }
    }*/

    suspend fun getTiming(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = bluetoothSocket.outputStream
                val inputStream = bluetoothSocket.inputStream

                val obdConnection = ObdDeviceConnection(inputStream, outputStream)
                val rpmResponse =
                    obdConnection.run(TimingAdvanceCommand(), delayTime = 10000L, maxRetries = 20)

                ResponseState.Success(rpmResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get Timing: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter): ResponseState<List<BluetoothDevice>> {
        return run {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            ResponseState.Success(ArrayList(pairedDevices))
        }
    }

    override suspend fun connectToDevice(bluetoothDevice: BluetoothDevice): ResponseState<BluetoothSocket> {
        val uuid: UUID = UUID.fromString(Const.UUID)
        bluetoothDevice.createRfcommSocketToServiceRecord(uuid).apply {
            connect()
            return if (isConnected) {
                resetDevice(this.inputStream,this.outputStream)
                ResponseState.Success(this)
            } else {
                ResponseState.Error("Connection Failed")
            }
        }
    }
}