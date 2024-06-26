package com.example.carlog.ui.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carlog.data.ModelMessages
import com.example.carlog.network.ResponseState
import com.example.carlog.repo.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ChatsViewModel @Inject constructor(private val repo : Repo) : ViewModel() {
    private val _chatsLiveData = MutableLiveData<ResponseState<ModelMessages>>()
    val chatsLiveData : LiveData<ResponseState<ModelMessages>> get() = _chatsLiveData

    fun getMessages(){
        _chatsLiveData.value = ResponseState.Loading
        viewModelScope.launch {
            try {
                _chatsLiveData.postValue(repo.getAllMessages())
            }catch (e : Exception){
                _chatsLiveData.value = ResponseState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}