package com.example.carlog.data.rating

import com.example.carlog.data.ModelAcceleration
import com.example.carlog.ui.home.HomeViewModel
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class Rating {
    companion object {
        const val SPEED_LIMIT = 80
        private const val MIN_EVENT_DURATION = 3
        private const val MAX_GAP_BETWEEN_EVENTS = 5
        private const val ACCELERATION_LIMIT = 3.0
        private const val BRAKING_LIMIT = -3.2
    }

    data class Event(var startIndex: Int, var endIndex: Int, var maxSpeed: Int, var duration: Int)
    data class Acceleration(var acceleration: Double, var speed: Int)
    data class Acceleration2(val acceleration: Double)


    private var inEvent = false
    private var currentEventStartIndex = 0
    private var currentEventDuration = 0
    private var currentMaxSpeed = Int.MIN_VALUE
    private val events = mutableListOf<Event>()

    fun speedRating(dataList: List<HomeViewModel.SpeedValue>): Double {
        dataList.forEachIndexed { index, speedValue ->
            val speed = speedValue.speed

            if (speed > SPEED_LIMIT) {
                if (!inEvent) {
                    inEvent = true
                    currentEventStartIndex = index
                    currentEventDuration = 0
                    currentMaxSpeed = speed
                } else {
                    currentEventDuration++
                    currentMaxSpeed = max(currentMaxSpeed, speed)
                }
            } else if (inEvent) {
                handleEventEnd(dataList, index)
            }
        }

        val tripRates = events.map { it.maxSpeed * it.duration }
        val totalTripRate = tripRates.sum()
        val maxSpeed = dataList.maxOfOrNull { it.speed } ?: 1

        return (totalTripRate / (maxSpeed * dataList.size).toDouble())*100
    }

    private fun handleEventEnd(dataList: List<HomeViewModel.SpeedValue>, index: Int) {
        if (currentEventDuration >= MIN_EVENT_DURATION) {
            if (events.isNotEmpty()) {
                val lastEvent = events.last()
                val endTimeOfLastEvent = dataList[lastEvent.endIndex].time
                val startTimeOfCurrentEvent = dataList[currentEventStartIndex].time
                val currentGap = startTimeOfCurrentEvent?.minus(endTimeOfLastEvent!!)

                if (currentGap != null && currentGap <= MAX_GAP_BETWEEN_EVENTS) {
                    lastEvent.maxSpeed = max(lastEvent.maxSpeed, currentMaxSpeed)
                    lastEvent.duration += currentEventDuration
                    lastEvent.endIndex = index - 1
                } else {
                    events.add(Event(currentEventStartIndex, index - 1, currentMaxSpeed, currentEventDuration))
                }
            } else {
                events.add(Event(currentEventStartIndex, index - 1, currentMaxSpeed, currentEventDuration))
            }
        }
        inEvent = false
    }

    fun accelerationRating(dataList: List<HomeViewModel.SpeedValue>): ModelAcceleration {
        val accelerationValues = calculateRateValues(dataList, true)
        val accTimes = accTimes(accelerationValues)
        return getFinalRate(accelerationValues,accTimes, true)
    }
    private fun accTimes(list : List<Acceleration>) : Int{
        var count = 0
        for (i in list.indices){
            if(list[i].acceleration > ACCELERATION_LIMIT)
                count++
        }
        return count
    }

    fun brakingRating(dataList: List<HomeViewModel.SpeedValue>): ModelAcceleration {
        val brakingValues = calculateRateValues(dataList, false)
        val decTimes = decTimes(brakingValues)
        return getFinalRate(brakingValues, decTimes, false)
    }
    private fun decTimes(list : List<Acceleration>) : Int{
        var count = 0
        for (i in list.indices){
            if(list[i].acceleration < BRAKING_LIMIT)
                count++
        }
        return count
    }

    private fun calculateRateValues(dataList: List<HomeViewModel.SpeedValue>, isAcceleration: Boolean): List<Acceleration> {
        val rateValues = mutableListOf<Acceleration>()
        for (i in 4 until dataList.size step 5) {
            val rate = (dataList[i].speed - dataList[i - 4].speed) / 5.0
            if ((isAcceleration && rate > 0) || (!isAcceleration && rate < 0)) {
                rateValues.add(Acceleration(rate, dataList[i].speed))
            }
        }
        return rateValues
    }

    private fun getFinalRate(rateValues: List<Acceleration>, accTimes: Int, isAcceleration: Boolean): ModelAcceleration {
        val scores = rateValues.map { rateScore(it.speed, it.acceleration, isAcceleration) }
        val totalScore = scores.filter { it != 0.0 }.average()
        val maxValue = rateValues.maxByOrNull { it.acceleration }?.acceleration
        return ModelAcceleration(totalScore, accTimes,maxValue)
    }

    private fun rateScore(speed: Int, rate: Double, isAcceleration: Boolean): Double {
        val limit = if (isAcceleration) ACCELERATION_LIMIT else BRAKING_LIMIT
        val result = if (isAcceleration) {
            if (rate > limit) ((rate - limit) / limit) * 100 else 0.0
        } else {
            if (rate < limit) ((limit - rate) / limit) * 100 else 0.0
        }
        return result
    }


    fun calculateMean(values: List<Acceleration2>?): Double {
        if (values.isNullOrEmpty()) {
            return 0.0 // or handle as per your application's requirements
        }

        val sum = values.sumByDouble { it.acceleration }
        return sum / values.size
    }

    fun calculateStandardDeviation(values: List<Acceleration2>?, mean: Double): Double {
        if (values.isNullOrEmpty()) {
            return 0.0 // or handle as per your application's requirements
        }

        val sum = values.sumByDouble { (it.acceleration - mean).pow(2) }
        val variance = sum / (values.size - 1)
        return sqrt(variance)
    }

    fun calculateStandardDeviationPercentage(values: List<Acceleration2>?): Double {
        val mean = calculateMean(values)
        val stdDeviation = calculateStandardDeviation(values, mean)

        if (mean == 0.0) {
            return 0.0 // Or handle this case according to your application's logic
        }

        return (stdDeviation / mean) * 100
    }


    private fun calculatePercentage(newVal: Int): Int {
        val percentage = (newVal - 3000) / 3000.0 * 100
        return if (percentage > 100) 100 else percentage.toInt()
    }

    // Function to calculate the average percentage
    fun calculateAveragePercentage(data: List<Int>): Int {
        // Calculate percentages for the given pairs
        val percentage1 = calculatePercentage(data[2])
        val percentage2 = calculatePercentage(data[3])

        // Calculate the average percentage
        return (percentage1 + percentage2) / 2
    }
}
