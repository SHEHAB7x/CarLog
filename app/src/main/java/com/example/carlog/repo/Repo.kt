package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.carlog.data.ModelAllTrips
import com.example.carlog.data.ModelMessages
import com.example.carlog.data.ModelUser
import com.example.carlog.network.LoginRequestBody
import com.example.carlog.network.ResponseState
import com.example.carlog.network.RetrofitService
import com.example.carlog.network.TripRequestBody
import com.example.carlog.utils.Const
import com.example.carlog.utils.Const.Companion.OBD_RPM_RESPONSE
import com.example.carlog.utils.Const.Companion.OBD_SPEED
import com.example.carlog.utils.Const.Companion.OBD_SPEED_RESPONSE
import com.example.carlog.utils.Const.Companion.RPM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

class Repo
@Inject constructor(private val retrofitService: RetrofitService) : IRepo {

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

    override suspend fun login(email: String, password: String): ResponseState<ModelUser> {
        val response = retrofitService.loginUser(LoginRequestBody(email, password))
        return if (response.statusCode.statusCode == 200) {
            ResponseState.Success(response)
        } else {
            ResponseState.Error("error")
        }
    }

    override suspend fun postTrip(
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
    ): ResponseState<Int> {
        val response = retrofitService.postTrip(
            TripRequestBody(
                date,
                acceleration,
                deceleration,
                tripTime,
                idling,
                overSpeedTimes,
                tripRate,
                maxSpeed,
                maxAcceleration,
                maxBreaking,
                maxIdling
            )
        )
        return if (response == 200) {
            ResponseState.Success(200)
        } else {
            ResponseState.Error("0")
        }
    }

    override suspend fun getAllTrips(): ResponseState<ModelAllTrips> {
        val response = retrofitService.getAllTrips()
        return if (response.statuscode.statusCode == 200) {
            ResponseState.Success(response)
        } else {
            ResponseState.Error("0")
        }
    }

    override suspend fun getAllMessages(): ResponseState<ModelMessages> {
        val response = retrofitService.getAllMessages()
        return if (response.statuscode.statusCode == 200) {
            ResponseState.Success(response)
        } else {
            ResponseState.Error("0")
        }
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
            return 0
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

    private suspend fun resetDevice(inputStream: InputStream?, outputStream: OutputStream?) {
        if (inputStream == null || outputStream == null) {
            Log.e("INIT_ERROR", "Socket not set")
            return
        }

        sendCommand(inputStream, outputStream, Const.OBD_RESET)
        sendCommand(inputStream, outputStream, Const.OBD_ACTIVATE_AUTO_PROTOCOL_SEARCH)
    }

    override suspend fun connectToDevice(bluetoothDevice: BluetoothDevice): ResponseState<BluetoothSocket> {
        val uuid: UUID = UUID.fromString(Const.UUID)
        bluetoothDevice.createRfcommSocketToServiceRecord(uuid).apply {
            connect()
            return if (isConnected) {
                resetDevice(this.inputStream, this.outputStream)
                ResponseState.Success(this)
            } else {
                ResponseState.Error("Connection Failed")
            }
        }
    }
}