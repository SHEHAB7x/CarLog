package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carlog.network.ResponseState
import com.example.carlog.repo.Repo
import com.github.eltonvs.obd.command.ObdResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repo) : ViewModel() {
    private val _liveSpeed = MutableLiveData<ResponseState<String>>()
    val liveSpeed: LiveData<ResponseState<String>> get() = _liveSpeed

    private val _liveRPM = MutableLiveData<ResponseState<String>>()
    val liveRPM: LiveData<ResponseState<String>> get() = _liveRPM

    private val _liveFuel = MutableLiveData<ResponseState<String>>()
    val liveFuel: LiveData<ResponseState<String>> get() = _liveFuel

    fun getSpeed(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO){
            while (isActive){
                try {
                    _liveSpeed.postValue(repo.getSpeed(bluetoothSocket))
                    delay(1000)
                }catch (e:Exception){
                    _liveSpeed.postValue(e.localizedMessage?.let { ResponseState.Error("Speed Exception: $it") })
                }
            }
        }
    }


    fun getRPM(bluetoothSocket: BluetoothSocket){
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive){
                try {
                    _liveRPM.postValue(repo.getRPM(bluetoothSocket))
                    delay(3000)
                }catch (e : Exception){
                    _liveRPM.postValue(e.localizedMessage?.let { ResponseState.Error("RPM Exception: $it") })
                }
            }
        }
    }

    fun getFuel(bluetoothSocket: BluetoothSocket){
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive){
                try {
                    _liveFuel.postValue(repo.getFuel(bluetoothSocket))
                    delay(2000)
                }catch (e : Exception){
                    _liveFuel.postValue(e.localizedMessage?.let { ResponseState.Error("RPM Exception: $it") })
                }
            }
        }
    }



}