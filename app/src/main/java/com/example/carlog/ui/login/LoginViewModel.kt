package com.example.carlog.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.carlog.data.ModelUser
import com.example.carlog.network.ResponseState

class LoginViewModel : ViewModel() {
    private var _loginLiveData = MutableLiveData<ResponseState<ModelUser>>()
    val loginLiveData : LiveData<ResponseState<ModelUser>> = _loginLiveData

    fun loginUser(email: String, password: String) {

    }
}