package com.example.carlog.ui.home

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carlog.data.ModelResponse
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
    private val _liveSpeed = MutableLiveData<ResponseState<ObdResponse>>()
    val liveSpeed: LiveData<ResponseState<ObdResponse>> get() = _liveSpeed

    private val _liveRPM = MutableLiveData<ResponseState<ObdResponse>>()
    val liveRPM: LiveData<ResponseState<ObdResponse>> get() = _liveRPM

    private val _liveFuel = MutableLiveData<ResponseState<ObdResponse>>()
    val liveFuel: LiveData<ResponseState<ObdResponse>> get() = _liveFuel

    /*private val _liveSpeed = MutableLiveData<ResponseState<ModelResponse>>()
    val liveSpeed: LiveData<ResponseState<ModelResponse>> get() = _liveSpeed

    private val _liveRPM = MutableLiveData<ResponseState<ModelResponse>>()
    val liveRPM: LiveData<ResponseState<ModelResponse>> get() = _liveRPM

    private val _liveFuel = MutableLiveData<ResponseState<ModelResponse>>()
    val liveFuel: LiveData<ResponseState<ModelResponse>> get() = _liveFuel*/


    fun getSpeed(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    _liveSpeed.postValue(repo.getSpeed(bluetoothSocket))
                    delay(1000)
                } catch (e: Exception) {
                    _liveSpeed.postValue(e.localizedMessage?.let { ResponseState.Error("Speed Exception: $it") })
                }
            }
        }
    }
    fun getRPM(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    _liveRPM.postValue(repo.getRPM(bluetoothSocket))
                } catch (e: Exception) {
                    _liveRPM.postValue(e.localizedMessage?.let { ResponseState.Error("RPM Exception: $it") })
                }
            }
        }
    }
    fun getFuel(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    _liveFuel.postValue(repo.getFuel(bluetoothSocket))
                    delay(2000)
                } catch (e: Exception) {
                    _liveFuel.postValue(e.localizedMessage?.let { ResponseState.Error("RPM Exception: $it") })
                }
            }
        }
    }


}