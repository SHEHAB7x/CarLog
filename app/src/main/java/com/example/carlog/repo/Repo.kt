package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.control.TimingAdvanceCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.fuel.FuelLevelCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
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

    override suspend fun getSpeed(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val obdConnection = ObdDeviceConnection(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
                val speedResponse = obdConnection.run(SpeedCommand(), delayTime = 1000L, maxRetries = 5)

                ResponseState.Success(speedResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get speed: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {

                val obdConnection = ObdDeviceConnection(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
                val rpmResponse = obdConnection.run(RPMCommand(), delayTime = 2000L, maxRetries = 3)

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
                val rpmResponse = obdConnection.run(FuelLevelCommand(), delayTime = 10000L, maxRetries = 3)

                ResponseState.Success(rpmResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get Fuel: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getTiming(bluetoothSocket: BluetoothSocket): ResponseState<ObdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = bluetoothSocket.outputStream
                val inputStream = bluetoothSocket.inputStream

                val obdConnection = ObdDeviceConnection(inputStream, outputStream)
                val rpmResponse = obdConnection.run(TimingAdvanceCommand(), delayTime = 10000L, maxRetries = 3)

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
}