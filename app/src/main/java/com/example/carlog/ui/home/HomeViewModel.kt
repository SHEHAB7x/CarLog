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


    private val _liveRPM = MutableLiveData<ResponseState<String>>()

    val liveRPM : LiveData<ResponseState<String>> get() = _liveRPM

    fun getSpeed(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    _liveSpeed.postValue(repo.getSpeed(bluetoothSocket))
                    delay(500)
                } catch (e: Exception) {
                    _liveSpeed.postValue(e.localizedMessage?.let { ResponseState.Error("viewModel s Exception: $it") })
                }
            }
        }
    }

    fun getRPM(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    when (val rpmResponse = repo.getRPM(bluetoothSocket)) {
                        is ResponseState.Success -> {
                            val rpm = rpmResponse.data
                            _liveRPM.postValue(ResponseState.Success(rpm))
                        }
                        is ResponseState.Error -> _liveRPM.postValue(rpmResponse)
                    }
                    delay(1000)
                } catch (e: Exception) {
                    _liveRPM.postValue(e.localizedMessage?.let { ResponseState.Error("viewModel Exception: $it") })
                }
            }
        }
    }
}