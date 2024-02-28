package com.example.carlog.utils

import android.bluetooth.BluetoothSocket
import android.content.Context

object MyBluetoothSocket {

    private var mAppContext: Context? = null
    fun init(appContext: Context?) {
        mAppContext = appContext
    }

    var bluetoothSocket: BluetoothSocket? = null
}