package com.example.carlog.utils

import android.app.Application
import android.bluetooth.BluetoothSocket
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    var bluetoothSocket: BluetoothSocket? = null
}