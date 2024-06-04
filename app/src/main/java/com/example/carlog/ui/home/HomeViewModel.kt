package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carlog.data.ModelAcceleration
import com.example.carlog.data.rating.Rating
import com.example.carlog.network.ResponseState
import com.example.carlog.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repo) : ViewModel() {
    private val _liveSpeed = MutableLiveData<ResponseState<Int>>()
    val liveSpeed: LiveData<ResponseState<Int>> get() = _liveSpeed
    private val _liveRPM = MutableLiveData<ResponseState<Int>>()
    val liveRPM: LiveData<ResponseState<Int>> get() = _liveRPM
    private val _speedValues = mutableListOf<SpeedValue>()

    private val _postTripLiveData = MutableLiveData<ResponseState<Int>>()
    val postTripLiveData: LiveData<ResponseState<Int>> get() = _postTripLiveData

    val speedValues: List<SpeedValue> get() = _speedValues.toList()

    data class SpeedValue(var time: Long?, var speed: Int)

    private val rating: Rating = Rating()
    private var idlingTime = 0L

    fun getData(bluetoothSocket: BluetoothSocket) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            var count = 0
            while (isActive) {
                try {
                    val speedState = repo.getSpeed(bluetoothSocket)
                    _liveSpeed.postValue(speedState)
                    if (speedState is ResponseState.Success) {
                        _speedValues.add(
                            SpeedValue(
                                ((System.currentTimeMillis() - startTime) / 1000),
                                speedState.data
                            )
                        )
                        if (speedState.data == 0) {
                            count++
                            if (count == 120) {
                                idlingTime++
                                count = 0
                            }
                        } else
                            count = 0
                    }

                    delay(1000)

                    _liveRPM.postValue(repo.getRPM(bluetoothSocket))
                } catch (e: Exception) {
                    _liveRPM.postValue((e.localizedMessage?.let { ResponseState.Error("RPM Exception: $it") }))
                    _liveSpeed.postValue(e.localizedMessage?.let { ResponseState.Error("Speed Exception: $it") })
                }
            }
        }
    }


    fun postTrip(
        date: String,
        acceleration: Int,
        deceleration: Int,
        tripTime: String,
        idling: Int,
        overSpeedTimes: Int,
        tripRate: Int,
        maxSpeed: Int,
        maxAcceleration: Int,
        maxBreaking: Int,
        maxIdling: Int,
    ) {
        _postTripLiveData.value = ResponseState.Loading
        viewModelScope.launch {
            try {
                _postTripLiveData.postValue(
                    repo.postTrip(
                        date,
                        acceleration,
                        deceleration,
                        tripTime,
                        idling,
                        overSpeedTimes,
                        tripRate,
                        maxSpeed,
                        maxAcceleration,
                        maxBreaking,
                        maxIdling
                    )
                )
            } catch (e: Exception) {
                _postTripLiveData.value = e.localizedMessage?.let { ResponseState.Error(it) }
            }
        }
    }

    fun getSpeedRate(): Double {
        return rating.speedRating(speedValues)
    }
    fun getMaxSpeed(): Int {
        return speedValues.maxByOrNull { it.speed }?.speed ?: 0
    }

    fun getAccelerationRate(): ModelAcceleration {
        return rating.accelerationRating(speedValues)
    }

    fun getBreakingRate(): ModelAcceleration {
        return rating.brakingRating(speedValues)
    }

    fun getIdlingTime(): Long {
        return idlingTime
    }

    fun getAllMax(){
        val maxSpeed = speedValues.maxByOrNull { it.speed }?.speed

    }


    fun getSpeed(bluetoothSocket: BluetoothSocket) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val speedState = repo.getSpeed(bluetoothSocket)
                    _liveSpeed.postValue(speedState)
                    if (speedState is ResponseState.Success) {
                        _speedValues.add(
                            SpeedValue(
                                ((System.currentTimeMillis() - startTime) / 1000),
                                speedState.data
                            )
                        )
                    }
                    delay(1000)
                } catch (e: Exception) {
                    _liveSpeed.postValue(e.localizedMessage?.let { ResponseState.Error("Speed Exception: $it") })
                }
            }
        }
    }

    fun getRPM(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch {
            while (isActive) {
                try {
                    _liveRPM.postValue(repo.getRPM(bluetoothSocket))
                    delay(2000)
                } catch (e: Exception) {
                    _liveRPM.postValue((e.localizedMessage?.let { ResponseState.Error("RPM Exception: $it") }))
                }
            }
        }
    }
}