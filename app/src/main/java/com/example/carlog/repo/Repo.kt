package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import com.example.carlog.utils.Const.Companion.OBD_RPM_RESPONSE
import com.example.carlog.utils.Const.Companion.OBD_SPEED
import com.example.carlog.utils.Const.Companion.OBD_SPEED_RESPONSE
import com.example.carlog.utils.Const.Companion.RPM
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
        val speedResponse =
            sendCommand(bluetoothSocket.inputStream, bluetoothSocket.outputStream, OBD_SPEED)
        if (!speedResponse.contains(OBD_SPEED_RESPONSE)) {
            ResponseState.Error("OBD_ERROR\", \"Invalid response for speed command: $speedResponse")
        }

        val speed = parseResponse(speedResponse)
        return ResponseState.Success(speed)
    }

    override suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<Int> {
        val rpmResponse =
            sendCommand(bluetoothSocket.inputStream, bluetoothSocket.outputStream, RPM)
        if (!rpmResponse.contains(OBD_RPM_RESPONSE)) {
            ResponseState.Error("OBD_ERROR\", \"Invalid response for RPM command: $rpmResponse")
        }
        val rpm = parseResponse(rpmResponse)
        return ResponseState.Success(rpm)
    }

    private suspend fun sendCommand(inputStream: InputStream, outputStream: OutputStream, command: String): String {
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
                Log.e("dosifdsj", "sdfsd")
            }
        }

        return String(buffer, 0, bytesRead).trim()
    }

    private fun parseResponse(response: String): Int {
        val cleanedResponse = response
            .replace("\r", "")
            .replace("\n", "")
            .replace(">", "")

        val dataFields = cleanedResponse.split(" ")
        if (dataFields.size < 4) {
            return -1
        }
        val hexResult = dataFields[3].replace(">", "")
        return hexResult.toInt(16)
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
                ResponseState.Success(this)
            } else {
                ResponseState.Error("Connection Failed")
            }
        }
    }
}