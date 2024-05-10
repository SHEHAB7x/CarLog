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
    private var inEvent = false
    private var currentEventStartIndex = 0
    private var currentEventDuration = 0
    private var currentMaxSpeed = Int.MIN_VALUE
    private val events = mutableListOf<Event>()
    fun rating(dataList : List<HomeViewModel.SpeedValue>) : Double {
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
}