package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.control.TimingAdvanceCommand
import com.github.eltonvs.obd.command.removeAll
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.github.pires.obd.commands.SpeedCommand
import com.github.pires.obd.commands.ObdMultiCommand
import com.github.pires.obd.commands.ObdCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.github.pires.obd.commands.fuel.FuelLevelCommand

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.system.measureTimeMillis

class Repo
@Inject constructor() : IRepo {

    override suspend fun getSpeed(bluetoothSocket: BluetoothSocket) : ResponseState<String>{
        return withContext(Dispatchers.IO) {
            try {
                val speedCommand = SpeedCommand()
                speedCommand.run(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
                val speed = speedCommand.formattedResult

                ResponseState.Success(speed)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get speed: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<String> {
        return withContext(Dispatchers.IO) {
            try {
                val engineRPMCommand = RPMCommand()
                engineRPMCommand.run(bluetoothSocket.inputStream,bluetoothSocket.outputStream)
                val engineRPM = engineRPMCommand.formattedResult
                ResponseState.Success(engineRPM)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get RPM: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getFuel(bluetoothSocket: BluetoothSocket): ResponseState<String> {
        return withContext(Dispatchers.IO) {
            try {

                val fuelCommand = FuelLevelCommand()
                fuelCommand.run(bluetoothSocket.inputStream, bluetoothSocket.outputStream)
                val fuelLevel = fuelCommand.formattedResult

                ResponseState.Success(fuelLevel)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get Fuel: ${e.localizedMessage}")
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