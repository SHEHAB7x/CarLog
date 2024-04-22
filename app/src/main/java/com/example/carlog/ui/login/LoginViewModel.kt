package com.example.carlog.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carlog.data.ModelUser
import com.example.carlog.network.ResponseState
import com.example.carlog.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repo: Repo) : ViewModel() {
    private var _loginLiveData = MutableLiveData<ResponseState<ModelUser>>()
    val loginLiveData : LiveData<ResponseState<ModelUser>> = _loginLiveData

    fun loginUser(email: String, password: String) {
        _loginLiveData.value = ResponseState.Loading
        viewModelScope.launch {
            try {
                _loginLiveData.postValue(repo.login(email,password))
            } catch (e: Exception) {
                _loginLiveData.value = e.localizedMessage?.let { ResponseState.Error(it) }
            }
        }
    }
}