package com.example.carlog.ui.connect

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carlog.network.ResponseState
import com.example.carlog.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConnectViewModel
@Inject constructor(private val repo: Repo) : ViewModel() {
    private val _devices = MutableLiveData<ResponseState<List<BluetoothDevice>>>()
    val devices: LiveData<ResponseState<List<BluetoothDevice>>> get() = _devices

    private val _connectionStateLiveData = MutableLiveData<ResponseState<BluetoothSocket>>()
    val connectionStateLiveData: LiveData<ResponseState<BluetoothSocket>> get() = _connectionStateLiveData
    fun initializeBluetooth(bluetoothAdapter: BluetoothAdapter) {
        _devices.value = ResponseState.Loading
        viewModelScope.launch {
            _devices.postValue(repo.getPairedDevices(bluetoothAdapter))
        }
    }


    fun connectToDevice(bluetoothDevice: BluetoothDevice) {


        viewModelScope.launch {
            try {
                _connectionStateLiveData.value = ResponseState.Loading
                val result = withContext(Dispatchers.IO) {
                    repo.connectToDevice(bluetoothDevice)
                }
                _connectionStateLiveData.postValue(result)
            }catch (e : Exception){
                _connectionStateLiveData.postValue(e.localizedMessage?.let { ResponseState.Error(it)})
            }

        }
    }
}