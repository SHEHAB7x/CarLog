package com.example.carlog.data.rating

class Trip {
    private var speed = 0
    private var time: String? = null
    fun Trip(time: String?, speed: Int) {
        this.speed = speed
        this.time = time
    }

}