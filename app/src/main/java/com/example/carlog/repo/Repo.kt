package com.example.carlog.repo
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.data.ModelResponse
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.control.TimingAdvanceCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.fuel.FuelLevelCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import com.pnuema.android.obd.commands.OBDCommand
import com.pnuema.android.obd.enums.ObdModes
import com.pnuema.android.obd.models.PID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

class Repo
@Inject constructor() : IRepo {

    override suspend fun getSpeed(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val obdConnection = ObdDeviceConnection(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
                val speedResponse = obdConnection.run(SpeedCommand(), delayTime = 5000L, maxRetries = 100)

                ResponseState.Success(speedResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get speed: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getTiming(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = bluetoothSocket.outputStream
                val inputStream = bluetoothSocket.inputStream

                val obdConnection = ObdDeviceConnection(inputStream, outputStream)
                val rpmResponse = obdConnection.run(TimingAdvanceCommand(), delayTime = 10000L, maxRetries = 20)

                ResponseState.Success(rpmResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get Timing: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val obdConnection = ObdDeviceConnection(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
                val rpmResponse = obdConnection.run(RPMCommand(), delayTime = 1000L, maxRetries = 50)

                ResponseState.Success(rpmResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get RPM: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getFuel(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = bluetoothSocket.outputStream
                val inputStream = bluetoothSocket.inputStream

                val obdConnection = ObdDeviceConnection(inputStream, outputStream)
                val rpmResponse = obdConnection.run(FuelLevelCommand(), delayTime = 1000L, maxRetries = 30)

                ResponseState.Success(rpmResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get Fuel: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun sendCommand(command: String, delayTime : Long = 0,bluetoothSocket: BluetoothSocket)  {
        withContext(Dispatchers.IO) {
            val outputStream =  bluetoothSocket.outputStream
            outputStream.write("${command}\r".toByteArray())
            outputStream.flush()
            if (delayTime > 0) {
                delay(delayTime)
            }
        }
    }
    private fun readResponse(bluetoothSocket: BluetoothSocket) : ModelResponse{
        val inputStream = bluetoothSocket.inputStream
        val byteArray = ByteArray(1024)
        var bytes = inputStream.read(byteArray)
        val response = StringBuilder()
        while (bytes != -1) {
            response.append(String(byteArray, 0, bytes))
            bytes = inputStream.read(byteArray)
        }
        val data = response.toString().split(" ").filter { it.isNotEmpty() }
        return ModelResponse(data[1].toFloat(),response.toString(), data[3])
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