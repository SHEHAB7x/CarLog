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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: Repo) : ViewModel() {
    private val _liveSpeed = MutableLiveData<ResponseState<ObdResponse>>()
    val liveSpeed: LiveData<ResponseState<ObdResponse>> get() = _liveSpeed

    private val _liveRPM = MutableLiveData<ResponseState<ObdResponse>>()
    val liveRPM: LiveData<ResponseState<ObdResponse>> get() = _liveRPM

    fun getSpeed(bluetoothSocket: BluetoothSocket) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    _liveSpeed.postValue(repo.getSpeed(bluetoothSocket))
                } catch (e: Exception) {
                    _liveSpeed.postValue(e.localizedMessage?.let { ResponseState.Error("viewModel s Exception: $it") })
                }
            }
        }
    }

    fun getRPM(bluetoothSocket: BluetoothSocket){
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive){
                try {
                    _liveRPM.postValue(repo.getRPM(bluetoothSocket))
                }catch (e : Exception){
                    _liveRPM.postValue(e.localizedMessage?.let { ResponseState.Error("viewModel s Exception: $it") })
                }
            }
        }
    }

}