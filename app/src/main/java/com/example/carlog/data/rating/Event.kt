package com.example.carlog.data.rating

class Event {
    private var startIndex = 0
    private var endIndex = 0
    private var maxSpeed = 0
    private var duration = 0

    fun Event(startIndex: Int, endIndex: Int, maxSpeed: Int, duration: Int) {
        this.startIndex = startIndex
        this.endIndex = endIndex
        this.maxSpeed = maxSpeed
        this.duration = duration
    }

    // Getters and setters for the Event class
    fun getStartIndex(): Int {
        return startIndex
    }

    fun setStartIndex(startIndex: Int) {
        this.startIndex = startIndex
    }

    fun getEndIndex(): Int {
        return endIndex
    }

    fun setEndIndex(endIndex: Int) {
        this.endIndex = endIndex
    }

    fun getMaxSpeed(): Int {
        return maxSpeed
    }

    fun setMaxSpeed(maxSpeed: Int) {
        this.maxSpeed = maxSpeed
    }

    fun getDuration(): Int {
        return duration
    }

    fun setDuration(duration: Int) {
        this.duration = duration
    }

}