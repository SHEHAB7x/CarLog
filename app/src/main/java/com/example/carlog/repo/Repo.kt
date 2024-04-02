package com.example.carlog.repo

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.carlog.network.ResponseState
import com.example.carlog.utils.Const
import com.github.eltonvs.obd.command.ObdResponse
import com.github.eltonvs.obd.command.RegexPatterns.SEARCHING_PATTERN
import com.github.eltonvs.obd.command.control.TimingAdvanceCommand
import com.github.eltonvs.obd.command.control.VINCommand
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
    private suspend fun runCommand(
        command: String,
        delayTime: Long,
        maxRetries: Int,
        bluetoothSocket: BluetoothSocket
    ): String {
        var rawData = ""
        val inputStream = bluetoothSocket.inputStream
        val outputStream = bluetoothSocket.outputStream
        sendCommand(command, delayTime, outputStream)
        rawData = readRawData(maxRetries, inputStream)
        return rawData
    }

    private suspend fun sendCommand(command: String, delayTime: Long, outputStream: OutputStream) =
        runBlocking {
            withContext(Dispatchers.IO) {
                outputStream.write("${command}\r".toByteArray())
                outputStream.flush()
                if (delayTime > 0) {
                    delay(delayTime)
                }
            }
        }

    private suspend fun readRawData(maxRetries: Int, inputStream: InputStream): String =
        runBlocking {
            var b: Byte
            var c: Char
            val res = StringBuffer()
            var retriesCount = 0

            withContext(Dispatchers.IO) {
                // read until '>' arrives OR end of stream reached (-1)
                while (retriesCount <= maxRetries) {
                    if (inputStream.available() > 0) {
                        b = inputStream.read().toByte()
                        if (b < 0) {
                            break
                        }
                        c = b.toInt().toChar()
                        if (c == '>') {
                            break
                        }
                        res.append(c)
                    } else {
                        retriesCount += 1
                        delay(500)
                    }
                }
                removeAll(SEARCHING_PATTERN, res.toString()).trim()
            }
        }

    override suspend fun getSpeed(bluetoothSocket: BluetoothSocket) : ResponseState<String>{
        return withContext(Dispatchers.IO) {
            try {
                val speedResponse = runCommand(
                    Const.SPEED,
                    delayTime = 1000L,
                    maxRetries = 5,
                    bluetoothSocket = bluetoothSocket
                )

                ResponseState.Success(speedResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get speed: ${e.localizedMessage}")
            }
        }
    }

    override suspend fun getRPM(bluetoothSocket: BluetoothSocket): ResponseState<String> {
        return withContext(Dispatchers.IO) {
            try {
                val rpmResponse = runCommand(
                    Const.RPM,
                    delayTime = 1000L,
                    maxRetries = 5,
                    bluetoothSocket = bluetoothSocket
                )

                ResponseState.Success(rpmResponse)
            } catch (e: IOException) {
                ResponseState.Error("Failed to get RPM: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getFuel(bluetoothSocket: BluetoothSocket): ResponseState<String> {
        return withContext(Dispatchers.IO) {
            try {

                val fuelResponse = runCommand(
                    Const.FUEL,
                    delayTime = 1000L,
                    maxRetries = 5,
                    bluetoothSocket = bluetoothSocket
                )

                ResponseState.Success(fuelResponse)
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
                val rpmResponse =
                    obdConnection.run(TimingAdvanceCommand(), delayTime = 10000L, maxRetries = 3)

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