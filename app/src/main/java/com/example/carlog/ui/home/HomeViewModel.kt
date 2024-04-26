package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _speedValues = mutableListOf<Int>()
    val speedValues: List<Int> get() = _speedValues.toList()

    fun getSpeed(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val speedState = repo.getSpeed(bluetoothSocket)
                    _liveSpeed.postValue(speedState)
                    if (speedState is ResponseState.Success) {
                        _speedValues.add(speedState.data)
                    }
                    delay(1000)
                } catch (e: Exception) {
                    _liveSpeed.postValue(e.localizedMessage?.let { ResponseState.Error("Speed Exception: $it") })
                }
            }
        }
    }

    fun getRPM(bluetoothSocket: BluetoothSocket){
        viewModelScope.launch {
            while (isActive){
                try{
                    _liveRPM.postValue(repo.getRPM(bluetoothSocket))
                    delay(2000)
                }catch (e: Exception){
                    _liveRPM.postValue((e.localizedMessage?.let { ResponseState.Error("RPM Exception: $it") }))
                }
            }
        }
    }

}