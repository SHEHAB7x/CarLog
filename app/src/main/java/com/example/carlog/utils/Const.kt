package com.example.carlog.utils

class Const {
    companion object {
        const val SPEED = "01 0D"
        const val RPM = "01 0C"
        const val FUEL = "012F"
        const val AUTO = "AT SP 0"
        const val UNKNOWN_DEVICE = "Unknown Device"
        const val UUID = "00001101-0000-1000-8000-00805F9B34FB"


        const val OBD_RESET = "AT Z\r"
        const val OBD_ACTIVATE_AUTO_PROTOCOL_SEARCH = "AT SP 0\r"

        const val OBD_SPEED = "01 0D\r"
        const val OBD_SPEED_RESPONSE = "41 0D"
    }
}