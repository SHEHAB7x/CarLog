package com.example.carlog.data.rating

import com.example.carlog.ui.home.HomeViewModel
import kotlin.math.max

class Rating(){
    private companion object {
        const val SPEED_LIMIT = 20
        const val MIN_EVENT_DURATION = 3
        const val MAX_GAP_BETWEEN_EVENTS = 5
    }
    data class Event(var startIndex: Int, var endIndex: Int, var maxSpeed: Int, var duration: Int)
    data class Acceleration(var acceleration: Double, var speed: Int)
    private var inEvent = false
    private var currentEventStartIndex = 0
    private var currentEventDuration = 0
    private var currentMaxSpeed = Int.MIN_VALUE
    private val events = mutableListOf<Event>()
    private val accelerationLimit = 3.0
    private val breakingLimit = -3.0

    fun speedRating(dataList : List<HomeViewModel.SpeedValue>) : Double {
        for (i in dataList.indices) {
            val speed = dataList[i].speed

            if (speed > SPEED_LIMIT) {
                if (!inEvent) {
                    inEvent = true
                    currentEventStartIndex = i
                    currentEventDuration = 0
                    currentMaxSpeed = speed
                } else {
                    currentEventDuration++
                    currentMaxSpeed = max(currentMaxSpeed, speed)
                }
            } else if (inEvent) {
                if (currentEventDuration >= MIN_EVENT_DURATION) {
                    if (events.isNotEmpty()) {

                        val lastEvent = events.last()

                        val endTimeOfLastEvent = dataList[lastEvent.endIndex].time //end time of last event

                        val startTimeOfCurrentEvent = dataList[currentEventStartIndex].time //start time of current event

                        val currentGap = startTimeOfCurrentEvent?.minus(endTimeOfLastEvent!!)

                        if (currentGap!! <= MAX_GAP_BETWEEN_EVENTS) {
                            lastEvent.maxSpeed = max(lastEvent.maxSpeed, currentMaxSpeed)
                            lastEvent.duration += currentEventDuration
                            lastEvent.endIndex = i - 1
                        } else {
                            events.add(Event(currentEventStartIndex, i - 1, currentMaxSpeed, currentEventDuration))
                        }

                    } else {
                        events.add(Event(currentEventStartIndex, i - 1, currentMaxSpeed, currentEventDuration))
                    }
                }
                inEvent = false
            }
        }

        val tripRates = events.map { event ->
            event.maxSpeed * event.duration
        }

        val totalTripRate = tripRates.sum()
        val overallTripRate = totalTripRate / ((dataList.maxOf { it.speed }) * dataList.size).toDouble()

        return overallTripRate
    }

    //1
    fun accelerationRating(dataList : List<HomeViewModel.SpeedValue>) : Double{
        val accelerationValues = calculateAcceleration(dataList)
        val finalRate = getAccelerationRate(accelerationValues)
        return finalRate
    }
    //2
    private fun calculateAcceleration(dataList: List<HomeViewModel.SpeedValue>): List<Acceleration> {
        val accelerationValues = mutableListOf<Acceleration>()
        for (i in 4 until dataList.size step 5) {
            val acceleration = (dataList[i].speed - dataList[i-4].speed) / 5.0
            if(acceleration > 0)
                accelerationValues.add(Acceleration(acceleration, dataList[i].speed))
        }
        return accelerationValues
    }
    //3
    private fun getAccelerationRate(accelerationWithSpeed : List<Acceleration>): Double {
        val allAccelerations = accelerationWithSpeed.map { accelerationScore(it.speed, it.acceleration) }
        return accelerationFinalRate(allAccelerations)
    }
    //4
    private fun accelerationScore(speed: Int, acc: Double): Double {
        val increaseResult = if (acc > accelerationLimit) {
            ((acc - accelerationLimit) / accelerationLimit) * 100
        } else {
            return 0.0
        }
        return when (speed) {
            in 30..40 -> increaseResult * 0.1
            in 40..50 -> increaseResult * 0.32
            in 50..60 -> increaseResult * 0.8
            else -> increaseResult * 0.95
        }
    }
    //5
    private fun accelerationFinalRate(list: List<Double>): Double {
        val filteredList = list.filter { it != 0.0 }
        return if (filteredList.isNotEmpty()) filteredList.average() else 0.0
    }


    fun breakingRate(dataList : List<HomeViewModel.SpeedValue>) : Double{
        val breakingValues = calculateBreaking(dataList)
        return getBreakingRate(breakingValues)
    }

    private fun calculateBreaking(dataList: List<HomeViewModel.SpeedValue>): List<Acceleration> {
        val breakingValues = mutableListOf<Acceleration>()
        for (i in 4 until dataList.size step 5) {
            val acceleration = (dataList[i].speed - dataList[i - 4].speed) / 5.0
            if (acceleration < 0) {
                breakingValues.add(Acceleration(acceleration, dataList[i].speed))
            }
        }
        return breakingValues
    }

    private fun getBreakingRate(breakingValues: List<Acceleration>): Double {
        val allBreaking = breakingValues.map { breakingScore(it.speed, it.acceleration) }
        return breakingFinalRate(allBreaking)
    }

    private fun breakingScore(speed: Int, acceleration: Double): Double {
        val decreaseResult = if (acceleration < breakingLimit) {
            ((breakingLimit - acceleration) / breakingLimit) * 100
        } else {
            return 0.0
        }
        return when (speed) {
            in 30..40 -> decreaseResult * 0.1
            in 40..50 -> decreaseResult * 0.32
            in 50..60 -> decreaseResult * 0.8
            else -> decreaseResult * 0.9
        }
    }

    private fun breakingFinalRate(allBreaking: List<Double>): Double {
        val filteredList = allBreaking.filter { it != 0.0 }
        return if (filteredList.isNotEmpty()) filteredList.average() else 0.0
    }

}